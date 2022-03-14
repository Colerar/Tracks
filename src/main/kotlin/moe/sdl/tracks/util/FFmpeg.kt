package moe.sdl.tracks.util

import moe.sdl.tracks.util.io.getJarLocation
import moe.sdl.tracks.util.io.toNormalizedAbsPath
import java.io.File

private val failedRegex = Regex("""(not find|not found|could not|cannot|can't|can not)""", RegexOption.IGNORE_CASE)

fun getCliPath(program: String): String? {
    val cmd = if (osType == OsType.WINDOWS) "where.exe" else "which"
    val callback = Runtime.getRuntime()
        .exec("$cmd $program").inputStream.readBytes().decodeToString()
    val path = if (failedRegex.matches(callback)) null else {
        callback.reader().readLines().firstOrNull()
    }
    return buildList {
        path?.let { add(File(it)) }
        add(File(getJarLocation(), program))
        add(File(getJarLocation(), "$program.exe"))
    }.firstOrNull { it.exists() }?.toNormalizedAbsPath()
}
