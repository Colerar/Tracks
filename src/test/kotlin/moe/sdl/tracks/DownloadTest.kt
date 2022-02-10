package moe.sdl.tracks

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import java.io.File
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.util.io.downloadFile
import moe.sdl.tracks.util.io.downloadResumable
import moe.sdl.tracks.util.io.ensureCreate
import moe.sdl.tracks.util.io.fetchPgcDashTracks
import moe.sdl.tracks.util.io.fetchVideoDashTracks
import moe.sdl.tracks.util.io.filterDashTracks
import moe.sdl.yabapi.api.getVideoParts
import moe.sdl.yabapi.data.stream.CodecId
import moe.sdl.yabapi.data.stream.QnQuality
import mu.KotlinLogging
import org.junit.jupiter.api.Test

private val logger by lazy { KotlinLogging.logger {} }

class DownloadTest {

    private inline fun HttpRequestBuilder.configureForBili(extra: HttpRequestBuilder.() -> Unit = {}) {
        this.extra()
        headers { append(HttpHeaders.Referrer, "https://www.bilibili.com") }
    }

    @Test
    fun videoDownloadTest(): Unit = runBlocking {
//        client.loginWebQRCodeInteractive()
        val bid = "BV1wF411W7WM"
        val cid = client.getVideoParts(bid).data.first().cid!!
        val url = client
            .fetchVideoDashTracks(bid, cid).data!!
            .filterDashTracks(CodecId.AVC, QnQuality.V360P)!!
            .baseUrl!!
        client.client.downloadFile(
            url,
            File("./storage/$bid.m4s").apply { ensureCreate() },
            getBuilder = { configureForBili() }
        )
    }

    @Test
    fun pgcDownloadTest(): Unit = runBlocking {
        val ep = 457775
        val url = client
            .fetchPgcDashTracks(ep).data!!
            .filterDashTracks(CodecId.HEVC, QnQuality.V1080P)!!
            .baseUrl!!
        client.client.downloadResumable(
            url,
            File("/Users/hbj/Downloads/ep$ep.m4s"),
            headBuilder = { configureForBili() },
            getBuilder = { configureForBili() },
            onDuplicate = { true }
        )
    }

    @Test
    fun multiThreadDownloadTest() = runBlocking {
        val ep = 107856
        val url = client
            .fetchPgcDashTracks(ep).data!!
            .filterDashTracks(CodecId.AVC, QnQuality.V720P)!!
            .baseUrl!!
        client.client.downloadResumable(
            url,
            File("/Users/hbj/Downloads/ep$ep.m4s"),
            headBuilder = { configureForBili() },
            getBuilder = { configureForBili() },
            onDuplicate = { true },
            partCount = 4,
        )
    }
}
