package moe.sdl.tracks.util

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.output.TermUi.echo

fun CliktCommand.errorExit(code: Int = 1, withHelp: Boolean = true, lazyMessage: () -> String): Nothing {
    echo(lazyMessage(), err = true)
    echo("\n")
    echo(getFormattedHelp())
    throw ProgramResult(code)
}
