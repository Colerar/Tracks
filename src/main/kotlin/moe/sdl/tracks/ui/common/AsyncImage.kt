package moe.sdl.tracks.ui.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import com.dropbox.android.external.store4.get
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.sdl.tracks.core.biliImgStore
import moe.sdl.yabapi.enums.ImageFormat
import moe.sdl.yabapi.util.string.buildImageUrl
import mu.KotlinLogging
import org.xml.sax.InputSource

private val logger by lazy { KotlinLogging.logger {} }

@Composable
fun BiliImage(
    url: String,
    description: String = "图像",
    format: ImageFormat = ImageFormat.WEBP,
    quality: Int = 75,
    pxWidth: Int? = null,
    pxHeight: Int? = null,
    onLoad: (ImageBitmap) -> Unit = {},
    modifier: Modifier = Modifier,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    AsyncImage(
        load = {
            biliImgStore.get(buildImageUrl(url, format, quality, pxWidth, pxHeight)).also {
                onLoad(it)
            }
        },
        painterFor = { remember { BitmapPainter(it) } },
        contentDescription = description,
        dispatcher = dispatcher,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
internal fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    val image: T? by produceState<T?>(null) {
        value = withContext(dispatcher) {
            try {
                load()
            } catch (e: IOException) {
                logger.warn(e) { "Failed to load image by ${load::class.qualifiedName}..." }
                null
            }
        }
    }

    if (image != null) {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

internal fun loadImageBitmap(file: File): ImageBitmap =
    file.inputStream().buffered().use(::loadImageBitmap)

internal fun loadSvgPainter(file: File, density: Density): Painter =
    file.inputStream().buffered().use { loadSvgPainter(it, density) }

internal fun loadXmlImageVector(file: File, density: Density): ImageVector =
    file.inputStream().buffered().use { loadXmlImageVector(InputSource(it), density) }

internal suspend fun loadImageBitmap(url: String): ImageBitmap =
    urlStream(url).use(::loadImageBitmap)

internal suspend fun loadSvgPainter(url: String, density: Density): Painter =
    urlStream(url).use { loadSvgPainter(it, density) }

internal suspend fun loadXmlImageVector(url: String, density: Density): ImageVector =
    urlStream(url).use { loadXmlImageVector(InputSource(it), density) }

private suspend fun urlStream(url: String) = HttpClient(CIO).use {
    ByteArrayInputStream(it.get(url))
}
