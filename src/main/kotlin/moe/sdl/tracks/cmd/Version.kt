package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import moe.sdl.tracks.config.BUILD_BRANCH
import moe.sdl.tracks.config.BUILD_EPOCH_TIME
import moe.sdl.tracks.config.COMMIT_HASH
import moe.sdl.tracks.config.VERSION
import moe.sdl.tracks.util.string.toAbsTime

class Version : CliktCommand(
    "显示 Tracks 版本",
    name = "version",
) {
    val long: Boolean by option("-l", "-long", help = "显示长版本号, 默认关闭")
        .flag("-s", "-short", default = false, defaultForHelp = "默认关闭")

    override fun run() {
        echo(
            if (long) "v$VERSION[$BUILD_BRANCH]$COMMIT_HASH ${BUILD_EPOCH_TIME.toAbsTime()}"
            else "v$VERSION"
        )
    }
}
