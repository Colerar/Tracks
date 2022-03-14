package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.client.HttpClient
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.config.tracksPreference
import moe.sdl.tracks.enums.DownloadType
import moe.sdl.tracks.enums.QualityStrategy
import moe.sdl.tracks.enums.audioQualityMap
import moe.sdl.tracks.enums.videoQualityMap
import moe.sdl.tracks.external.zhconvert.Converter
import moe.sdl.tracks.external.zhconvert.requestZhConvert
import moe.sdl.tracks.model.VideoResult
import moe.sdl.tracks.model.printConsole
import moe.sdl.tracks.model.toAnsi
import moe.sdl.tracks.model.toMetadata
import moe.sdl.tracks.model.toShow
import moe.sdl.tracks.util.Log
import moe.sdl.tracks.util.PlaceholderContext
import moe.sdl.tracks.util.basicContext
import moe.sdl.tracks.util.buildFile
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.errorExit
import moe.sdl.tracks.util.getCliPath
import moe.sdl.tracks.util.infoExit
import moe.sdl.tracks.util.io.configureForBili
import moe.sdl.tracks.util.io.downloadFile
import moe.sdl.tracks.util.io.downloadResumable
import moe.sdl.tracks.util.io.ensureCreate
import moe.sdl.tracks.util.io.fetchPgcDashTracks
import moe.sdl.tracks.util.io.fetchVideoDashTracks
import moe.sdl.tracks.util.io.getRemoteFileSize
import moe.sdl.tracks.util.io.toNormalizedAbsPath
import moe.sdl.tracks.util.placeHolderContext
import moe.sdl.tracks.util.placeHolderResult
import moe.sdl.tracks.util.string.Size
import moe.sdl.tracks.util.string.progressBar
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.tracks.util.string.trimBiliNumber
import moe.sdl.yabapi.api.getBangumiDetailedByEp
import moe.sdl.yabapi.api.getBangumiDetailedBySeason
import moe.sdl.yabapi.api.getBangumiReviewInfo
import moe.sdl.yabapi.api.getSubtitleContent
import moe.sdl.yabapi.api.getVideoInfo
import moe.sdl.yabapi.api.getVideoPlayerInfo
import moe.sdl.yabapi.data.GeneralCode
import moe.sdl.yabapi.data.bangumi.BangumiDetailedResponse
import moe.sdl.yabapi.data.stream.AbstractStreamData
import moe.sdl.yabapi.data.stream.CodecId
import moe.sdl.yabapi.data.stream.DashStream
import moe.sdl.yabapi.data.stream.DashTrack
import moe.sdl.yabapi.data.stream.QnQuality
import moe.sdl.yabapi.data.stream.VideoStreamData
import moe.sdl.yabapi.data.video.SubtitleTrack
import moe.sdl.yabapi.data.video.VideoInfoGetResponse
import moe.sdl.yabapi.data.video.encodeToSrt
import moe.sdl.yabapi.enums.ImageFormat
import moe.sdl.yabapi.util.encoding.bv
import moe.sdl.yabapi.util.string.buildImageUrl
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder
import okio.buffer
import okio.sink
import java.io.File
import kotlin.math.max
import kotlin.math.min

class Dig : CliktCommand(
    name = "dig", help = "下载命令".trimIndent(), printHelpOnEmptyArgs = true
) {

    init {
        val errorTip =
            "尝试获取 ffmpeg 路径失败! 若未下载可前往 https://www.ffmpeg.org/ 下载, 已下载的可通过 'tracks config ffmpeg=path/to/file' 指定路径"
        if (tracksPreference.programDir.ffmpeg == null) {
            val path = getCliPath("ffmpeg") ?: errorExit { errorTip }
            TermUi.echo("@|yellow 自动检测到 FFmpeg 路径:|@ $path".color)
            tracksPreference.programDir.ffmpeg = path
        }
    }

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

    private val onlyInfo by option(
        "-no-down", "-only-info", "-nd", "-oi", help = "仅输出信息, 不下载"
    ).flag()

    private val multipart by option("-multipart", "-mt", help = "下载分块数, 默认不分块")
        .int().default(1)
        .validate {
            if (it !in 1..16) throw UsageError("分块数 n 应该满足 1 ≤ n ≤ 16 ", this, context)
        }

    private val downloads: Set<DownloadType> by lazy {
        only?.let {
            buildSet {
                add(it)
            }
        } ?: buildSet {
            if (video) add(DownloadType.VIDEO)
            if (audio) add(DownloadType.AUDIO)
            if (cover) add(DownloadType.COVER)
            if (subtitle) add(DownloadType.SUBTITLE)
        }
    }

    private val qualityStrategy by option(help = "质量匹配模式, 可选精确或临近模式, 默认向上临近").switch(
        "-qe" to QualityStrategy.EXACT,
        "-qn" to QualityStrategy.NEAR_UP,
        "-qnd" to QualityStrategy.NEAR_DOWN,
        "-quality-exact" to QualityStrategy.EXACT,
        "-quality-near" to QualityStrategy.NEAR_UP,
        "-quality-near-down" to QualityStrategy.NEAR_DOWN,
    ).default(QualityStrategy.NEAR_UP)

    private val _videoQuality by option(
        "-qv", "-video-quality",
        help = "视频质量, 支持 360P 到 8K, 可搭配 -quality-xxx 使用, 可用选项: [${videoQualityMap.keys.joinToString(",")}]"
    ).convert {
        videoQualityMap[it] ?: throw UsageError(
            """
            解析失败, 未找到 '$it' 对应的画质, 请检查后重试
            可用选项: [${videoQualityMap.keys.joinToString(",")}]
            """.trimIndent()
        )
    }

    private val videoQuality by lazy {
        _videoQuality ?: run {
            echo("@|yellow 未指定分辨率, 默认选择可下载的最高画质|@".color)
            QnQuality.V8K
        }
    }

    private val availableVideoCodec by lazy {
        listOf("avc", "hevc", "av1", "h264", "h265", "h.264", "h.265").joinToString(",")
    }

    private val videoCodec by option(
        "-videocodec", "-codec", "-cv",
        help = "视频编码优先级, 默认 [avc, hevc, av1], 可用 [$availableVideoCodec]"
    ).convert { str ->
        str.split(Regex("[,，]"))
            .filterNot { it.isEmpty() || it.isBlank() }
            .map {
                when {
                    it.matches(Regex("""^\s*(hevc|h\.?265)\s*$""", RegexOption.IGNORE_CASE)) -> CodecId.HEVC
                    it.matches(Regex("""^\s*(avc|h\.?264)\s*$""", RegexOption.IGNORE_CASE)) -> CodecId.AVC
                    it.matches(Regex("""^\s*av1\s*$""", RegexOption.IGNORE_CASE)) -> CodecId.AV1
                    else -> throw UsageError("未知的视频编码 '$it', 请检查输入后重试, 可用 [$availableVideoCodec]")
                }
            }.also {
                Log.debug { "Priority of video codecs: ${it.joinToString()}" }
            }
    }.default(listOf(CodecId.AVC, CodecId.HEVC, CodecId.AV1))

    private val _audioQuality by option(
        "-qa", "-audio-quality",
        help = "音频质量, 支持 64kbps - 320kbps 以及 dolby e-ac-3, 可搭配 -quality-xxx使用, 可用选项: ${audioQualityMap.keys.joinToString()}"
    ).convert {
        audioQualityMap[it] ?: throw UsageError(
            """
            解析失败, 未找到 '$it' 对应的音质, 请检查后重试,
            可用选项 ${audioQualityMap.keys.joinToString()}
            """.trimIndent()
        )
    }

    private val audioQuality by lazy {
        _audioQuality ?: run {
            echo("@|yellow 未指定音质, 默认选择可下载的最高音质|@".color)
            QnQuality.AUDIO_DOLBY
        }
    }

    private fun extractLangCode(str: String) = str.split(Regex("[，,]"))
        .filter { it.isNotEmpty() && it.isNotBlank() }

    private val subtitleLanguages by option(
        "-sub-lang", "-sl",
        help = "要下载的字幕的语言代码(如 zh-hant, zh-hans), 默认中文, 可指定多个, all 为全部"
    ).convert { extractLangCode(it) }.default(listOf("zh-hans", "zh-hant", "中文"))

    private val subtitleLooseMode by option(
        "-sub-loose", "-sub-loose-match", "-slm",
        help = "是否开启宽松模式, 不仅将匹配语言代码, 同时也匹配语言名称, 并且仅要求匹配项包含, 而非相等, 默认开启"
    ).flag("-sub-strict", "-ss", default = true)

    private val subtitleWildMatch by option(
        "-sub-weird", "-sw",
        help = "字幕贪婪 / 回退匹配模式, 默认回退, 前者将根据指定的顺序选定, 至多选择一个; 后者将下载所有匹配的字幕"
    ).flag("-sub-fallback", "-sf", default = false)

    private val zhConvert by option(
        "-zhconvert-enable", "-ze", help = "是否使用繁化姬转换字词, 默认关闭"
    ).flag("-zhconvert-disable", "-zd", default = false)

    private val zhConvertTo by option(
        "-zhconvert-to", "-zt",
        help = "使用繁化姬转换的目标, 默认简体化, 详见: https://zhconvert.org/ , 可用 [${
        Converter.values().joinToString(",") { it.code }
        }]"
    ).convert { str ->
        Converter(str) ?: throw UsageError("转换目标 [$str] 输入错误! 可用选项: [${Converter.values().joinToString { it.code }}]")
    }.default(Converter.SIMPLIFIED)

    private val zhConvertKeepOrigin by option(
        "-zhconvert-keep-origin", "-zhconvert-keep", "-zk",
        help = "是否保留转换前文本, 默认开启"
    ).flag("-zhconvert-only-artifact", "-zhconvert-clean", "-zoa", "-zc", default = true)

    private val onlyArtifact by option("-clean-up", "-only-artifact", "-oa", help = "是否只保留混流后的成品, 默认开启")
        .flag("-keep-material", "-km", default = true)

    private val skipMux by option("-skip-mux", "-sm", help = "是否跳过混流")
        .flag("-mux", "-m", default = false)

    private val showAllParts by option(
        "-pd", "-part-detail", "-show-all-parts",
        help = "显示所有分P, 默认关闭"
    ).flag(default = false)

    private val targetParts by option("-p", "-part", "-parts", help = "视频分 P, 支持范围选择, 形如 '3-5', '0' 表示全部")
        .convert { opt ->
            val partSyntaxErr by lazy {
                UsageError(
                    """
                        分 P 解析失败! 请检查语法:
                        1. 至少指定一个分 P
                        2. 可以单独指定一个分 P 如 '12', 也可指定分 P 范围 如 '1-4' '12-5'
                        3. 多个块可使用 ',' (全|半角皆可) 连接 如 '1-12,14-17', 逗号可尾随 如 '1,2,3,'
                        4. '0' 表示全部
                    """.trimIndent(),
                    this, context
                )
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
        var trimmed = trimBiliNumber(url) ?: errorExit { "输入有误！请检查后重试" }
        echo("获取 @|yellow,bold [$trimmed]|@ 视频信息...".color)
        if (trimmed.startsWith("md", ignoreCase = true)) {
            val info = client.getBangumiReviewInfo(
                mediaId = trimmed.lowercase().removePrefix("md").toIntOrNull()
                    ?: errorExit { "md 号输入有误！请检查后重试" }
            )
            trimmed = "ss" + info.result?.media?.seasonId
        }
        val info: Any = when {
            trimmed.startsWith("av", ignoreCase = true) -> client.getVideoInfo(trimmed.bv)
            trimmed.startsWith("bv", ignoreCase = true) -> client.getVideoInfo(trimmed)
            trimmed.startsWith("ss", ignoreCase = true) ->
                client.getBangumiDetailedBySeason(
                    seasonId = trimmed.lowercase().removePrefix("ss").toIntOrNull()
                        ?: errorExit { "ss 号输入有误！请检查后重试" }
                )
            trimmed.startsWith("ep", ignoreCase = true) ->
                client.getBangumiDetailedByEp(
                    epId = trimmed.lowercase().removePrefix("ep").toIntOrNull()
                        ?: errorExit { "ep 号输入有误！请检查后重试" }
                )
            else -> errorExit { "解析链接失败！请检查后重试" }
        }
        when (info) {
            is VideoInfoGetResponse -> processVideo(info, this)
            is BangumiDetailedResponse -> processBangumi(info, trimmed, this)
            else -> infoExit { "暂不支持该类解析！" }
        }
    }

    private suspend fun processVideo(info: VideoInfoGetResponse, scope: CoroutineScope) {
        val targets by lazy {
            targetParts ?: listOf(1)
        }
        val model = VideoResult(info)
        echo(model.toAnsi())
        echo()
        var parts = info.data?.parts ?: errorExit(withHelp = false) { "获取分 P 失败, 可能是网络波动, 请稍后再试。" }
        parts.printConsole(showAllParts)
        if (onlyInfo) return
        echo()

        // '0' means ALL, i.e., keep all origin, so here only filter for non-zero input
        if (!targets.contains(0)) {
            parts = parts.filter {
                targets.contains(it.part)
            }
        }
        echo("已选择: @|bold ${parts.joinToString { it.part.toString() }}|@".color)

        // download cover
        if (downloads.contains(DownloadType.COVER)) {
            val context = basicContext + info.data!!.placeHolderContext
            val dst = context.buildFile(tracksPreference.fileDir.coverName)
            downloadCover(info.data!!.cover, dst)
        }

        parts.forEachIndexed { idx, part ->
            if (idx >= 1) repeat(2) { echo() }

            echo("@|bold 下载分 P: |@".color.toString() + part.toAnsi().toString())
            val response = client.fetchVideoDashTracks(
                aid = info.data?.aid ?: errorExit { "获取 aid 失败, 可能是网络错误, 稍后重试看看" },
                cid = part.cid ?: errorExit { "cid 获取失败, 稍后重试看看" }
            )
            if (response.code != GeneralCode.SUCCESS) errorExit { "取流失败 ${response.code} - ${response.message}" }
            val data = response.data ?: errorExit { "取流 [response.data] 失败, 稍后重试看看" }

            downloadAndMux(
                data, info.data!!.aid, part.cid!!, scope,
                placeholderContext = basicContext + part.placeHolderContext + info.data!!.placeHolderContext,
                keys = setOf(part.cid.toString())
            )
        }
    }

    private suspend fun processBangumi(info: BangumiDetailedResponse, id: String, scope: CoroutineScope) {
        if (info.data == null) errorExit(withHelp = false) { "获取 PGC 信息失败, 可能是地区限制或网络波动: ${info.code} - ${info.message}" }
        echo(info.data?.toAnsi())
        val isEpisode = id.startsWith("ep")
        val isSeason = id.startsWith("ss")
        val numId = id.drop(2).toIntOrNull() ?: errorExit(withHelp = false) { "EP 或 SS 号解析失败, id: $id" }
        val bangumiContext = basicContext + info.data!!.placeHolderResult
        val dst = bangumiContext.buildFile(tracksPreference.fileDir.coverName)
        var episodes = info.data?.episodes ?: infoExit { "无可用集数, 退出下载" }
        echo()
        episodes.printConsole(info.data!!.type, showAllParts)
        if (onlyInfo) return
        echo()

        downloadCover(info.data!!.cover, dst)
        episodes = when {
            !targetParts.isNullOrEmpty() && targetParts!!.contains(0) -> episodes
            targetParts.isNullOrEmpty() && isEpisode ->
                listOf(episodes.firstOrNull { it.id == numId } ?: infoExit { "无法找到 ep$numId, 退出下载" })
            targetParts.isNullOrEmpty() && isSeason -> {
                echo("@|yellow 未选择集数, 默认下载 P1. |@".color)
                listOf(episodes.first())
            }
            !targetParts.isNullOrEmpty() && (isSeason || isEpisode) -> {
                if (targetParts!!.contains(0)) episodes else
                    episodes.filterIndexed { index, _ ->
                        targetParts!!.contains(index + 1)
                    }
            }
            else -> errorExit(withHelp = false) { "集数选择遇到了意料外的情况, 可能是无可用集数" }
        }

        echo("@|bold 已选择:|@ ${episodes.map { it.title }}".color)

        episodes.forEachIndexed { index, episode ->
            if (index >= 1) repeat(2) { echo() }
            echo("@|bold 下载${info.data!!.type.toShow()}单集:|@ ${episode.toAnsi()}".color)
            val result = client.fetchPgcDashTracks(
                episode.id ?: run {
                    echo("@|yellow 无法获取本集 epid, 跳过下载|@".color)
                    return@forEachIndexed
                }
            ).data ?: run {
                echo("@|red 获取 ep$numId 视频流失败, 跳过下载|@".color)
                return@forEachIndexed
            }
            downloadAndMux(
                result,
                episode.aid ?: errorExit { "获取番剧 aid 失败, 停止下载" },
                episode.cid ?: errorExit { "获取番剧 cid 失败, 停止下载" },
                scope,
                placeholderContext = bangumiContext + episode.placeHolderContext,
                keys = setOf(episode.id.toString(), info.data?.seasonId.toString())
            )
        }
    }

    /**
     * @param final final artifact, will be automatically rename if having same name file
     */
    private suspend fun muxStream(audioDst: File?, videoDst: File?, chapterDst: File?, final: File, scope: CoroutineScope) {
        val artifact = if (final.exists()) {
            final.duplicateRename().also {
                echo("@|yellow 检测到文件重复, 更名为:|@ ${it.name}".color)
            }
        } else final
        val ffmpegDir = tracksPreference.programDir.ffmpeg
        val ffmpeg = FFmpeg(ffmpegDir ?: errorExit { "FFmpeg 未配置! 通过 'tracks config ffmpeg=/path/to/file' 配置" })
        if (!ffmpeg.isFFmpeg) errorExit { "路径错误, 非 FFmpeg 路径!" }
        val builder = ffmpeg.builder()
            .apply {
                chapterDst?.let {
                    if (chapterDst.exists()) {
                        addInput(it.toNormalizedAbsPath())
                    }
                }
                videoDst?.let { addInput(it.toNormalizedAbsPath()) }
                audioDst?.let { addInput(it.toNormalizedAbsPath()) }
            }
            .addOutput(
                FFmpegOutputBuilder()
                    .apply {
                        if (chapterDst?.exists() == true) {
                            addExtraArgs("-map_metadata", "0")
                        }
                    }
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
                    .setFormat("mp4")
                    .setFilename(artifact.toNormalizedAbsPath())
            )
        val job = scope.launch {
            FFmpegExecutor(ffmpeg)
                .createJob(builder).run()
        }
        echo("混流中...")
        job.join()
        job.invokeOnCompletion {
            echo("@|yellow 混流完毕! |@ 文件存放于: ${artifact.toNormalizedAbsPath()}".color)
            if (onlyArtifact) {
                echo("清除中间文件...")
                audioDst?.apply { if (exists()) delete() }
                videoDst?.apply { if (exists()) delete() }
                chapterDst?.apply { if (exists()) delete() }
            }
        }
    }

    private fun File.duplicateRename(): File = if (exists()) {
        val name = nameWithoutExtension.let {
            val regex = Regex("""^(.+)\((\d+)\)$""")
            val matchEntire = regex.matchEntire(it)
            if (matchEntire != null) {
                val (fst, snd) = matchEntire.destructured
                "$fst(${snd.toInt() + 1})"
            } else "$it(1)"
        }
        File(parent, "$name.$extension").duplicateRename()
    } else this

    private fun filterVideo(data: AbstractStreamData): DashTrack? {
        if (data.dash?.videos == null) errorExit { "获取 Dash 视频流 [data.dash.videos] 失败, 稍后重试看看" }
        Log.debug { "当前视频可用画质: [${data.acceptDescription.joinToString()}]" }
        val videos = data.dash!!.videos.asSequence()

        // quality filter
        val qualityList = videos.mapNotNull { it.id }
        val filtered = qualityList.filterByOpt(videoQuality) ?: run {
            if (data is VideoStreamData) {
                if (videoQuality in data.acceptQuality) infoExit { "@|red,bold 匹配画质 [$videoQuality] 需要大会员, 停止下载|@".color }
            }
            infoExit { "@|red 无匹配画质 [$videoQuality], 停止下载|@".color }
        }
        if (data is VideoStreamData) {
            if (videoQuality in data.acceptQuality && filtered != videoQuality)
                echo("@|yellow 匹配画质 [$videoQuality] 需要大会员, 回退为 [$filtered]|@".color)
        }

        // codec filter
        val track = videos.filter { it.id == filtered }.firstOrNull {
            videoCodec.forEach { target ->
                if (it.codec == target) return@firstOrNull true
            }
            false
        }

        return track
    }

    private fun filterAudio(dash: DashStream): DashTrack? {
//        if (dash?.audios == null) errorExit { "获取 Dash 音频流失败, 稍后重试看看" }
        val audios = buildList {
            addAll(dash.audios)
            dash.dolby?.audio?.forEach { add(it) }
        }
        val target = audios.mapNotNull { it.id }.asSequence().filterByOpt(audioQuality)
        return audios.firstOrNull { it.id == target }
    }

    private fun Sequence<QnQuality>.filterByOpt(opt: QnQuality) = filter {
        when (qualityStrategy) {
            QualityStrategy.EXACT -> it == opt
            QualityStrategy.NEAR_UP -> it >= opt
            QualityStrategy.NEAR_DOWN -> it <= opt
        }
    }.let {
        when (qualityStrategy) {
            QualityStrategy.EXACT -> it.firstOrNull()
            QualityStrategy.NEAR_UP -> it.minOrNull() ?: this.maxOrNull()
            QualityStrategy.NEAR_DOWN -> it.maxOrNull() ?: this.maxOrNull()
        }
    }

    /**
     * @param url will be converted to PNG format request using [buildImageUrl]
     * @param onDuplicate If [dst] are not file downloaded before, and exists, aka, name shadowed,
     * this func will be invoked. Func should return [Boolean] to control flow, `true` for *replace the origin file*;
     * `false` for return func.
     */
    private suspend fun downloadCover(
        url: String?,
        dst: File,
        onDuplicate: () -> Boolean = {
            echo("@|yellow 封面已存在, 跳过下载|@".color)
            echo()
            false
        }
    ) {
        if (url == null) {
            echo("@|yellow 获取封面链接失败, 跳过封面下载...|@".color)
            return
        }
        echo("@|magenta ==>|@ @|bold 下载封面...|@".color)
        if (dst.exists()) {
            if (!onDuplicate()) return
            dst.delete()
        }
        client.client.downloadFile(
            url = buildImageUrl(url, ImageFormat.PNG),
            dst = dst,
            getBuilder = { configureForBili() }
        )
        echo("@|yellow 封面下载成功!|@ 文件: ${dst.toPath().normalize().toFile().absolutePath}".color)
        echo()
    }

    private suspend fun downloadAndMux(
        data: AbstractStreamData,
        aid: Int,
        cid: Int,
        scope: CoroutineScope,
        placeholderContext: PlaceholderContext,
        keys: Set<String>,
    ) {
        // for mux
        var videoDst: File? = null
        var audioDst: File? = null
        val chapterDst: File by lazy {
            File(".", "$aid-$cid.metadata.txt")
        }
        val duration: Long? = data.dash?.duration?.toLong()

        // placeholder context for file name
        var videoStreamContext: PlaceholderContext? = null
        var audioStreamContext: PlaceholderContext? = null

        suspend fun downloadStream(dst: File, track: DashTrack, url: String, size: Size) {
            val filesRef = atomic<List<File>>(emptyList())
            downloadStreamAndShow(dst, size, scope, filesRef) {
                client.client.downloadStream(
                    url, dst, partCount = multipart.toLong(), scope = scope,
                    filesRef, setOf(track.id.toString(), track.codec.toString(), size.bytes.toString()) + keys,
                )
            }
        }

        val playerInfo by lazy {
            scope.async {
                client.getVideoPlayerInfo(aid, cid)
            }
        }

        run subtitle@{
            if (DownloadType.SUBTITLE in downloads) {
                echo()
                echo("@|magenta ==>|@ @|bold 下载字幕中...|@".color)
                val subtitles = playerInfo.await().data?.subtitle?.list
                if (subtitles.isNullOrEmpty()) {
                    echo("@|yellow 无可用字幕, 跳过下载...|@".color)
                    return@subtitle
                }
                val langs = subtitles.joinToString { "${it.languageName}[${it.language}]" }
                echo("可用字幕: $langs")

                fun String?.contain(other: String) = this?.contains(other, true) == true

                val tracks =
                    if (subtitleLanguages.firstOrNull { it.equals("all", true) } != null) {
                        subtitles
                    } else {
                        subtitleLanguages.fold(mutableListOf<SubtitleTrack>()) { acc, str ->
                            subtitles.filter { tr ->
                                when {
                                    subtitleLooseMode && (tr.languageName.contain(str) || tr.language.contain(str)) -> true
                                    tr.language?.equals(str, true) == true -> true
                                    else -> false
                                }
                            }.let { acc.addAll(it) }
                            acc
                        }.let { list ->
                            if (!subtitleWildMatch) {
                                list.firstOrNull()?.let { listOf(it) } ?: emptyList()
                            } else list
                        }
                    }

                if (tracks.isEmpty()) echo("@|yellow 无匹配字幕, 跳过下载 |@".color)
                tracks.forEachIndexed { idx, it ->
                    if (idx >= 1) echo()
                    echo("@|bold 正在下载：|@ ${it.languageName}[${it.language}]".color)
                    val srt = client.getSubtitleContent(
                        it.subtitleUrl ?: run {
                            echo("@|red 无法获取当前字幕地址, 跳过下载|@".color)
                            return@forEachIndexed
                        }
                    ).body.encodeToSrt()
                    val fileDst by lazy {
                        with(placeholderContext + it.placeHolderContext) {
                            buildFile(tracksPreference.fileDir.subtitleName)
                        }.duplicateRename()
                    }

                    suspend fun File.writeTextWithBuff(text: String) {
                        ensureCreate()
                        sink().use { sink ->
                            sink.buffer().writeString(text, Charsets.UTF_8)
                        }
                    }

                    if (!zhConvert || (zhConvert && zhConvertKeepOrigin)) {
                        fileDst.writeTextWithBuff(srt)
                        echo("@|yellow 下载完成! 文件保存至:|@ ${fileDst.toNormalizedAbsPath()}".color)
                    }
                    if (zhConvert) {
                        echo("使用繁化姬将文本转换至 @|yellow,bold [${zhConvertTo.languageName}]|@...".color)
                        if (tracksPreference.zhConvertAlert) {
                            confirm(
                                """
                                @|bold 注意:|@
                                @|bold 使用繁化姬转换服务即说明您业已同意其服务条款:|@ https://docs.zhconvert.org/license/
                                @|bold 若需商业使用应按照其要求支付费用:|@ https://docs.zhconvert.org/commercial/
                                @|bold 是否同意? |@
                                """.trimIndent().color.toString(),
                                default = false
                            ).also {
                                if (it != true) {
                                    echo("@|yellow 跳过转换... |@")
                                    if (!zhConvertKeepOrigin) {
                                        fileDst.writeTextWithBuff(srt)
                                        echo("@|yellow 将原始文本保存至:|@ ${fileDst.toNormalizedAbsPath()}")
                                    }
                                }
                                if (it == true) tracksPreference.zhConvertAlert = false
                            }
                        }

                        val converted = client.client.requestZhConvert(srt, zhConvertTo)
                        converted.onFailed {
                            echo("转换失败! $it - ${converted.msg}")
                        }.onSuccess {
                            val convDst =
                                with(placeholderContext + converted.data.converter.placeHolderContext) {
                                    buildFile(tracksPreference.fileDir.subtitleName)
                                }.duplicateRename()

                            convDst.writeTextWithBuff(converted.data.text)
                            echo("转换成功, 保存至: ${convDst.toNormalizedAbsPath()}")
                        }
                    }
                }
                echo()
            }
        }

        // Filter and download video
        run video@{
            if (DownloadType.VIDEO in downloads) {
                val tr = filterVideo(data) ?: run {
                    echo("@|red 未发现视频流, 跳过视频下载 |@".color)
                    return@video
                }
                val trackUrl = tr.baseUrl ?: errorExit { "获取视频链接失败..." }
                val size = client.client.getRemoteFileSize(trackUrl) { configureForBili() }
                    .let { Size(it) }
                val bitrate = size.toStringOrDefault {
                    it.toBandwidth(duration ?: return@toStringOrDefault "--").toShow()
                }
                echo(
                    """
                        @|magenta ==>|@ @|bold 视频流信息: 
                         -> 画质 ${tr.id} | 编码 ${tr.codec} | 帧率 ${tr.frameRate} F
                         -> 比特率 $bitrate | 大小 ${size.toShow()} |@
                    """.trimIndent().color
                )
                videoStreamContext = tr.placeHolderContext
                val context = placeholderContext + videoStreamContext
                videoDst = context.buildFile(tracksPreference.fileDir.videoName)

                downloadStream(videoDst!!, tr, trackUrl, size)
                echo()
                val viewPoints = playerInfo.await().data?.viewPoints ?: return@video
                if (viewPoints.isNotEmpty()) {
                    chapterDst.ensureCreate()
                    chapterDst.writeText(viewPoints.toMetadata())
                }
            }
        }

        // Filter and download audio
        run audio@{
            if (DownloadType.AUDIO in downloads) {
                fun failedTip() = echo("@|red 未发现音频流, 跳过音频下载 |@".color)
                val tr = filterAudio(data.dash ?: run { failedTip(); return@audio })
                    ?: run { failedTip(); return@audio }
                val trackUrl = tr.baseUrl ?: errorExit { "获取音频链接失败" }
                val size = client.client.getRemoteFileSize(trackUrl) { configureForBili() }
                    .let { Size(it) }
                val bitrate = size.toStringOrDefault {
                    it.toBandwidth(duration ?: return@toStringOrDefault "--").toShow()
                }
                echo(
                    """
                        @|magenta ==>|@ @|bold 音频流信息: 
                         -> 比特率 $bitrate | 大小 ${size.toShow()} |@
                    """.trimIndent().color
                )
                audioStreamContext = tr.placeHolderContext
                val context = placeholderContext + audioStreamContext
                audioDst = context.buildFile(tracksPreference.fileDir.audioName)

                downloadStream(audioDst!!, tr, trackUrl, size)
                echo()
            }
        }

        // Mux video and audio
        when {
            skipMux -> echo("@|cyan,bold 根据选项跳过混流...|@".color)
            videoDst?.exists() == true || audioDst?.exists() == true -> {
                echo("@|magenta ==>|@ @|bold 开始混流...|@".color)
                val context = placeholderContext + videoStreamContext + audioStreamContext
                val final = context.buildFile(tracksPreference.fileDir.finalArtifact)

                muxStream(audioDst, videoDst, chapterDst, final, scope)
            }
            else -> echo("@|cyan,bold 无视频或音频, 跳过混流...|@".color)
        }
    }

    private suspend fun HttpClient.downloadStream(
        url: String,
        dst: File,
        partCount: Long,
        scope: CoroutineScope,
        filesRef: AtomicRef<List<File>>? = null,
        key: Set<String>,
    ) = scope.launch {
        downloadResumable(
            url,
            dst,
            filesRef = filesRef,
            onDuplicate = {
                (
                    prompt(text = "目标文件已经存在, 要覆盖吗? (y|n)", default = "n") { str ->
                        when {
                            str.matches(Regex("""y(es)?""", RegexOption.IGNORE_CASE)) -> true
                            else -> false
                        }
                    } ?: false
                    ).also {
                    echo("选择了 @|yellow,bold ${if (it) "覆盖" else "不覆盖"}|@".color)
                    if (it && dst.exists()) {
                        Log.debug { "Deleting file at ${dst.absolutePath}" }
                        dst.delete()
                    }
                    if (!it) {
                        echo("@|yellow 跳过下载|@".color)
                        cancel("Exit for file duplicate")
                    }
                }
            },
            headBuilder = { configureForBili() },
            getBuilder = { configureForBili() },
            partCount = partCount,
            coroutineScope = scope,
            key = key
        )
    }

    private suspend fun downloadStreamAndShow(
        dst: File,
        size: Size,
        scope: CoroutineScope,
        filesRef: AtomicRef<List<File>>,
        downloadJob: suspend () -> Job,
    ) = coroutineScope {
        launch {
            val cur = atomic(0L)

            runCatching {
                val downJob = downloadJob()
                val countJob = scope.launch {
                    while (isActive) {
                        cur.getAndUpdate { _ ->
                            max(
                                filesRef.value.filter {
                                    it.exists()
                                }.fold(0L) { acc, file ->
                                    acc + file.length()
                                },
                                dst.length()
                            )
                        }
                        delay(100)
                    }
                }
                val printJob = scope.progressBar(cur, size.bytes)
                printJob.invokeOnCompletion { countJob.cancel() }
                joinAll(downJob)
                delay(300)
                printJob.cancel()
            }.onSuccess {
                echo()
                echo("下载完成! 文件路径: ${dst.toPath().normalize().toFile().absolutePath}")
            }.onFailure {
                if (it is CancellationException) throw it else echo(it)
            }
        }
    }
}
