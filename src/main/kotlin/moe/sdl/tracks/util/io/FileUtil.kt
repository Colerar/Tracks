package moe.sdl.tracks.util.io

import io.ktor.util.normalizeAndRelativize
import java.io.File
import java.net.URLDecoder
import kotlin.io.path.absolutePathString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.sdl.tracks.util.Log

/**
 * 获取当前 jar 文件绝对位置
 */
@Suppress("SpellCheckingInspection")
fun getJarLocation(): File {
    var path: String = Init::class.java.protectionDomain.codeSource.location.path.also {
        Log.debug { "Got raw jar path: $it" }
    }
    if (System.getProperty("os.name").lowercase().contains("dows")) {
        path = path.substring(1)
    }
    if (path.contains("jar")) {
        path = path.substring(0, path.lastIndexOf("/"))
        return File(URLDecoder.decode(path, Charsets.UTF_8))
    }
    return File(URLDecoder.decode(path.replace("target/classes/", ""), Charsets.UTF_8)).also {
        Log.debug { "Finally processed jar path: ${it.absolutePath}" }
    }
}

private object Init

suspend fun File.ensureCreate() = withContext(Dispatchers.IO) {
    if (!this@ensureCreate.exists()) {
        Log.debug { "Creating file at ${this@ensureCreate.absolutePath}" }
        this@ensureCreate.parentFile?.mkdirs()
        this@ensureCreate.createNewFile()
    }
}

fun File.toNormalizedAbsPath(): String = toPath().normalize().absolutePathString()
