package moe.sdl.tracks

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.TermUi.echo
import java.nio.channels.UnresolvedAddressException
import moe.sdl.tracks.cmd.Dig
import moe.sdl.tracks.cmd.LoginQR
import moe.sdl.tracks.consts.tracksVersion
import moe.sdl.tracks.util.Log
import moe.sdl.tracks.util.color
import org.fusesource.jansi.AnsiConsole

fun main(args: Array<String>) {
    AnsiConsole.systemInstall()
    try {
        MainCommand()
            .subcommands(Dig(), LoginQR())
            .main(args)
    } catch (e: UnresolvedAddressException) {
        echo("@|red,bold 网络错误！可能是网络不稳定或已离线|@".color)
        echo(e)
        Log.debug(e) { "Stacktrace:" }
    }
}

class MainCommand : CliktCommand(
    name = "tracks",
    help =
    """
       Kotlin 编写的哔哩哔哩下载 Cli ${"@|yellow,bold $tracksVersion|@\n".color}
    """.trimIndent(),
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true,
) {
    override fun run() = Unit
}
