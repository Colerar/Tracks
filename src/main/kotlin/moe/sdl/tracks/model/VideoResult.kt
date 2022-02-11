package moe.sdl.tracks.model

import kotlinx.serialization.Serializable
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.string.secondsToDuration
import moe.sdl.tracks.util.string.toAbsTime
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.tracks.util.string.toStringWithUnit
import moe.sdl.yabapi.data.video.VideoInfoGetResponse

@Serializable
internal data class VideoResult(
    val title: String,
    val bvId: String? = null,
    val cover: String? = null,
    val view: String = "--",
    val like: String = "--",
    val coin: String = "--",
    val favorite: String = "--",
    val duration: String = "--",
    val authorName: String = "--",
    val date: String = "--",
//    val parts: List<VideoPartModel> = emptyList(),
) {
    fun toAnsi() = """
            @|cyan,bold =================== 当前下载 ===================|@
            @|bold $title|@
            @|bold | 日　期 $date |@
            @|bold | ＵＰ主 $authorName|@
            @|bold | ▶️  $view  👍  $like  💰  $coin  ⭐  $favorite|@
            """.trimIndent().color

    companion object {
        val EMPTY = VideoResult(
            title = "解析失败(╥﹏╥), 检查输入后重试看看哟~",
        )
    }
}

internal fun VideoResult(response: VideoInfoGetResponse): VideoResult {
    val data = response.data ?: return VideoResult.EMPTY
    fun Int?.toShow() = toStringOrDefault { it.toStringWithUnit() }
    return VideoResult(
        title = data.title,
        bvId = data.bvid,
        cover = data.cover,
        view = data.stat?.view.toShow(),
        like = data.stat?.like.toShow(),
        coin = data.stat?.coin.toShow(),
        favorite = data.stat?.collect.toShow(),
        duration = (data.durationStr?.toIntOrNull() ?: data.durationLong?.toInt())
            .toStringOrDefault { it.secondsToDuration() },
        authorName = (data.owner?.name ?: data.authorName).toStringOrDefault(),
        date = (data.releaseDate ?: data.uploadDate).toStringOrDefault { it.toAbsTime() },
//        parts = data.parts.map { VideoPartModel(it) }
    )
}

//@Serializable
//internal data class VideoPartModel(
//    val part: Int = 1,
//    val title: String = "",
//    val duration: String = "--",
//    val cid: Int = 1,
//) {
//    companion object {
//        val EMPTY = VideoPartModel(
//            title = "解析失败(╥﹏╥), 重试看看哟~"
//        )
//    }
//}
//
//internal fun VideoPartModel(videoPart: VideoPart): VideoPartModel {
//    return VideoPartModel(
//        part = videoPart.part ?: 1,
//        title = videoPart.name ?: "",
//        duration = videoPart.duration?.toInt().toStringOrDefault { it.secondsToDuration() },
//        cid = videoPart.cid ?: return VideoPartModel.EMPTY
//    )
//}
