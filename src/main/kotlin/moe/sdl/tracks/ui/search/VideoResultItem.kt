package moe.sdl.tracks.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.sdl.tracks.consts.NOT_FOUND_COVER
import moe.sdl.tracks.model.search.VideoResultModel
import moe.sdl.tracks.ui.common.BiliImage
import moe.sdl.tracks.util.string.toAbsTime
import moe.sdl.tracks.util.string.toRelativeTime
import moe.sdl.tracks.util.string.toStringOrDefault
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger {} }

private val highlight = Color(75, 172, 230)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun VideoResultItem(
    model: VideoResultModel,
    onClick: () -> Unit = {},
) {
    val width = remember { mutableStateOf(256.dp) }
    var showAbsDate by remember { mutableStateOf(false) }
    val dateToShow by remember {
        mutableStateOf(derivedStateOf {
            model.date.toStringOrDefault {
                if (showAbsDate) it.toAbsTime() else it.toRelativeTime()
            }
        })
    }
    Column(Modifier.width(width.value)) {
        VideoBox(model, width)
        var isHighlighted by remember { mutableStateOf(false) }
        val animatedColor = animateColorAsState(if (!isHighlighted) Color.Black else highlight,
            spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
        // Title
        Text(AnnotatedString(model.title,
            SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = animatedColor.value),
            ParagraphStyle(textAlign = TextAlign.Justify, textIndent = TextIndent.None)),
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 5.dp)
                .pointerMoveFilter(onEnter = {
                    isHighlighted = true
                    false
                }, onExit = {
                    isHighlighted = false
                    false
                }).clickable(interactionSource = MutableInteractionSource(), indication = null) {
                    logger.debug { "Title of VideoResultItem was Clicked" }
                    onClick()
                })
        Spacer(Modifier.width(4.dp))
        // Author and TIme
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            StatusIcon(
                text = "${model.authorName} · ",
                icon = Icons.Outlined.Person,
                needEndSpacer = false,
            )
            // Time
            Box {
                if (!showAbsDate) {
                    StatusIcon(
                        text = dateToShow.value,
                        modifier = Modifier.pointerMoveFilter(
                            onEnter = {
                                showAbsDate = true
                                false
                            },
                        ),
                    )
                }
                this@Row.AnimatedVisibility(showAbsDate,
                    enter = fadeIn(initialAlpha = 0.1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()) {
                    StatusIcon(
                        text = dateToShow.value,
                        modifier = Modifier.pointerMoveFilter(onExit = {
                            showAbsDate = false
                            false
                        }),
                    )
                }
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private inline fun VideoBox(
    model: VideoResultModel,
    width: MutableState<Dp>,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var height by remember { mutableStateOf(width.value / 1.778f) }
    val pxW by remember { derivedStateOf { with(density) { width.value.toPx().toInt() } } }
    val pxH by remember { derivedStateOf { with(density) { height.toPx() } } }
    var maskVisibility by remember { mutableStateOf(true) }
    Box(modifier = Modifier
        .size(width.value, height)
        .requiredSize(width.value, height)
        .clip(RoundedCornerShape(10.dp))
        .pointerMoveFilter(onEnter = {
            maskVisibility = false
            false
        }, onExit = {
            maskVisibility = true
            false
        }).then(modifier), contentAlignment = Alignment.BottomStart) {
        BiliImage(
            url = model.cover ?: NOT_FOUND_COVER,
            description = "${model.title} 的封面",
            pxWidth = pxW,
            onLoad = { img ->
                // Let Box Aspect Ratio as the same remote image, [Double] for precision purpose
                height = width.value / (img.width.toDouble() / img.height.toDouble()).toFloat()
            },
            modifier = Modifier.matchParentSize()
        )
        AnimatedVisibility(
            maskVisibility,
            Modifier.matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(Modifier.size(width.value.value.dp, height.value.dp).background(Brush.verticalGradient(colors = listOf(
                Color.Black.copy(alpha = 0.5f),
                Color.Transparent,
            ), startY = pxH, endY = pxH * 0.7f)))
            Box(Modifier.align(Alignment.BottomCenter)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    StatusIcon(model.view, Icons.Outlined.PlayArrow, "播放量", tint = Color.White)
                    StatusIcon(model.like, Icons.Outlined.ThumbUp, "点赞量", tint = Color.White)
                    Spacer(Modifier.weight(1f))
                    StatusIcon(model.duration, needEndSpacer = false, tint = Color.White)
                }
            }
        }
    }
}

@Composable
internal fun StatusIcon(
    text: String,
    icon: ImageVector? = null,
    description: String? = null,
    needEndSpacer: Boolean = true,
    modifier: Modifier = Modifier,
    tint: Color = Color.Black,
) = Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    icon?.let {
        Icon(icon, description, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(3.dp))
    }
    Text(text, color = tint, fontSize = 14.sp)
    if (needEndSpacer) Spacer(Modifier.size(10.dp))
}
