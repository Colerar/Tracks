package moe.sdl.tracks.util

val osType: OsType by lazy {
    System.getProperty("os.name")?.lowercase()?.run {
        when {
            contains("win") -> OsType.WINDOWS
            listOf("nix", "nux", "aix").any { contains(it) } -> OsType.LINUX
            contains("mac") -> OsType.MAC
            contains("sunos") -> OsType.SOLARIS
            else -> OsType.OTHER
        }
    } ?: OsType.OTHER
}

enum class OsType { WINDOWS, LINUX, MAC, SOLARIS, OTHER }
