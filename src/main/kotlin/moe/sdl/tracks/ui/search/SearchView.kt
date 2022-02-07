@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package moe.sdl.tracks.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.sdl.tracks.core.trimBiliNumber
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger {} }

@Composable
internal fun SearchView() {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    var text by remember { mutableStateOf("") }
    var trimmed by remember { mutableStateOf<String?>(null) }
    var submit by remember { mutableStateOf(false) }
    val isError = trimmed == null && submit
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier
            .widthIn(min = 200.dp)
            .padding(top = 25.dp, start = 50.dp, end = 50.dp, bottom = 5.dp)
        ) {
            SearchBar(
                text,
                placeholder = "输入视频 AV/BV 号等下载视频w(ﾟДﾟ)w",
                onChanged = { text = it },
                onClear = { text = "" },
                onSubmit = {
                    submit = true
                    if (text.isNotBlank()) {
                        scope.launch { trimmed = trimBiliNumber(text) }
                    }
                }
            )
        }
        AnimatedVisibility(
            isError,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val color = Color(200, 0, 0)
                Icon(Icons.Default.Warning, "错误警告", Modifier.width(30.dp).padding(horizontal = 3.dp), color)
                Text("输入错误! 应该输入带有 av, BV, md, ss, ep 等前缀的号码或其链接.", color = color)
            }
        }
        if (!trimmed.isNullOrBlank()) { Text(trimmed!!)
        }
        AnimatedVisibility(
            !isError,
        ) {

        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SearchBar(
    text: String,
    placeholder: String = "",
    onChanged: (newText: (String)) -> Unit = {},
    onClear: () -> Unit = {},
    onSubmit: () -> Unit = {},
    isError: Boolean = false,
) {
    var showClearButton by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                showClearButton = it.isFocused
            }
            .focusRequester(focusRequester)
            .onKeyEvent {
                when (it.key) {
                    Key.Enter -> {
                        onSubmit()
                        focusManager.clearFocus()
                        true
                    }
                    Key.Escape -> {
                        focusManager.clearFocus()
                        true
                    }
                    else -> false
                }
            },
        textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp),
        value = text,
        onValueChange = onChanged,
        maxLines = 1,
        singleLine = true,
        isError = isError,
        shape = MaterialTheme.shapes.small.copy(all = CornerSize(30.dp)),
        placeholder = {
            Text(placeholder, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = showClearButton,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(onClear) {
                    Icon(
                        Icons.Filled.Close,
                        "Clear Button"
                    )
                }
            }
        },
    )
}
