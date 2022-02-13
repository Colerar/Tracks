package moe.sdl.tracks.util

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.output.TermUi.echo

fun CliktCommand.errorExit(code: Int = 1, withHelp: Boolean = true, lazyMessage: () -> Any?): Nothing {
    echo(lazyMessage().toString(), err = true)
    if (withHelp) {
        echo("\n")
        echo(getFormattedHelp())
    }
    throw ProgramResult(code)
}

fun CliktCommand.infoExit(code: Int = 0, lazyMessage: () -> Any?): Nothing {
    echo(lazyMessage().toString())
    throw ProgramResult(code)
}
