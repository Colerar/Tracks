package moe.sdl.tracks.ui

import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.sdl.tracks.model.RightPanelType
import moe.sdl.tracks.ui.common.PanelState
import moe.sdl.tracks.ui.common.ResizablePanel
import moe.sdl.tracks.ui.common.VerticalSplittable
import moe.sdl.tracks.ui.download.DownloadView
import moe.sdl.tracks.ui.search.SearchView
import moe.sdl.tracks.ui.settings.SettingsView
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger {} }

internal class MainViewState {
    var rightPanelType by mutableStateOf(RightPanelType.SEARCH)
}

@Composable
@Preview
internal fun MainView() {
    val panelState = remember { PanelState() }
    val viewState = remember { MainViewState() }
    val animatedSize = if (panelState.splitter.isResizing) {
        if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize
    } else {
        animateDpAsState(
            if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize,
            SpringSpec(stiffness = StiffnessLow)
        ).value
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
            Modifier.width(animatedSize).fillMaxHeight(), panelState
        ) {
            Column(
                Modifier.padding(top = 30.dp)
            ) {
                PanelIcon(Icons.Default.Search, "搜索", viewState, RightPanelType.SEARCH)
                PanelIcon(Icons.Default.List, "下载", viewState, RightPanelType.DOWNLOADING)
                PanelIcon(Icons.Default.Settings, "设置", viewState, RightPanelType.SETTINGS)
            }
        }

        Surface {
            when (viewState.rightPanelType) {
                RightPanelType.SEARCH -> SearchView()
                RightPanelType.DOWNLOADING -> DownloadView()
                RightPanelType.SETTINGS -> SettingsView()
            }
        }
    }
}

@Composable
internal fun PanelIcon(
    icon: ImageVector,
    text: String,
    state: MainViewState,
    switchTo: RightPanelType,
    onClick: () -> Unit = {},
) {
    Box(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
            .clickable {
                state.rightPanelType = switchTo
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, text, Modifier.size(50.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.requiredWidth(50.dp)
            )
        }
    }
}
