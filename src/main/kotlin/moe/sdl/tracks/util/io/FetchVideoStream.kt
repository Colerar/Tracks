package moe.sdl.tracks.util.io

import kotlin.coroutines.CoroutineContext
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.api.fetchPgcStream
import moe.sdl.yabapi.api.fetchVideoStream
import moe.sdl.yabapi.data.stream.AbstractStreamData
import moe.sdl.yabapi.data.stream.CodecId
import moe.sdl.yabapi.data.stream.DashTrack
import moe.sdl.yabapi.data.stream.PgcStreamResponse
import moe.sdl.yabapi.data.stream.QnQuality
import moe.sdl.yabapi.data.stream.StreamRequest
import moe.sdl.yabapi.data.stream.VideoFnvalFormat
import moe.sdl.yabapi.data.stream.VideoStreamResponse
import moe.sdl.yabapi.enums.video.VideoFormat
import moe.sdl.yabapi.util.encoding.bv
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger {} }

internal val highestRequest = StreamRequest(
    qnQuality = QnQuality.V8K,
    fnvalFormat = VideoFnvalFormat(
        format = VideoFormat.DASH,
        needHDR = true,
        need4K = true,
        need8K = true,
        needDolby = true
    ))

internal suspend inline fun BiliClient.fetchVideoDashTracks(
    bid: String,
    cid: Int,
    context: CoroutineContext = this.context,
): VideoStreamResponse = fetchVideoStream(bid, cid, highestRequest, context)

internal suspend inline fun BiliClient.fetchVideoDashTracks(
    aid: Int, cid: Int, context: CoroutineContext = this.context,
): VideoStreamResponse = fetchVideoDashTracks(aid.bv, cid, context)

internal suspend inline fun BiliClient.fetchPgcDashTracks(
    epId: Int,
    context: CoroutineContext = this.context,
): PgcStreamResponse = fetchPgcStream(epId, highestRequest, context)

internal fun AbstractStreamData.filterDashTracks(
    codec: CodecId,
    quality: QnQuality,
): DashTrack? {
    val dash = this.dash ?: return run {
        logger.warn { "Try to filter dash tracks but null dash stream find." }
        null
    }
    return dash.videos.asSequence()
        .filter { it.codec == codec }
        .firstOrNull { it.id == quality }
}
