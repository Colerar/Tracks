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
    """.trimIndent(),
    printHelpOnEmptyArgs = true
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
            val lowercase = k.lowercase()
            if (lowercase == "list") {
                echo("@|bold 当前可用配置:|@".color)
                keyMap.forEach { (t, u) -> 
                    echo()
                    echo("${t.padEnd(20, ' ')} - ${u.desc}")
                }
                return
            }
            val operation = keyMap[lowercase] ?: run {
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
            desc = "FFmpeg 路径",
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
        "proxy-enable" to ArgumentOperation(
            desc = "是否开启代理, 仅支持 http",
            onQuery = {
                TermUi.echo("当前代理状态 ${
                    if (tracksPreference.proxy.enable) "@|yellow,bold 开启|@".color.toString() else "关闭"
                }")
            },
            onSet = {
                val l = it.lowercase() // lowercase
                val boolean = when {
                    l.startsWith("t") || l.toBooleanStrictOrNull() ==true || (it == "1") -> true
                    l.startsWith("f") || l.toBooleanStrictOrNull() == true || (it == "0") -> false
                    else -> throw UsageError("输入错误! 请输入 true/false!")
                }
                tracksPreference.proxy.enable = boolean
                TermUi.echo(if (boolean) "代理已开启!" else "代理已关闭！")
            }
        ),
        "proxy-url" to ArgumentOperation(
            desc = "http 代理地址",
            onQuery = { TermUi.echo("当前代理地址: ${tracksPreference.proxy.url}") },
            onSet = {
                tracksPreference.proxy.url = it
                TermUi.echo("代理地址已设置为: $it")
            }
        )
    )
}

private class ArgumentOperation(
    val desc: String,
    val onQuery: CliktCommand.() -> Unit,
    val onSet: CliktCommand.(value: String) -> Unit,
)
