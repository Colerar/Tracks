package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import java.io.File
import kotlin.reflect.KMutableProperty
import moe.sdl.tracks.config.tracksPreference
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.io.toNormalizedAbsPath
import net.bramp.ffmpeg.FFmpeg

class Config : CliktCommand(
    name = "config", help = """
    配置命令
    
    使用方法:
     
    'tracks config list' 列举所有可用选项
     
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
                val list = keyMap.toList()
                list.forEachIndexed { idx, (t, u) ->
                    echo("${t.padEnd(20, ' ')} - ${u.desc}")
                    if (idx != list.lastIndex) echo()
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
                if (it is UsageError) echo(it.text)
                else {
                    echo("对于 [$k] 的设置出现错误, 可能是输入错误: $v")
                    echo(it)
                }
            }
        }
    }
}

private class ArgumentOperation(
    val desc: String,
    val onQuery: CliktCommand.() -> Unit,
    val onSet: CliktCommand.(value: String) -> Unit,
)

@Suppress("FunctionName")
private inline fun <T : Any?> ArgumentVariable(
    name: String, prop: KMutableProperty<T>, crossinline conversion: CliktCommand.(String) -> T,
) = ArgumentOperation(
    desc = name,
    onQuery = { TermUi.echo("当前$name：${prop.getter.call()}") },
    onSet = {
        prop.setter.call(conversion(it))
        TermUi.echo("${name}设置为：${prop.getter.call()}")
    }
)

@Suppress("FunctionName")
private inline fun ArgumentVariableString(
    name: String, prop: KMutableProperty<String>, crossinline conversion: CliktCommand.(String) -> String = { it }
) = ArgumentVariable(name, prop, conversion)

@Suppress("FunctionName")
private inline fun ArgumentVariableStringNullable(
    name: String, prop: KMutableProperty<String?>, crossinline conversion: CliktCommand.(String) -> String? = { it }
) = ArgumentVariable(name, prop, conversion)

@Suppress("FunctionName")
private inline fun ArgumentBoolean(
    name: String, prop: KMutableProperty<Boolean>, crossinline conversion: CliktCommand.(String) -> Boolean = {
        val l = it.lowercase() // lowercase
        when {
            l.startsWith("t") || l.toBooleanStrictOrNull() == true || (it == "1") -> true
            l.startsWith("f") || l.toBooleanStrictOrNull() == true || (it == "0") -> false
            else -> throw UsageError("输入错误! 请输入 true/false!")
        }
    }
) = ArgumentVariable(name, prop, conversion)

private val keyMap by lazy {
    mapOf(
        "ffmpeg" to ArgumentVariableStringNullable("FFmpeg 路径", tracksPreference.programDir::ffmpeg, conversion = {
            val abs = File(it).toNormalizedAbsPath()
            if (FFmpeg(abs).isFFmpeg) it
            else throw UsageError("@|red 输入路径非 FFmpeg 路径: $abs|@".color.toString())
        }),
        "proxy-enable" to ArgumentBoolean("代理状态", tracksPreference.proxy::enable),
        "proxy-url" to ArgumentVariableStringNullable("HTTP 代理地址", tracksPreference.proxy::url),
        "name-cover" to ArgumentVariableString("封面名称样式", tracksPreference.fileDir::coverName),
        "name-video" to ArgumentVariableString("视频名称样式", tracksPreference.fileDir::videoName),
        "name-audio" to ArgumentVariableString("音频名称样式", tracksPreference.fileDir::audioName),
        "name-subtitle" to ArgumentVariableString("字幕名称样式", tracksPreference.fileDir::subtitleName),
        "name-final" to ArgumentVariableString("混流后的名称样式", tracksPreference.fileDir::finalArtifact),
    )
}
