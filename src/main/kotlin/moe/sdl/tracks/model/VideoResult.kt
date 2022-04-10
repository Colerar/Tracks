package moe.sdl.tracks.model

import kotlinx.serialization.Serializable
import moe.sdl.tracks.config.emoji
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.string.secondsToDuration
import moe.sdl.tracks.util.string.toAbsTime
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.tracks.util.string.toStringWithUnit
import moe.sdl.yabapi.data.video.VideoInfoGetResponse
import org.fusesource.jansi.Ansi

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
    fun toAnsi(): Ansi {
        val viewK = emoji("▶️", "播放")
        val likeK = emoji("👍", "点赞")
        val coinK = emoji("💰", "投币")
        val favoriteK = emoji("⭐️", "收藏")
        return """
                @|cyan,bold =================== 视频信息 ===================|@
                @|bold $title|@
                @|bold | 日　期 $date |@
                @|bold | ＵＰ主 $authorName|@
                @|bold | $viewK  $view  $likeK  $like  $coinK  $coin  $favoriteK  $favorite|@
        """.trimIndent().color
    }

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
    )
}
