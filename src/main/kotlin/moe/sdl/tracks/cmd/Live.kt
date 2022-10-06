package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import moe.sdl.tracks.config.client
import moe.sdl.tracks.util.Log
import moe.sdl.tracks.util.ModuleScope
import moe.sdl.tracks.util.OsType
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.firstOrNullMatched
import moe.sdl.tracks.util.osType
import moe.sdl.tracks.util.string.splitToList
import moe.sdl.tracks.util.string.toReadable
import moe.sdl.yabapi.api.fetchLiveStream
import moe.sdl.yabapi.api.getUserCard
import moe.sdl.yabapi.data.GeneralCode
import moe.sdl.yabapi.data.live.LiveRoomStatus
import moe.sdl.yabapi.data.stream.LiveQnQuality
import moe.sdl.yabapi.data.stream.LiveStreamPlayUrl
import moe.sdl.yabapi.data.stream.LiveStreamRequest
import java.util.concurrent.TimeUnit

private const val CUSTOM_PREFIX = "custom:"

private val OPEN_CMD = if (osType == OsType.WINDOWS) "start" else "open"

class Live : CliktCommand(
    "获取直播流链接", name = "live"
) {
    val ids by argument("ID", help = "直播房间号").int().multiple(true).unique()

    val player by option(
        "-p", "-player", help = "使用指定播放器打开, 可用 [iina, potplayer, vlc, nplayer, custom]"
    ).convert {
        when (it.lowercase()) {
            "iina" -> "iina://weblink?url={url}"
            "potplayer" -> "potplayer://{url}"
            "vlc" -> "vlc://{url}"
            "nplayer" -> "nplayer-{url}"
            else -> {
                if (!it.startsWith(CUSTOM_PREFIX)) {
                    throw PrintMessage(
                        """
                        输入了错误的播放器类型, 可用 [iina, potplayer, vlc, nplayer]
                        或者使用 custom: 开头, 后接自定义 url, m3u8 链接使用 {url} 表示
                        如: -player custom:myplayer://{url}
                    """.trimIndent()
                    )
                }
                val custom = it.drop(CUSTOM_PREFIX.length)
                if (!custom.contains("{url}")) {
                    throw PrintMessage(
                        """
                        自定义播放器需使用 custom: 后接自定义 url, m3u8 链接使用 {url} 表示
                        如: -player custom:myplayer://{url}
                    """.trimIndent()
                    )
                }
                custom
            }
        }
    }

    val quality by option("-q", "-quality", help = "直播画质").choice(
        ignoreCase = true, choices = mapOf(
            "fast" to LiveQnQuality.FAST,
            "流畅" to LiveQnQuality.FAST,
            "std" to LiveQnQuality.STANDARD,
            "standard" to LiveQnQuality.STANDARD,
            "高清" to LiveQnQuality.STANDARD,
            "high" to LiveQnQuality.HIGH,
            "超清" to LiveQnQuality.HIGH,
            "bluray" to LiveQnQuality.BLU_RAY,
            "blu-ray" to LiveQnQuality.BLU_RAY,
            "蓝光" to LiveQnQuality.BLU_RAY,
            "dolby" to LiveQnQuality.BLU_RAY_DOLBY,
            "杜比" to LiveQnQuality.BLU_RAY_DOLBY,
            "origin" to LiveQnQuality.ORIGIN,
            "原画" to LiveQnQuality.ORIGIN,
            "4k" to LiveQnQuality.UHD,
        )
    ).default(LiveQnQuality.ORIGIN)

    private val defaultProtocols = listOf("http_hls", "http_stream")
    private val protocol by option(
        "-protocol", "-P", help = "协议优先级, 默认 $defaultProtocols"
    ).convert { str ->
        str.splitToList().map {
            val protocol = it.lowercase()
            if (!defaultFormats.contains(protocol)) {
                throw PrintMessage("""未知的协议 "$it", 可用: $$defaultProtocols""")
            }
            protocol
        }.also {
            Log.debug { "Priority of live protocols: ${it.joinToString()}" }
        }
    }.default(defaultProtocols)

    private val defaultFormats = listOf("ts", "fmp4", "flv")
    private val format by option(
        "-format", "-f", help = "封装优先级, 默认 $defaultFormats"
    ).convert { str ->
        str.splitToList().map {
            val format = it.lowercase()
            if (!defaultFormats.contains(format)) {
                throw PrintMessage("""未知的格式 "$it", 可用: $defaultFormats""")
            }
            format
        }.also {
            Log.debug { "Priority of live formats: ${it.joinToString()}" }
        }
    }.default(defaultFormats)

    private val defaultCodecs = listOf("avc", "hevc")
    private val codec by option(
        "-codec", "-c", help = "编码优先级, 默认 [avc, hevc]"
    ).convert { str ->
        str.splitToList().map {
            when {
                it.matches(Regex("""^\s*(hevc|h\.?265)\s*$""", RegexOption.IGNORE_CASE)) -> "hevc"
                it.matches(Regex("""^\s*(avc|h\.?264)\s*$""", RegexOption.IGNORE_CASE)) -> "avc"
                else -> throw UsageError("未知的视频编码 '$it', 可用: $defaultCodecs")
            }
        }.also {
            Log.debug { "Priority of live codecs: ${it.joinToString()}" }
        }
    }.default(defaultCodecs)

    override fun run(): Unit = runBlocking {
        val moduleScope = ModuleScope("LiveFetcher")
        ids.map { id ->
            moduleScope.launch l@{
                val stream = client.fetchLiveStream(
                    id, LiveStreamRequest(
                        qnQuality = quality
                    )
                )
                val data = stream.data
                if (stream.code != GeneralCode.SUCCESS || data == null) {
                    echo("@|red,bold 获取 $id 直播间信息失败: ${stream.code} ${stream.message}|@".color)
                    return@l
                }
                val liverName = async a@{
                    withTimeoutOrNull(5000) n@{
                        val uid = data.uid ?: return@n null
                        client.getUserCard(uid, false).data?.card?.name
                    } ?: id.toString()
                }
                val playUrl = data.playUrlInfo?.playUrl
                if (data.status == LiveRoomStatus.NOT_LIVE || playUrl == null) {
                    echo("@|red,bold [${liverName.await()}] 获取直播流信息失败, 可能未开播 |@".color)
                    return@l
                }
                fun filter(playUrl: LiveStreamPlayUrl) =
                    playUrl.stream.firstOrNullMatched(protocol) { i, protocol ->
                        i.protocolName == protocol
                    }?.format?.firstOrNullMatched(format) { i, format ->
                        i.formatName == format
                    }?.codec?.firstOrNullMatched(codec) { i, codec ->
                        i.codecName == codec
                    }

                val track = filter(playUrl)

                val quality = track?.currentQn?.let {
                    LiveQnQuality.fromCodeOrNull(it)?.toReadable() ?: "未知"
                }

                val url = track?.playUrl
                if (url == null) {
                    echo("@|red,bold [${liverName.await()}] 获取直播流失败, 未能找到与输入要求一致的直播流|@".color)
                    return@l
                }
                echo(
                    "@|yellow [${liverName.await()}] 成功获取到直播流 |@@|green,bold [${quality}]|@@|yellow : $url|@".color
                )

                val playerUrl = player?.replace("{url}", url) ?: return@l
                withContext(Dispatchers.IO) {
                    val builder = ProcessBuilder(OPEN_CMD, playerUrl)
                    builder.inheritIO()
                    val process = builder.start()
                    process.waitFor(5, TimeUnit.SECONDS)
                    val code = process.exitValue()
                    if (code == 0) {
                        echo("@|yellow [${liverName.await()}] 成功打开播放器|@".color)
                    } else {
                        echo("@|red,bold [${liverName.await()}] 打开播放器失败: $code|@".color)
                    }
                }
            }
        }.joinAll()
    }
}
