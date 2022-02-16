package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import java.io.File
import moe.sdl.tracks.config.tracksPreference
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.io.toNormalizedAbsPath
import net.bramp.ffmpeg.FFmpeg

class Config : CliktCommand(
    name = "config", help = """
    配置命令
    
    使用方法:
     
    'tracks config key1=xxx,key2=yyy,' 分别设置 key1 key2 的值为 xxx yyy
    
    'tracks config key1,key2' 查询 key1 和 key2 的值
    
    'tracks config key1=xxx,key2' 设置 key1 为 xxx, 查询 key2
    """.trimIndent(), printHelpOnEmptyArgs = true
) {
    private val expr by argument("expr", help = "表达式")
        .convert { opt ->
            val partSyntaxErr by lazy { UsageError("解析失败! 请检查语法后重试", this, context) }
            if (!Regex("""(.+(=.+)?[,，]?)+""").matches(opt)) throw partSyntaxErr
            opt.replace('，', ',').split(',').asSequence()
                .filter { it.isNotBlank() && it.isNotEmpty() }
                .associateTo(mutableMapOf()) {
                    val values =
                        Regex("""^([\w_-]+)(=)?(.+?)?$""").find(it)?.groupValues?.drop(1) ?: throw partSyntaxErr
                    if (values.size == 2) throw partSyntaxErr
                    val key = values.getOrNull(0) ?: throw partSyntaxErr
                    val value = values.getOrNull(2)
                    key to value
                }.toMap()
        }

    override fun run() {
        expr.asSequence().forEach { (k, v) ->
            val operation = keyMap[k.lowercase()] ?: run {
                echo("无匹配 [$k] 的设置")
                return@forEach
            }
            runCatching {
                if (v == null || v.isBlank() || v.isEmpty()) operation.onQuery(this) else operation.onSet(this, v)
            }.onFailure {
                echo("对于 [$k] 的设置出现错误, 可能是输入错误: $v")
                echo(it)
            }
        }
    }
}

private val keyMap by lazy {
    mapOf(
        "ffmpeg" to ArgumentOperation(
            onQuery = { TermUi.echo("FFmpeg 路径目前为 ${tracksPreference.programDir.ffmpeg}") },
            onSet = {
                val abs = File(it).toNormalizedAbsPath()
                if (FFmpeg(abs).isFFmpeg) {
                    tracksPreference.programDir.ffmpeg = abs
                    TermUi.echo("@|yellow FFmpeg 路径被设置为: $it |@".color)
                } else TermUi.echo("@|red 输入路径非 FFmpeg 路径: $abs|@".color)
            },
        ),
//        "ffprobe" to ArgumentOperation(
//            onQuery = { TermUi.echo("FFprobe 路径目前为 ${tracksPreference.programDir.ffprobe}") },
//            onSet = {
//                val abs = File(it).toNormalizedAbsPath()
//                if (FFprobe(abs).isFFprobe) {
//                    tracksPreference.programDir.ffprobe = abs
//                    TermUi.echo("@|yellow FFprobe 路径被设置为: $it |@".color)
//                } else TermUi.echo("@|red 输入路径非 FFprobe 路径: $abs|@".color)
//            },
//        )
    )
}

private class ArgumentOperation(
    val onQuery: CliktCommand.() -> Unit,
    val onSet: CliktCommand.(value: String) -> Unit,
)
