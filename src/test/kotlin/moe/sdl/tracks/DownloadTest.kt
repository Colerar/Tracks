package moe.sdl.tracks

import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import java.io.File
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.util.downloadFile
import moe.sdl.tracks.util.fetchPgcDashTracks
import moe.sdl.tracks.util.fetchVideoDashTracks
import moe.sdl.tracks.util.filterDashTracks
import moe.sdl.yabapi.api.getVideoParts
import moe.sdl.yabapi.data.stream.CodecId
import moe.sdl.yabapi.data.stream.QnQuality
import org.junit.jupiter.api.Test

class DownloadTest {
    @Test
    fun videoDownloadTest(): Unit = runBlocking {
//        client.loginWebQRCodeInteractive()
        val bid = "BV1wF411W7WM"
        val cid = client.getVideoParts(bid).data.first().cid!!
        val url = client.fetchVideoDashTracks(bid, cid).data!!.filterDashTracks(CodecId.AVC, QnQuality.V360P)!!.baseUrl!!
        client.client.downloadFile(url, File("./storage/$bid.m4s")) {
            headers { append(HttpHeaders.Referrer, "https://www.bilibili.com") }
        }
    }
    @Test
    fun pgcDownloadTest() : Unit = runBlocking {
        val ep = 457775
        val url = client.fetchPgcDashTracks(ep).data!!.filterDashTracks(CodecId.HEVC, QnQuality.V1080P)!!.baseUrl!!
        client.client.downloadFile(url, File("./storage/ep$ep.m4s")) {
            headers { append(HttpHeaders.Referrer, "https://www.bilibili.com") }
        }
    }
}
