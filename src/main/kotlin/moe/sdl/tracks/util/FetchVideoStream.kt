package moe.sdl.tracks.util

import kotlin.coroutines.CoroutineContext
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.api.fetchVideoStream
import moe.sdl.yabapi.data.stream.CodecId
import moe.sdl.yabapi.data.stream.DashTrack
import moe.sdl.yabapi.data.stream.QnQuality
import moe.sdl.yabapi.data.stream.StreamRequest
import moe.sdl.yabapi.data.stream.VideoFnvalFormat
import moe.sdl.yabapi.data.stream.VideoStreamResponse
import moe.sdl.yabapi.enums.video.VideoFormat
import moe.sdl.yabapi.util.encoding.avInt
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

suspend fun BiliClient.getVideoDashTracks(
    aid: Int,
    cid: Int,
    context: CoroutineContext = this.context,
): VideoStreamResponse = fetchVideoStream(aid, cid, StreamRequest(
    QnQuality.V8K,
    VideoFnvalFormat(
        VideoFormat.DASH,
        needHDR = true,
        need4K = true,
        need8K = true,
        needDolby = true
    )),
    context
)

suspend inline fun BiliClient.getVideoDashTracks(
    bid: String, cid: Int, context: CoroutineContext = this.context,
): VideoStreamResponse = getVideoDashTracks(bid.avInt, cid, context)

fun VideoStreamResponse.filterDashTracks(
    codec: CodecId,
    quality: QnQuality,
): DashTrack? {
    val dash = this.data?.dash ?: return run {
        logger.warn { "Try to filter dash tracks but null dash stream find." }
        null
    }
    return dash.videos.asSequence()
        .filter { it.codec == codec }
        .firstOrNull { it.id == quality }
}
