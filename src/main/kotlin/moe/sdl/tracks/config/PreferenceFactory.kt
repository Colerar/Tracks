package moe.sdl.tracks.config

import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger {} }

@Serializable
internal abstract class Preference {
    internal abstract val file: File
    @Transient internal val mutex = Mutex()
}

internal suspend inline fun <reified T : Preference> T.save() = mutex.withLock {
    logger.info { "Saving ${T::class.qualifiedName} to ${file.absolutePath}" }
    file.writeText(json.encodeToString(this))
}

internal inline fun <reified T : Preference> T.addShutdownSaveHook() {
    logger.debug { "Adding shutdown save hook for ${T::class.qualifiedName}" }
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking { save() }
    })
}

internal suspend inline fun <reified T : Preference> getOrCreatePreference(default: T): T {
    val file = default.file
    val absPath = file.absolutePath
    return if (file.exists()) {
        default.mutex.withLock {
            logger.debug { "Reading preference from file $absPath" }
            val text = file.readText()
            json.decodeFromString(text)
        }
    } else {
        logger.debug { "Path $absPath not exist, try to create..." }
        default.apply {
            mutex.withLock {
                file.parentFile.mkdirs()
                file.createNewFile()
                file.writeText(json.encodeToString(this))
            }
        }
    }
}
