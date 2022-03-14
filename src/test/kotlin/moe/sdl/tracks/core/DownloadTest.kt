package moe.sdl.tracks.core

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.util.io.downloadFile
import moe.sdl.tracks.util.io.downloadResumable
import moe.sdl.tracks.util.io.ensureCreate
import moe.sdl.tracks.util.io.fetchPgcDashTracks
import moe.sdl.tracks.util.io.fetchVideoDashTracks
import moe.sdl.tracks.util.io.filterDashTracks
import moe.sdl.tracks.util.readSidx
import moe.sdl.yabapi.api.getVideoParts
import moe.sdl.yabapi.data.stream.CodecId
import moe.sdl.yabapi.data.stream.QnQuality
import org.junit.jupiter.api.Test
import java.io.File

class DownloadTest {

    private inline fun HttpRequestBuilder.configureForBili(extra: HttpRequestBuilder.() -> Unit = {}) {
        this.extra()
        headers { append(HttpHeaders.Referrer, "https://www.bilibili.com") }
    }

    @Test
    fun sidxParseTest(): Unit = runBlocking {
        val bid = "BV1wF411W7WM"
        val cid = client.getVideoParts(bid).data.first().cid!!
        val tr = client.fetchVideoDashTracks(bid, cid).data!!
            .filterDashTracks(CodecId.AVC, QnQuality.V360P)!!
        val idxRange = tr.segmentBase!!.indexRange
        val bytes = client.client.get<ByteArray>(tr.baseUrl!!) {
            headers {
                append(HttpHeaders.Range, "bytes=${idxRange!!}")
                configureForBili()
            }
        }
        readSidx(bytes)
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
