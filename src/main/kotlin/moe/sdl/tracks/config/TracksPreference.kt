package moe.sdl.tracks.config

import kotlinx.serialization.Serializable
import moe.sdl.tracks.consts.TRACKS_CONFIG_FILE

internal val tracksPreference: TracksPreference by lazy {
    getOrCreatePreference(TRACKS_CONFIG_FILE, TracksPreference())
}

@Serializable
internal class TracksPreference : Preference {
    val isDebug: Boolean = true
}
