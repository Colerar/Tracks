package moe.sdl.tracks

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.config.initYabapi
import moe.sdl.tracks.consts.ICON_320W
import moe.sdl.tracks.consts.TRAY_ICON_WHITE
import moe.sdl.yabapi.api.getVideoInfo
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Composable
@Preview
fun app() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    initYabapi()
    logger.info { "Starting Tracks application!" }
    val icon = painterResource(ICON_320W)
    val trayIcon = painterResource(TRAY_ICON_WHITE)
    runBlocking { client.getVideoInfo(170001) }
    Tray(
        icon = trayIcon,
        menu = {
            Item("Quit App", onClick = ::exitApplication)
        },
        tooltip = "Tracks"
    )
    Window(onCloseRequest = ::exitApplication, icon = icon, title = "Tracks") {
        Box(Modifier.paint(icon).fillMaxSize())
    }
}
