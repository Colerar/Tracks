package moe.sdl.tracks.config

import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import moe.sdl.tracks.consts.TRACKS_CONFIG_FILE

internal val tracksPreference: TracksPreference =
    runBlocking { getOrCreatePreference(TracksPreference()) }.apply {
        addShutdownSaveHook()
    }

@Serializable
internal class TracksPreference(
    val enableColor: Boolean = true,
    val fileDir: Path = Path(),
    val programDir: Program = Program(),
) : Preference() {
    @Transient
    override val file: File = File(TRACKS_CONFIG_FILE)

    @Serializable
    class Path(
        val coverName: String = "%date%-%video:title%.png",
        val videoName: String = "%date%-%video:title%-%part:num%.m4v",
        val audioName: String = "%date%-%video:title%-%part:num%.m4a",
        val finalArtifact: String = "%date%-%video:title%-%part:num%.mp4",
    )

    @Serializable
    data class Program(
        var ffmpeg: String? = null,
//        var ffprobe: String? = null,
    )
}
