package moe.sdl.tracks.util

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class ExternalTest {
    @Test
    fun getCliPathTest() {
        if (osType == OsType.MAC) {
            assertEquals("/usr/local/bin/ffmpeg", getCliPath("ffmpeg"))
        }
    }
}
