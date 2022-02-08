package moe.sdl.tracks.ui.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.datetime.Clock
import moe.sdl.tracks.model.search.VideoPartModel
import moe.sdl.tracks.model.search.VideoResultModel
import org.junit.jupiter.api.Test

internal class VideoResultViewTest {
    private val testModel = VideoResultModel(
        "【斯特国高仿】RGB魔刀千刃，像素屏幕还原动画特效",
        "BV1iL411F7iH",
        cover = "https://i0.hdslb.com/bfs/archive/27419c1e848c5636dd57cd0bae7f2a8653d9b04f.jpg",
        view = "1.4 万",
        like = "3501",
        duration = "03:47",
        authorName = "野兽仙贝",
        (Clock.System.now() - 10.toDuration(DurationUnit.DAYS)).epochSeconds,
        parts = listOf(VideoPartModel(1, "", "03:47", cid = 114514))
    )

    @Test
    fun videoResultItemTest() = singleWindowApplication {
        Surface {
            Row(
                modifier = Modifier.padding(25.dp)
            ) {
                VideoResultItem(testModel)
                Spacer(Modifier.width(10.dp))
                VideoResultItem(testModel.copy(cover = "https://i1.hdslb.com/bfs/archive/42c5d3cdd5e2665040cc567375fbc4205982191d.jpg"))
            }
        }
    }
}
