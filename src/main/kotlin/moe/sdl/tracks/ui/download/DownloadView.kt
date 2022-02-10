package moe.sdl.tracks.ui.download

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.sdl.tracks.ui.common.PanelState
import moe.sdl.tracks.ui.common.ResizablePanel
import moe.sdl.tracks.ui.common.VerticalSplittable
import moe.sdl.tracks.ui.settings.DownloadList

@Composable
internal fun DownloadView() {
    val viewState = remember { DownloadViewState() }
    val panelState = remember {
        PanelState().apply {
            collapsedSize = 80.dp
            expandedSize = 80.dp
            showIcon = false
        }
    }
    VerticalSplittable(
        Modifier.fillMaxSize(),
        panelState.splitter,
        onResize = {
            panelState.expandedSize = (panelState.expandedSize + it)
                .coerceAtLeast(panelState.expandedSizeMin)
                .coerceAtMost(panelState.expandedSizeMax)
        }
    ) {
        ResizablePanel(
            Modifier.width(80.dp), panelState
        ) {
            Column {
                Box(Modifier.height(15.dp).fillMaxWidth())
                PanelIcon("正在下载", viewState, DownloadPanelType.DOWNLOADING)
                PanelIcon("传输完成", viewState, DownloadPanelType.DOWNLOADING)
            }
        }
        Surface {
            DownloadList(
                emptyList(),
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

internal class DownloadViewState {
    var panelType by mutableStateOf(DownloadPanelType.DOWNLOADING)
}

internal enum class DownloadPanelType {
    DOWNLOADING,
    FINISHED,
}

@Composable
internal fun PanelIcon(
    text: String,
    state: DownloadViewState,
    switchTo: DownloadPanelType,
    onClick: () -> Unit = {},
) {
    Box(
        Modifier
            .height(30.dp)
            .fillMaxWidth()
            .clickable {
                state.panelType = switchTo
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.requiredWidth(80.dp)
        )
    }
}
