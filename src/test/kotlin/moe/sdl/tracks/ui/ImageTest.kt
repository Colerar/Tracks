package moe.sdl.tracks.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import moe.sdl.tracks.ui.common.BiliImage
import org.junit.jupiter.api.Test

internal class ImageTest {
    @Test
    fun imageTest() = singleWindowApplication {
        Row {
            BiliImage(
                "https://i0.hdslb.com/bfs/archive/ecabf2328336cf652f44e22ed6a67bf252a921ce.jpg@1344w_756h_1c",
                width = 400.dp,
                height = 500.dp
            )
        }
    }
}
