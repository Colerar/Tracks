package moe.sdl.tracks.config

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import moe.sdl.tracks.consts.TRACKS_CONFIG_FILE

internal val tracksConfig: TracksConfig by lazy {
    val file = File(TRACKS_CONFIG_FILE)
    if (file.exists()) {
        val text = file.readText()
        json.decodeFromString(text)
    } else {
        TracksConfig(false).also {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText(json.encodeToString(it))
        }
    }
}

@Serializable
internal data class TracksConfig(
    val isDebug: Boolean,
)
