package moe.sdl.tracks.util

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi

object Log {
    fun debug(throwable: Throwable? = null, lazyMessage: () -> String) {
        if (moe.sdl.tracks.config.debug) {
            println(lazyMessage())
            throwable?.let { println(it) }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline val String.color: Ansi
    get() = ansi().render(this)
