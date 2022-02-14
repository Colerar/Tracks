package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.OptionCallTransformContext
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import java.io.File
import moe.sdl.tracks.config.tracksPreference

private val strToFile: OptionCallTransformContext.(String) -> String = {
    val file = File(it)
    if (!file.exists()) throw UsageError("输入错误, '$it' 非有效路径, 请检查后重新输入", this, context)
    file.toPath().normalize().toFile().absolutePath
}

class Config : CliktCommand(
    name = "config", help = """
    配置命令
    """.trimIndent(), printHelpOnEmptyArgs = true
) {
    private val ffmpeg by option("-ffmpeg", help = "设置 FFmpeg 的路径")
        .convert(conversion = strToFile)

    private val ffprobe by option("-ffprobe", help = "设置 FFprobe 的路径")
        .convert(conversion = strToFile)

    override fun run() {
        echo("当前 FFmpeg 路径: ${tracksPreference.programDir.ffmpeg}")
        echo("当前 FFprobe 路径: ${tracksPreference.programDir.ffprobe}")
        ffmpeg?.let {
            tracksPreference.programDir.ffmpeg = it
            echo("@| FFmpeg 的路径被设置为: $it")
        }
        ffprobe?.let {
            tracksPreference.programDir.ffprobe = it
            echo("@| FFprobe 的路径被设置为: $it")
        }
    }
}
