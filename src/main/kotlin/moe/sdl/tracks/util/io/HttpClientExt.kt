package moe.sdl.tracks.util.io

import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isNotEmpty
import io.ktor.utils.io.core.readBytes
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.properties.Delegates
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.sdl.tracks.config.protoBuf
import moe.sdl.tracks.util.Log
import okio.buffer
import okio.sink
import okio.source

@OptIn(ExperimentalContracts::class)
suspend fun HttpClient.downloadFile(
    url: String,
    dst: File,
    getBuilder: HttpRequestBuilder.() -> Unit = {},
) = withContext(Dispatchers.IO) {
    this@downloadFile.get<HttpStatement>(url, getBuilder).execute {
        val channel: ByteReadChannel = it.receive()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong(), 0)
            while (packet.isNotEmpty) {
                val bytes = packet.readBytes()
                dst.appendBytes(bytes)
                Log.debug { ("Received ${dst.length()} bytes from ${it.contentLength()}") }
            }
        }
        Log.debug { "A File saved to ${dst.path}, size: ${dst.length()}" }
    }
}

@Serializable
private data class SideData(
    val total: Long,
    val parts: MutableList<Part>,
)

@Serializable
private data class Part(
    val range: Pair<Long, Long>,
    @Serializable(FileSerializer::class)
    val file: File,
    val finished: Boolean = false,
)

@Serializable
private object FileSerializer : KSerializer<File> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("File", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: File) {
        encoder.encodeString(value.absolutePath)
    }

    override fun deserialize(decoder: Decoder): File = File(decoder.decodeString())
}

private fun SideData(total: Long, part: Long, files: List<File>): SideData {
    val partSize = (total / part)
    val ranges = (0..total).step(partSize).windowed(2).toMutableList()
    ranges[ranges.size - 1] = ranges.last().toMutableList().apply { set(1, total) }
    val mapped = ranges.mapIndexed { index, (fst, scd) ->
        (if (index == 0) fst else fst + 1) to scd
    }
    val zipped = mapped.zip(files)
    return SideData(total, zipped.map { (range, file) ->
        Part(
            range,
            file,
            false,
        )
    }.toMutableList())
}

/**
 * @param onDuplicate If [dst] are not file downloaded before, and exists, aka, name shadowed,
 * this func will be invoked. Func should return [Boolean] to control flow, `true` for *replace the origin file*;
 * `false` for return func.
 */
suspend fun HttpClient.downloadResumable(
    url: String,
    dst: File,
    onDuplicate: () -> Boolean,
    onReceive: (received: Long, total: Long) -> Unit = { _, _ -> },
    headBuilder: HttpRequestBuilder.() -> Unit = {},
    getBuilder: HttpRequestBuilder.() -> Unit = {},
    partCount: Long = 1,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("Tracks-Download")),
) {
    val sideFile = File(dst.parent, ".${dst.nameWithoutExtension}.tracksdown")
    var sideData: SideData? by Delegates.observable(null) { _, _, new ->
        sideFile.apply { writeBytes(protoBuf.encodeToByteArray(new)) }
        Log.debug { "SideData updated: $new" }
    }

    // [dst] exists but side file not, may be duplicated
    if (!dst.exists() && sideFile.exists()) {
        if (onDuplicate()) {
            Log.debug { "Stopping download, due to file name shadowed." }
            return
        }
        dst.apply {
            delete()
            ensureCreate()
        }
    }

    // INIT: Both are not exist, create new file and side file.
    if (!dst.exists() && !sideFile.exists()) {
        val total: Long = head<HttpStatement>(url, headBuilder).execute {
            it.headers[HttpHeaders.ContentLength]?.toLong() ?: error("Failed to get total length for $url")
        }
        val files = buildList {
            if (partCount == 1L) this.add(dst)
            for (i in 1L..partCount) {
                this.add(File(dst.parent, ".${dst.nameWithoutExtension}.trackspart$i"))
            }
        }
        dst.ensureCreate()
        sideFile.ensureCreate()
        sideData = SideData(total, partCount, files)
    }

    // LOAD: if side file exists, load it
    if (sideFile.exists()) sideData = protoBuf.decodeFromByteArray(sideFile.readBytes())

    requireNotNull(sideData) { "Failed to load sideData, find null" }

    require(sideData!!.parts.isNotEmpty()) { "Failed to load sideData.parts, find empty" }

    sideData?.parts?.mapIndexed { index, part ->
        coroutineScope.async {
            val (start, end) = part.range
            downloadFile(url, part.file, getBuilder = {
                getBuilder()
                header(HttpHeaders.Range, "bytes=${start + part.file.length()}-$end".also {
                    Log.debug { "Range header: $it" }
                })
                onReceive(part.file.length(), end)
            })
            sideData = sideData!!.copy().apply {
                parts[index] = parts[index].copy(finished = true)
            }
            Log.debug { "Part ${part.file.absolutePath}, finished download" }
        }
    }.orEmpty().awaitAll()
    dst.sink().use { sink ->
        val bufferedSink = sink.buffer()
        sideData?.parts?.map { it.file }.orEmpty().forEach {
            it.source().use { source ->
                bufferedSink.writeAll(source.buffer())
            }
            it.delete()
        }
        sideFile.delete()
    }
}
