package moe.sdl.tracks.util

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult

fun CliktCommand.errorExit(code: Int = 1, withHelp: Boolean = true, lazyMessage: () -> Any?): Nothing {
    issueMessage(lazyMessage().toString())
    if (withHelp) {
        println("\n")
        println(getFormattedHelp())
    }
    throw ProgramResult(code)
}

fun infoExit(code: Int = 0, lazyMessage: () -> Any?): Nothing {
    println(lazyMessage().toString())
    throw ProgramResult(code)
}
