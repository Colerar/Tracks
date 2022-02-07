package moe.sdl.tracks.util.io

import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isNotEmpty
import io.ktor.utils.io.core.readBytes
import java.io.File
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger {} }

internal suspend fun HttpClient.downloadFile(url: String, dst: File, request: HttpRequestBuilder.() -> Unit) {
    get<HttpStatement>(url, request).execute {
        val channel: ByteReadChannel = it.receive()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong(), 0)
            while (packet.isNotEmpty) {
                val bytes = packet.readBytes()
                dst.appendBytes(bytes)
                logger.trace { ("Received ${dst.length()} bytes from ${it.contentLength()}") }
            }
        }
        logger.info { "A File saved to ${dst.path}, size: ${dst.length()}" }
    }
}
