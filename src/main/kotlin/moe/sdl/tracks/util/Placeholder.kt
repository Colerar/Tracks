package moe.sdl.tracks.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import moe.sdl.tracks.external.zhconvert.Converter
import moe.sdl.tracks.model.toShow
import moe.sdl.tracks.util.string.secondsToDuration
import moe.sdl.tracks.util.string.toAbsTime
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.yabapi.data.bangumi.BangumiDetailed
import moe.sdl.yabapi.data.bangumi.BangumiEpisode
import moe.sdl.yabapi.data.stream.DashTrack
import moe.sdl.yabapi.data.video.SubtitleTrack
import moe.sdl.yabapi.data.video.VideoInfo
import moe.sdl.yabapi.data.video.VideoPart
import java.io.File

data class PlaceholderContext(
    val map: Map<String, () -> Any?>,
) {
    fun String.decodePlaceholder(): String {
        var str = this
        map.forEach { (t, u) ->
            str = str.replace("%$t%", u().toString())
        }
        return str
    }

    operator fun plus(other: PlaceholderContext?): PlaceholderContext =
        PlaceholderContext(this.map + other?.map.orEmpty())
}

@Suppress("NOTHING_TO_INLINE")
inline fun PlaceholderContext(vararg keyToFunc: Pair<String, () -> Any?>): PlaceholderContext =
    PlaceholderContext(keyToFunc.toMap())

fun PlaceholderContext.buildFile(pattern: String, parent: String = "."): File =
    File(
        parent,
        pattern.decodePlaceholder()
            .replace(Regex("""^\s+"""), "")
            .replace(Regex("""[?*:"<>/|\\]"""), "-")
    )

private fun ldtDefault() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

val basicContext by lazy { timeContext }

val timeContext by lazy {
    PlaceholderContext(
        "year" to { ldtDefault().year },
        "month" to { ldtDefault().month.toString().padStart(2, '0') },
        "day" to { ldtDefault().dayOfMonth.toString().padStart(2, '0') },
        "hour" to { ldtDefault().hour.toString().padStart(2, '0') },
        "minute" to { ldtDefault().minute.toString().padStart(2, '0') },
        "second" to { ldtDefault().second.toString().padStart(2, '0') },
        "date" to { ldtDefault().date.toString() },
        "timedate" to { ldtDefault().toAbsTime() }
    )
}

val VideoPart.placeHolderContext: PlaceholderContext
    get() = PlaceholderContext(
        "part:num" to { part },
        "part:title" to { name },
        "part:duration" to { duration?.toInt()?.secondsToDuration() ?: "-" },
        "part:sec-duration" to { duration ?: 0 },
        "part:cid" to { cid },
    )

val BangumiEpisode.placeHolderContext
    get() = PlaceholderContext(
        "part:num" to { title },
        "part:title" to { longTitle },
        "part:duration" to { duration?.toInt()?.secondsToDuration() ?: "-" },
        "part:sec-duration" to { duration ?: 0 },
        "part:cid" to { cid }
    )

val VideoInfo.placeHolderContext: PlaceholderContext
    get() = PlaceholderContext(
        "video:id" to { bvid },
        "video:type" to { videoType?.name ?: "未知" },
        "video:duration" to { (durationLong?.toInt() ?: durationStr?.toInt())?.secondsToDuration() ?: "-" },
        "video:title" to { title },
        "video:author" to { authorName },
        "video:date" to { (releaseDate ?: uploadDate).toStringOrDefault { it.toAbsTime() } },
    )

val BangumiDetailed.placeHolderResult: PlaceholderContext
    get() = PlaceholderContext(
        "video:id" to { "ss$seasonId" },
        "video:type" to { type.toShow() },
        "video:duration" to {
            if (episodes.isEmpty()) {
                "--"
            } else episodes.mapNotNull { it.duration }.reduce(Long::plus)
        },
        "video:title" to { title },
        "video:author" to { owner?.username ?: "-" },
        "video:date" to { this.publish?.releaseDate ?: this.publish?.releaseTime ?: "-" }
    )

val DashTrack.placeHolderContext: PlaceholderContext
    get() = PlaceholderContext(
        "track:codec" to { codec },
        "track:quality" to { id },
        "track:framerate" to { frameRate }
    )

val SubtitleTrack.placeHolderContext: PlaceholderContext
    get() = PlaceholderContext(
        "subtitle:lang" to { language },
        "subtitle:langName" to { languageName },
    )

val Converter.placeHolderContext: PlaceholderContext
    get() = PlaceholderContext(
        "subtitle:lang" to { languageCode },
        "subtitle:langName" to { languageName },
    )
