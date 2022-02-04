package moe.sdl.tracks.config

import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

internal interface Preference

internal inline fun <reified T: Preference> getOrCreatePreference(path: String, default: T): T {
    val file = File(path)
    return if (file.exists()) {
        val text = file.readText()
        json.decodeFromString(text)
    } else {
        default.also {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText(json.encodeToString(it))
        }
    }
}
