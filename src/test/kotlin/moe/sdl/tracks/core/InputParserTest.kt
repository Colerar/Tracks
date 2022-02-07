package moe.sdl.tracks.core

import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class InputParserTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun trimBiliNumberTest(): Unit = runBlocking {
        listOf(
            "av114514",
            "https://www.bilibili.com/bangumi/media/md28234644/",
            "https://www.bilibili.com/video/bv1JL4y1v7cZ?p=1",
            "https://www.bilibili.com/video/BV1oS4y1L7eg?spm_id_from=333.851.b_7265636f6d6d656e64.1",
            "https://www.bilibili.com/bangumi/play/ep467978?spm_id_from=..partition_recommend.content.click",
            "https://b23.tv/bctlPMP",
            "invalid",
        ).forEach {
            measureTimedValue {
                trimBiliNumber(it)
            }.also { (value, time) ->
                println("Costed ${time.inWholeMilliseconds} ms.")
                println("\tRaw $it ->\n\t\t$value")
            }
        }
    }
}
