package moe.sdl.tracks.config

import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import moe.sdl.tracks.consts.TRACKS_CONFIG_FILE
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

internal val tracksPreference: TracksPreference by lazy {
    runBlocking { getOrCreatePreference(TracksPreference()) }.apply {
        addShutdownSaveHook()
    }
}

@Serializable
internal class TracksPreference(
    var isDebug: Boolean = true,
    var hadFirstClose: Boolean = false,
) : Preference() {
    @Transient
    override val file: File = File(TRACKS_CONFIG_FILE)
}
