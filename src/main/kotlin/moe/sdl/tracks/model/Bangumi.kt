package moe.sdl.tracks.model

import moe.sdl.tracks.config.emoji
import moe.sdl.tracks.consts.PART_SHOW_LIMIT
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.string.secondsToDuration
import moe.sdl.tracks.util.string.toRelativeTime
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.yabapi.data.bangumi.BangumiDetailed
import moe.sdl.yabapi.data.bangumi.BangumiEpisode
import moe.sdl.yabapi.data.bangumi.BangumiType
import kotlin.math.min

fun BangumiDetailed.toAnsi() = """
    @|cyan,bold =================== ${type.toShow()}信息 ===================|@
    @|bold  $title|@
    @|bold  | ${emoji("💸　")}会员要求　${if (payment == null) "免费" else payment?.vipPromotion ?: "不明"}|@
    @|bold  | ${emoji("📆　")}更新状态　${if (publish?.isFinished == true) "已完结 共" else "已更新"} ${episodes.size} 话|@
    @|bold  | ${emoji("⌚️　")}上次更新　${episodes.lastOrNull()?.releaseTime?.toRelativeTime() ?: "暂无更新"}|@
    @|bold  | ${emoji("🌟　")}评分　　　${rating?.score ?: "暂无"}|@
""".trimIndent().color

fun BangumiType.toShow(): String = when (this) {
    BangumiType.ANIME -> "番剧"
    BangumiType.MOVIE -> "电影"
    BangumiType.DOCUMENTARY -> "纪录片"
    BangumiType.GUOCHUANG -> "国创"
    BangumiType.SERIES -> "电视剧"
    BangumiType.VARIETY -> "综艺"
    else -> "未知"
}

fun BangumiEpisode.toAnsi(): String {
    val paddedPart = title.toString().padStart(3, '0')
    val duration = this.durationInSecond.toStringOrDefault { it.toInt().secondsToDuration() }
    return """$paddedPart - $longTitle [$duration]"""
}

fun List<BangumiEpisode>.printConsole(type: BangumiType) {
    println("@|bold 目标${type.toShow()}共有|@ @|yellow,bold $size|@ @|bold 集|@".color)
    val tail = this.subList(0, min(this.size, PART_SHOW_LIMIT - 1))
    val last = this.lastOrNull() ?: return

    tail.forEach {
        println("- ${it.toAnsi()}")
    }

    if (tail.lastIndex != lastIndex) {
        println(" ......")
        println("- ${last.toAnsi()}")
    }
}
