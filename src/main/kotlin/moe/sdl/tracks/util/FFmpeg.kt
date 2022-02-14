package moe.sdl.tracks.util

import java.io.File

private val failedRegex = Regex("""(not find|not found|could not|cannot|can't|can not)""", RegexOption.IGNORE_CASE)

fun getCliPath(program: String): String? {
    val cmd = if (osType == OsType.WINDOWS) "where.exe" else "which"
    val callback = Runtime.getRuntime()
        .exec("$cmd $program").inputStream.readBytes().decodeToString()
    if (failedRegex.matches(callback)) return null
    val path = callback.reader().readLines().firstOrNull() ?: return null
    val file = File(path)
    if (file.exists()) {
        return file.toPath().normalize().toFile().absolutePath
    }
    return null
}
