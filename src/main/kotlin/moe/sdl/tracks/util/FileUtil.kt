package moe.sdl.tracks.util

import java.io.File
import java.net.URLDecoder
import moe.sdl.tracks.config.TracksConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 获取当前 jar 文件绝对位置
 */
@Suppress("SpellCheckingInspection")
fun getJarLocation(): File {
    var path: String = TracksConfig::class.java.protectionDomain.codeSource.location.path.also {
        logger.debug { "Got raw jar path: $it" }
    }
    if (System.getProperty("os.name").lowercase().contains("dows")) {
        path = path.substring(1)
    }
    if (path.contains("jar")) {
        path = path.substring(0, path.lastIndexOf("/"))
        return File(URLDecoder.decode(path, Charsets.UTF_8))
    }
    return File(URLDecoder.decode(path.replace("target/classes/", ""), Charsets.UTF_8)).also {
        logger.debug { "Finally processed jar path: ${it.absolutePath}" }
    }
}
