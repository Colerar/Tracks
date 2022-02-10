package moe.sdl.tracks.util

import moe.sdl.tracks.config.tracksPreference

object Log {
    fun debug(throwable: Throwable? = null, lazyMessage: () -> String) {
        if (tracksPreference.isDebug) {
            println(lazyMessage())
            println(throwable.toString())
        }
    }
}
