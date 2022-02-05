package moe.sdl.tracks

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import moe.sdl.tracks.config.tracksPreference
import moe.sdl.tracks.consts.ICON_320W
import moe.sdl.tracks.consts.TRAY_ICON_WHITE
import moe.sdl.tracks.model.MainViewState
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
    val state = MainViewState(
        rememberWindowState(position = WindowPosition(Alignment.Center)),
        rememberTrayState(),
        remember { mutableStateOf(true) },
    )
    val trayIcon = painterResource(TRAY_ICON_WHITE)
    Tray(icon = trayIcon, onAction = {
        state.visible.value = true
    }, menu = {
        Item("打开 Tracks", onClick = {
            state.visible.value = true
        })
        Item("退出", onClick = {
            logger.info { "Tracks exit." }
            exitApplication()
        })
    }, tooltip = "Tracks", state = state.tray)
    MaterialTheme {
        mainWindow(this, state)
    }
}

@ExperimentalComposeUiApi
@Composable
@Preview
fun mainWindow(
    scope: ApplicationScope,
    state: MainViewState,
) = scope.apply {
    val icon = painterResource(ICON_320W)
    Window(onCloseRequest = {
        logger.info { "Main window closed, still running in background..." }
        runBackground(state)
    }, icon = icon, title = "Tracks", visible = state.visible.value, state = state.window) {
        this.window.apply {
            this.requestFocus()
        }
        MenuBar {
            Menu("窗口", mnemonic = 'F') {
                Item("关闭", onClick = {
                    logger.info { "Window closed by shortcut" }
                    state.visible.value = false
                    runBackground(state)
                }, shortcut = KeyShortcut(Key.W, meta = true))
            }
        }
        logger.info { "Main window showed" }
        Surface {}
    }
}

fun runBackground(state: MainViewState) {
    state.visible.value = false
    if (!tracksPreference.hadFirstClose.also { logger.trace { "Checked hadFirstClose is $it" } }) {
        logger.debug { "First close, try to send to send notification..." }
        tracksPreference.hadFirstClose = true
        state.tray.sendNotification(
            Notification("Tracks 仍在后台运行", "您可以在设置中更改此行为", Notification.Type.Info)
        )
    }
}
