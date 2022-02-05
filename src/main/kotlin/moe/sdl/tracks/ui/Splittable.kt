package moe.sdl.tracks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalSplittable(
    modifier: Modifier,
    state: SplitterState,
    onResize: (delta: Dp) -> Unit = {},
    child: @Composable () -> Unit = {},
) = Layout(
    content = {
        child()
        VerticalSplitter(state, onResize = onResize)
    },
    modifier = modifier,
    measurePolicy = { measures, constraints ->
        require(measures.size == 3)
        val firstPlaceable = measures[0].measure(constraints.copy(minWidth = 0))
        val secondWidth = constraints.maxWidth - firstPlaceable.width
        val secondPlaceable = measures[1].measure(
            Constraints(
                minWidth = secondWidth,
                maxWidth = secondWidth,
                minHeight = constraints.maxHeight,
                maxHeight = constraints.maxHeight,
            )
        )
        val splitterPlaceable = measures[2].measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            firstPlaceable.place(0, 0)
            secondPlaceable.place(firstPlaceable.width, 0)
            splitterPlaceable.place(firstPlaceable.width, 0)
        }
    }
)

class SplitterState {
    var canResize by mutableStateOf(true)
    var isResizing by mutableStateOf(false)
}

@Composable
internal fun VerticalSplitter(
    state: SplitterState,
    color: Color = Color.Gray,
    onResize: (delta: Dp) -> Unit = {},
) = Box {
    val density = LocalDensity.current
    Box(
        Modifier
            .width(8.dp)
            .fillMaxHeight()
            .run {
                if (state.canResize) {
                    this.draggable(
                        state = rememberDraggableState {
                            with(density) {
                                onResize(it.toDp())
                            }
                        },
                        orientation = Orientation.Horizontal,
                        startDragImmediately = true,
                        onDragStarted = { state.isResizing = true },
                        onDragStopped = { state.isResizing = false }
                    )
                } else this
            }
    )
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color)
    )
}
