package moe.sdl.tracks

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.ktor.util.getDigestFunction
import moe.sdl.tracks.consts.ICON_320W
import moe.sdl.tracks.consts.TRAY_ICON_WHITE
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

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
    Tray(icon = trayIcon,
        onAction = {
            showWindow.value = true
        },
        menu = {
            Item("打开 Tracks", onClick = { showWindow.value = true })
            Item("退出", onClick = {
                logger.info { "Tracks exit." }
                exitApplication()
            })
        },
        tooltip = "Tracks"
    )
    MaterialTheme {
        mainWindow(this, windowState, showWindow)
    }
}

@Composable
@Preview
fun mainWindow(
    scope: ApplicationScope,
    state: WindowState,
    showWindow: MutableState<Boolean>,
) = scope.apply {
    val icon = painterResource(ICON_320W)
    if (showWindow.value) {
        Window(onCloseRequest = {
            logger.info { "Main window closed, still running in background..." }
            showWindow.value = false
        }, icon = icon, title = "Tracks", state = state) {
            logger.info { "Main window showed" }
            Surface {
            }
        }
    }
}
