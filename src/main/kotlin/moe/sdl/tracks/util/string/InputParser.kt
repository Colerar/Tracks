package moe.sdl.tracks.util.string

import io.ktor.client.features.RedirectResponseException
import io.ktor.client.request.get
import moe.sdl.tracks.config.client
import moe.sdl.tracks.util.color

private val pureNumberRegex by lazy { Regex("""^([aA][vV]\d+|[bB][vV]\w+|[eE][pP]\d+|[mM][dD]\d+|[sS]{2}\d+)$""") }
private val shortLinkRegex by lazy { Regex("""^(https?://)?(www\.)?b23\.tv/(\w+)$""") }
private val bvAvUrlRegex by lazy { Regex("""^(https?://)?(www\.)?bilibili\.com/video/([bB][vV]\w+|[aA][vV]\d+)""") }
private val mdUrlRegex by lazy { Regex("""^(https?://)?(www\.)?bilibili\.com/bangumi/media/([mM][dD]\d+)""") }
private val epUrlRegex by lazy { Regex("""^(https?://)?(www\.)?bilibili\.com/bangumi/play/([eE][pP]\d+|[sS]{2}\d+)""") }

/**
 * 将链接或短链接返回为纯号码
 * @return 在成功时会返回, 否则为空
 */
suspend fun trimBiliNumber(input: String): String? {
    var s = input.filterNot { it.isWhitespace() }
    if (s.matches(pureNumberRegex)) return s
    if (shortLinkRegex.matches(s)) {
        println("解析短链接 @|yellow,bold [$input]|@".color)
        try {
            client.client.config { followRedirects = false }.get<String>(s)
        } catch (e: RedirectResponseException) {
            s = e.response.headers["Location"] ?: run {
                println("@|red 解析短链接失败~输入长链接或重试看看哦~|@".color)
                return null
            }
        }
    }
    listOf(bvAvUrlRegex, mdUrlRegex, epUrlRegex).forEach { regex ->
        regex.find(s)?.groupValues?.getOrNull(3)?.let { return it }
    }
    return null
}

private val comma = Regex("[,，]")
fun String.splitToList() = split(comma).filterNot { it.isBlank() }
