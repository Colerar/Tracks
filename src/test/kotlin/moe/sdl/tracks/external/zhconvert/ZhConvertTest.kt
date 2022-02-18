package moe.sdl.tracks.external.zhconvert

import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import org.junit.jupiter.api.Test

class ZhConvertTest {
    @Test
    fun convertTest(): Unit = runBlocking {
        client.client.requestZhConvert(
            "一隻憂鬱的臺灣烏龜",
            Converter.CHINA,
        ).also(::println)
    }
}
