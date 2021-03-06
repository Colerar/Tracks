package moe.sdl.tracks

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import moe.sdl.tracks.cmd.Config
import moe.sdl.tracks.cmd.Dig
import moe.sdl.tracks.cmd.Login
import moe.sdl.tracks.cmd.Version
import moe.sdl.tracks.config.VERSION
import moe.sdl.tracks.config.debug
import moe.sdl.tracks.config.tracksPreference
import moe.sdl.tracks.util.Log
import moe.sdl.tracks.util.OsType
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.osType
import org.fusesource.jansi.AnsiConsole
import java.nio.channels.UnresolvedAddressException

fun main(args: Array<String>) {
    if (tracksPreference.first && osType == OsType.WINDOWS) {
        println(
            """ 
            @|bold 检测到您是首次运行本程序, 并且当前运行环境是 Windows|@
            @|bold 为了更好的使用体验, 强烈建议您, 不要将本程序运行于默认 cmd / powershell 上|@
            @|bold 推荐使用 Windows Terminal 等现代终端, 下载地址: https://aka.ms/terminal|@
            """.trimIndent().color
        )
        tracksPreference.first = false
    }
    if (tracksPreference.enableColor) AnsiConsole.systemInstall()
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, if (debug) "DEBUG" else "ERROR")
    try {
        MainCommand()
            .subcommands(Dig(), Login(), Config(), Version())
            .main(args)
    } catch (e: UnresolvedAddressException) {
        println("@|red,bold 网络错误！可能是网络不稳定或已离线|@".color)
        println(e)
        Log.debug(e) { "Stacktrace:" }
    }
}

class MainCommand : CliktCommand(
    name = "tracks",
    help =
    """
       Kotlin 编写的哔哩哔哩下载 Cli ${"@|yellow,bold v$VERSION|@\n".color}
    """.trimIndent(),
    printHelpOnEmptyArgs = true,
) {
    init {
        completionOption("-g", "-generate-completion", help = "为 bash|zsh|fish 生成补全文件")
    }

    override fun run() = Unit
}
