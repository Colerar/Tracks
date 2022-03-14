package moe.sdl.tracks.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExternalTest {
    @Test
    fun getCliPathTest() {
        if (osType == OsType.MAC) {
            assertEquals("/usr/local/bin/ffmpeg", getCliPath("ffmpeg"))
        }
    }
}
