package moe.sdl.tracks.model.search

import kotlinx.serialization.Serializable
import moe.sdl.tracks.util.string.secondsToDuration
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.tracks.util.string.toStringWithUnit
import moe.sdl.yabapi.data.video.VideoInfoGetResponse
import moe.sdl.yabapi.data.video.VideoPart

@Serializable
internal data class VideoResultModel(
    val title: String,
    val bvId: String? = null,
    val cover: String? = null,
    val view: String = "--",
    val like: String = "--",
    val duration: String = "--",
    val authorName: String = "--",
    val date: Long? = null,
    val parts: List<VideoPartModel> = emptyList(),
) {
    companion object {
        val EMPTY = VideoResultModel(
            title = "解析失败(╥﹏╥), 检查输入后重试看看哟~",
        )
    }
}

internal fun VideoResultModel(response: VideoInfoGetResponse): VideoResultModel {
    val data = response.data ?: return VideoResultModel.EMPTY
    fun Int?.toShow() = toStringOrDefault { it.toStringWithUnit() }
    return VideoResultModel(
        title = data.title,
        bvId = data.bvid,
        cover = data.cover,
        view = data.stat?.view.toShow(),
        like = data.stat?.like.toShow(),
        duration = (data.durationStr?.toIntOrNull() ?: data.durationLong?.toInt())
            .toStringOrDefault { it.secondsToDuration() },
        authorName = (data.owner?.name ?: data.authorName).toStringOrDefault(),
        date = data.releaseDate ?: data.uploadDate,
        parts = data.parts.map { VideoPartModel(it) }
    )
}

@Serializable
internal data class VideoPartModel(
    val part: Int = 1,
    val title: String = "",
    val duration: String = "--",
    val cid: Int = 1,
) {
    companion object {
        val EMPTY = VideoPartModel(
            title = "解析失败(╥﹏╥), 重试看看哟~"
        )
    }
}

internal fun VideoPartModel(videoPart: VideoPart): VideoPartModel {
    return VideoPartModel(
        part = videoPart.part ?: 1,
        title = videoPart.name ?: "",
        duration = videoPart.duration?.toInt().toStringOrDefault { it.secondsToDuration() },
        cid = videoPart.cid ?: return VideoPartModel.EMPTY
    )
}
