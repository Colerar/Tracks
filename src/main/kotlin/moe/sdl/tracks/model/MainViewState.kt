package moe.sdl.tracks.model

import androidx.compose.runtime.MutableState
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class MainViewState(
    val window: WindowState,
    val tray: TrayState,
    val visible: MutableState<Boolean>,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
)
