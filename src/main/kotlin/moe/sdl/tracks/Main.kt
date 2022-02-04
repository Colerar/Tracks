package moe.sdl.tracks

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import moe.sdl.tracks.consts.ICON_320W
import moe.sdl.tracks.consts.TRAY_ICON_WHITE
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@ExperimentalComposeUiApi
fun main() = application {
    logger.info { "Tracks launching!" }
    logger.info { "████████╗██████╗  █████╗  ██████╗██╗  ██╗███████╗" }
    logger.info { "╚══██╔══╝██╔══██╗██╔══██╗██╔════╝██║ ██╔╝██╔════╝" }
    logger.info { "   ██║   ██████╔╝███████║██║     █████╔╝ ███████╗" }
    logger.info { "   ██║   ██╔══██╗██╔══██║██║     ██╔═██╗ ╚════██║" }
    logger.info { "   ██║   ██║  ██║██║  ██║╚██████╗██║  ██╗███████║" }
    logger.info { "   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝" }
    val windowState = rememberWindowState(position = WindowPosition(Alignment.Center))
    val trayIcon = painterResource(TRAY_ICON_WHITE)
    val showWindow = remember { mutableStateOf(true) }
    Tray(icon = trayIcon, onAction = {
        showWindow.value = true
    }, menu = {
        Item("打开 Tracks", onClick = {
            showWindow.value = true
        })
        Item("退出", onClick = {
            logger.info { "Tracks exit." }
            exitApplication()
        })
    }, tooltip = "Tracks")
    MaterialTheme {
        mainWindow(this, windowState, showWindow)
    }
}

@ExperimentalComposeUiApi
@Composable
@Preview
fun mainWindow(
    scope: ApplicationScope,
    state: WindowState,
    showWindow: MutableState<Boolean>,
) = scope.apply {
    val icon = painterResource(ICON_320W)
    Window(onCloseRequest = {
        logger.info { "Main window closed, still running in background..." }
        showWindow.value = false
    }, icon = icon, title = "Tracks", visible = showWindow.value, state = state) {
        this.window.transferFocus()
        MenuBar {
            Menu("窗口", mnemonic = 'F') {
                Item("关闭", onClick = {
                    logger.info { "Window closed by shortcut" }
                    showWindow.value = false
                }, shortcut = KeyShortcut(Key.W, meta = true))
            }
        }
        logger.info { "Main window showed" }
        Surface {}
    }
}
