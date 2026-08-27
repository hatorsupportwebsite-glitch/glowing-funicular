package com.nlshowcase.overlay.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.nlshowcase.overlay.state.Store
import kotlinx.coroutines.launch

/** Lets the service open/close the menu from outside Compose. */
class OverlayController {
    var open by mutableStateOf(false)
}

/**
 * Root of the overlay window.
 *
 * The menu is opened and closed with the white "home line" at the bottom of the
 * screen (tap or swipe up/down, iPhone style), and everything is animated:
 * fade + scale + slide, plus a progressive blur on the menu itself and on the
 * content behind the window.
 */
@Composable
fun OverlayRoot(
    controller: OverlayController,
    homeLineVisible: Boolean = true,
    blurEnabled: Boolean = true,
    menuScale: Float = 1f,
    onWindowExpand: (Boolean) -> Unit = {},
    onBackdropBlur: (Float) -> Unit = {},
) {
    val anim = remember { Animatable(0f) }
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    var screenHeight by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()

    val shown = dragFraction ?: anim.value

    // Popups blur the menu behind them, like in the original.
    val popupBlur by animateFloatAsState(
        targetValue = if (Store.menu.popupDepth > 0 && blurEnabled) 7f else 0f,
        animationSpec = tween(200),
        label = "popupBlur",
    )

    LaunchedEffect(controller.open) {
        if (controller.open) {
            onWindowExpand(true)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
            )
        } else {
            anim.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
            onWindowExpand(false)
        }
    }

    SideEffect { onBackdropBlur(if (blurEnabled) shown else 0f) }

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { screenHeight = it.height.toFloat().coerceAtLeast(1f) },
    ) {
        if (shown > 0.004f) {
            // dim / scrim, tap outside closes
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.38f * shown))
                    .pointerInput(Unit) {
                        detectTapGestures { controller.open = false }
                    },
            )

            Box(
                Modifier.fillMaxSize().padding(bottom = 44.dp, top = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight(0.86f)
                        .graphicsLayer {
                            alpha = shown
                            val s = (0.86f + 0.14f * shown) * menuScale
                            scaleX = s
                            scaleY = s
                            translationY = (1f - shown) * screenHeight * 0.16f
                        }
                        .blur(
                            radius = ((1f - shown) * 20f + popupBlur).dp,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded,
                        ),
                ) {
                    MenuRoot(onClose = { controller.open = false })
                }
            }
        }

        if (homeLineVisible) {
            HomeLine(
                shown = shown,
                onTap = { controller.open = !controller.open },
                onDragStart = {
                    onWindowExpand(true)
                    dragFraction = anim.value
                },
                onDrag = { dy ->
                    val current = dragFraction ?: anim.value
                    dragFraction = (current - dy / (screenHeight * 0.55f)).coerceIn(0f, 1f)
                },
                onDragEnd = {
                    val fraction = dragFraction ?: anim.value
                    dragFraction = null
                    val opening = fraction > 0.38f
                    controller.open = opening
                    scope.launch {
                        anim.snapTo(fraction)
                        if (opening) {
                            anim.animateTo(
                                1f,
                                spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
                            )
                        } else {
                            anim.animateTo(0f, tween(230, easing = FastOutSlowInEasing))
                            onWindowExpand(false)
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** The white iPhone-style home line: tap to toggle, swipe up/down to drag. */
@Composable
private fun HomeLine(
    shown: Float,
    onTap: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(38.dp)
            .verticalTapOrDrag(
                key = Unit,
                onTap = onTap,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .padding(bottom = 8.dp)
                .size(width = (132 + 14 * shown).dp, height = 5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f + 0.4f * shown)),
        )
    }
}
