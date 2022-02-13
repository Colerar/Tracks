package moe.sdl.tracks.util

import com.github.ajalt.clikt.output.TermUi.echo
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi

object Log {
    fun debug(throwable: Throwable? = null, lazyMessage: () -> String) {
        if (moe.sdl.tracks.config.debug) {
            echo("[DEBUG]" + lazyMessage())
            throwable?.let { echo(it) }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline val String.color: Ansi
    get() = ansi().render(this)
