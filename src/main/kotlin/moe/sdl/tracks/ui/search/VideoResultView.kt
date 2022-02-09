package moe.sdl.tracks.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import moe.sdl.tracks.config.client
import moe.sdl.tracks.model.search.VideoResultModel
import moe.sdl.yabapi.api.getVideoInfo
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger {} }

@Composable
fun VideoResultView(
    bvId: String,
) {
    var model by remember { mutableStateOf<VideoResultModel?>(null) }
    LaunchedEffect(bvId) {
        model = VideoResultModel(client.getVideoInfo(bvId))
    }
    model?.let { VideoResultItem(it) }
}
