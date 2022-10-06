package moe.sdl.tracks.config

import io.ktor.client.engine.ProxyType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import moe.sdl.tracks.consts.TRACKS_CONFIG_FILE
import java.io.File

internal val tracksPreference: TracksPreference =
    runBlocking { getOrCreatePreference(TracksPreference()) }.apply {
        addShutdownSaveHook()
    }

@Serializable
internal class TracksPreference(
    val enableColor: Boolean = true,
    val fileDir: Path = Path(),
    val programDir: Program = Program(),
    val show: Show = Show(),
    val proxy: Proxy = Proxy(),
    var userAgent: String? = null,
    var emoji: Boolean = true,
    var first: Boolean = true,
    var zhConvertAlert: Boolean = true,
) : Preference() {
    @Transient
    override val file: File = File(TRACKS_CONFIG_FILE)

    @Serializable
    class Path(
        var coverName: String = "%date%-%video:title%.png",
        var videoName: String = "%date%-%video:title%-%part:num%.m4v",
        var audioName: String = "%date%-%video:title%-%part:num%.m4a",
        var subtitleName: String = "%date%-%video:title%-%part:num%.%subtitle:lang%.srt",
        var finalArtifact: String = "%date%-%video:title%-%part:num%.mp4",
    )

    @Serializable
    class Program(
        var ffmpeg: String? = null,
//        var ffprobe: String? = null,
    )

    @Serializable
    class Show(
        val progressInterval: Long = 100,
    )

    @Serializable
    class Proxy(
        var enable: Boolean = false,
        val type: ProxyType = ProxyType.HTTP,
        var url: String? = null,
    )
}

fun emoji(str: String, fallback: String = "") = if (tracksPreference.emoji) str else fallback
