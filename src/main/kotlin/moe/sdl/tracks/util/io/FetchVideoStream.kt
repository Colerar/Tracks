package moe.sdl.tracks.util.io

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import moe.sdl.tracks.consts.BILI_WWW
import moe.sdl.tracks.util.Log
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
import kotlin.coroutines.CoroutineContext

val highestRequest = StreamRequest(
    qnQuality = QnQuality.V8K,
    fnvalFormat = VideoFnvalFormat(
        format = VideoFormat.DASH,
        needHDR = true,
        need4K = true,
        need8K = true,
        needDolby = true
    )
)

suspend inline fun BiliClient.fetchVideoDashTracks(
    bid: String,
    cid: Long,
    context: CoroutineContext = this.context,
): VideoStreamResponse = fetchVideoStream(bid, cid, highestRequest, context)

suspend inline fun BiliClient.fetchVideoDashTracks(
    aid: Long,
    cid: Long,
    context: CoroutineContext = this.context,
): VideoStreamResponse = fetchVideoDashTracks(aid.bv, cid, context)

suspend inline fun BiliClient.fetchPgcDashTracks(
    epId: Long,
    context: CoroutineContext = this.context,
): PgcStreamResponse = fetchPgcStream(epId, highestRequest, context)

fun AbstractStreamData.filterDashTracks(
    codec: CodecId,
    quality: QnQuality,
): DashTrack? {
    val dash = this.dash ?: return run {
        Log.debug { "Try to filter dash tracks but null dash stream find." }
        null
    }
    return dash.videos.asSequence()
        .filter { it.codec == codec }
        .firstOrNull { it.id == quality }
}

fun HttpRequestBuilder.configureForBili() {
    headers {
        append(HttpHeaders.Referrer, BILI_WWW)
    }
}
