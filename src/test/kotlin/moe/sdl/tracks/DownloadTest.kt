package moe.sdl.tracks

import moe.sdl.tracks.cmd.Dig
import org.junit.jupiter.api.Test

class DownloadTest {
    @Test
    fun `8K`() {
        Dig().parse("https://www.bilibili.com/video/BV1KS4y197BN".split(' '))
    }

    @Test
    fun lowSizeTest() {
        Dig().parse("https://www.bilibili.com/video/av170001".split(' '))
    }
}
