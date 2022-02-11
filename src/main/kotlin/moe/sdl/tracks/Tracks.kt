package moe.sdl.tracks

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import java.nio.channels.UnresolvedAddressException
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.consts.tracksVersion
import moe.sdl.tracks.enums.DownloadType
import moe.sdl.tracks.enums.QualityStrategy
import moe.sdl.tracks.enums.videoQualityMap
import moe.sdl.tracks.model.VideoResult
import moe.sdl.tracks.model.printConsole
import moe.sdl.tracks.util.Log
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.errorExit
import moe.sdl.tracks.util.string.trimBiliNumber
import moe.sdl.yabapi.api.getBangumiDetailedByEp
import moe.sdl.yabapi.api.getBangumiDetailedBySeason
import moe.sdl.yabapi.api.getBangumiInfo
import moe.sdl.yabapi.api.getVideoInfo
import moe.sdl.yabapi.data.video.VideoInfoGetResponse
import moe.sdl.yabapi.util.encoding.bv
import org.fusesource.jansi.AnsiConsole

fun main(args: Array<String>) {
    AnsiConsole.systemInstall()
    try {
        MainCommand()
            .main(args)
    } catch (e: UnresolvedAddressException) {
        echo("@|red,bold 网络错误！可能是网络不稳定或已离线|@".color)
        echo(e)
        Log.debug(e) { "Stacktrace:" }
    }
}

class MainCommand : CliktCommand(
    name = "tracks",
    help =
    """
       Kotlin 编写的哔哩哔哩下载 Cli ${"@|yellow,bold $tracksVersion|@\n".color}
       url - 输入 B 站视频 链接|短链接|号码
    """.trimIndent(),
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true,
) {

    private val url by argument("url", "B 站视频地址或 av BV ss ep md 等号码")

    /**
     * 取流相关
     * 默认视频/音频/字幕/封面全下载
     */
    private val video by option("-v", "-video", help = "是否下载视频, 默认下载")
        .flag("-vn", "-no-video", default = true, defaultForHelp = "默认会下载视频")

    private val audio by option("-a", "-audio", help = "是否下载音频, 默认下载")
        .flag("-an", "-no-audio", default = true, defaultForHelp = "默认会下载音频")

    private val subtitle by option("-s", "-subtitle", help = "是否下载字幕, 默认下载")
        .flag("-sn", "-no-subtitle", default = true, defaultForHelp = "默认会下载字幕")

    private val cover by option("-c", "-cover", help = "是否下载封面, 默认下载")
        .flag("-cn", "-no-cover", default = true, defaultForHelp = "默认会下载封面")

    private val only by option(help = "仅下载特定类型, 该项优先级最高").switch(
        "-vo" to DownloadType.VIDEO,
        "-ao" to DownloadType.AUDIO,
        "-so" to DownloadType.SUBTITLE,
        "-co" to DownloadType.COVER,
        "-only-video" to DownloadType.VIDEO,
        "-only-audio" to DownloadType.AUDIO,
        "-only-subtitle" to DownloadType.SUBTITLE,
        "-only-cover" to DownloadType.COVER,
    )

    private val qualityStrategy by option(help = "质量匹配模式, 可选精确或临近模式, 默认向上临近").switch(
        "-qe" to QualityStrategy.EXACT,
        "-qn" to QualityStrategy.NEAR_UP,
        "-qnd" to QualityStrategy.NEAR_DOWN,
        "-quality-exact" to QualityStrategy.EXACT,
        "-quality-near" to QualityStrategy.NEAR_UP,
        "-quality-near-down" to QualityStrategy.NEAR_DOWN,
    ).default(QualityStrategy.NEAR_UP)

    private val videoQuality by option("-qv",
        "-video-quality",
        help = "视频质量, 支持 360P 到 8K, 可搭配 -quality-xxx 使用, 可用选项: [${videoQualityMap.keys.joinToString(",")}]")

    private val showAllParts by option("-pd",
        "-part-detail",
        "-show-all-parts",
        help = "显示所有分P, 默认关闭").flag(default = false)

    private val targetParts by option("-p", "-part", "-parts", help = "视频分 P, 支持范围选择, 形如 '3-5', '0' 表示全部")
        .convert { opt ->
            val partSyntaxErr by lazy {
                UsageError("""
                        分 P 解析失败! 请检查语法:
                        1. 至少指定一个分 P
                        2. 可以单独指定一个分 P 如 '12', 也可指定分 P 范围 如 '1-4' '12-5'
                        3. 多个块可使用 ',' (全|半角皆可) 连接 如 '1-12,14-17', 逗号可尾随 如 '1,2,3,'
                        4. '0' 表示全部
                        """.trimIndent(), this, context)
            }
            if (!Regex("""(\d+(-\d+)?[,，]?)+""").matches(opt))
                throw partSyntaxErr
            opt.replace('，', ',').split(',').asSequence()
                .filter { it.isNotBlank() && it.isNotEmpty() }
                .map {
                    val values = Regex("""^(\d+)(-)?(\d+)?$""").find(it)?.groupValues?.drop(1) ?: throw partSyntaxErr
                    if (values.size == 2) throw partSyntaxErr
                    val fst = values.getOrNull(0)?.toIntOrNull() ?: throw partSyntaxErr
                    val lst = values.getOrNull(2)?.toIntOrNull() ?: fst
                    (min(fst, lst)..max(fst, lst)).toList()
                }.flatten().distinct().toList()
        }

    override fun run(): Unit = runBlocking {
        val trimmed = trimBiliNumber(url) ?: errorExit { "输入有误！请检查后重试" }
        echo("获取 @|yellow,bold [$trimmed]|@ 视频信息...".color)
        val info: Any = when {
            trimmed.startsWith("av", ignoreCase = true) -> client.getVideoInfo(trimmed.bv)
            trimmed.startsWith("bv", ignoreCase = true) -> client.getVideoInfo(trimmed)
            trimmed.startsWith("ss", ignoreCase = true) ->
                client.getBangumiDetailedBySeason(seasonId = trimmed.lowercase().removePrefix("ss").toIntOrNull()
                        ?: errorExit { "ss 号输入有误！请检查后重试" })
            trimmed.startsWith("md", ignoreCase = true) ->
                client.getBangumiInfo(mediaId = trimmed.lowercase().removePrefix("md").toIntOrNull()
                        ?: errorExit { "md 号输入有误！请检查后重试" })
            trimmed.startsWith("ep", ignoreCase = true) ->
                client.getBangumiDetailedByEp(epId = trimmed.lowercase().removePrefix("ep").toIntOrNull()
                        ?: errorExit { "ep 号输入有误！请检查后重试" })
            else -> errorExit { "解析链接失败！请检查后重试" }
        }
        when (info) {
            is VideoInfoGetResponse -> processVideo(info)
            else -> errorExit { "暂不支持番剧解析！" }
        }
    }

    private fun processVideo(info: VideoInfoGetResponse) {
        val targets = targetParts ?: run {
            echo("@|yellow 未选择分 P 默认下载 P1|@".color)
            listOf(1)
        }
        val model = VideoResult(info)
        echo(model.toAnsi())
        echo()
        var parts = info.data?.parts ?: errorExit(withHelp = false) { "获取分 P 失败，可能是网络波动，请稍后再试。" }
        parts.printConsole(showAllParts)
        echo()

        // '0' means ALL, i.e., keep all origin, so here only filter for non-zero input
        if (!targets.contains(0)) {
            parts = parts.filter {
                targets.contains(it.part)
            }
        }
        echo("@|bold 已选择: ${parts.map { it.part }.joinToString()}|@".color)
//                val queryJob =
//                    parts.fold(mutableListOf<VideoStreamResponse>()) { acc, part ->
//                        if (part.cid != null) acc.add(client.fetchVideoStream(trimmed, part.cid!!))
//                        acc
//                    }
    }
}
