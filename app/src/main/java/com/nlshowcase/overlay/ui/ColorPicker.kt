package com.nlshowcase.overlay.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlshowcase.overlay.state.Tint
import kotlin.math.roundToInt

private val QUICK_COLORS = listOf(
    Color(0xFFFFFFFF), Color(0xFFB4C8FF), Color(0xFF7BA4E8), Color(0xFF3B82F6),
    Color(0xFF6FE3A1), Color(0xFF4FD37E), Color(0xFFFFD479), Color(0xFFFF8A8A),
    Color(0xFFFF5A96), Color(0xFFC7A6FF), Color(0xFF8A919C), Color(0xFF1B2029),
)

fun hsvColor(h: Float, s: Float, v: Float, a: Float): Color =
    Color(android.graphics.Color.HSVToColor((a * 255f).roundToInt().coerceIn(0, 255), floatArrayOf(h, s, v)))

fun colorToHsv(color: Color): FloatArray {
    val out = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), out)
    return out
}

fun hexOf(color: Color, withAlpha: Boolean): String {
    val argb = color.toArgb()
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    val a = (argb shr 24) and 0xFF
    return if (withAlpha) String.format("#%02X%02X%02X%02X", r, g, b, a)
    else String.format("#%02X%02X%02X", r, g, b)
}

/** The small colour square shown inside settings rows. */
@Composable
fun NlSwatch(
    tint: Tint,
    enabled: Boolean = true,
    showAlpha: Boolean = true,
    chipLabel: String? = null,
    swatchSize: Dp = 18.dp,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Box(
            Modifier
                .size(swatchSize)
                .clip(RoundedCornerShape(5.dp))
                .background(Nl.Field)
                .border(1.dp, Nl.Stroke, RoundedCornerShape(5.dp))
                .clickable(enabled = enabled) { open = true },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(swatchSize - 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (enabled) tint.color else Nl.Disabled),
            )
        }
        if (open) {
            NlPopup(
                onDismiss = { open = false },
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, 26),
            ) {
                ColorPickerBody(tint, showAlpha, chipLabel)
            }
        }
    }
}

@Composable
fun ColorPickerBody(tint: Tint, showAlpha: Boolean, chipLabel: String?) {
    val initial = remember { colorToHsv(tint.color) }
    var hue by remember { mutableFloatStateOf(initial[0]) }
    var sat by remember { mutableFloatStateOf(initial[1]) }
    var value by remember { mutableFloatStateOf(initial[2]) }
    var alpha by remember { mutableFloatStateOf(tint.color.alpha) }
    var quickOpen by remember { mutableStateOf(false) }

    fun push() {
        tint.color = hsvColor(hue, sat, value, if (showAlpha) alpha else 1f)
    }

    Column(Modifier.width(266.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {

        if (chipLabel != null) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(chipLabel, color = Nl.Text, fontSize = 12.sp)
            }
        }

        // Saturation / value square
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        fun apply(p: Offset) {
                            sat = (p.x / size.width.toFloat()).coerceIn(0f, 1f)
                            value = 1f - (p.y / size.height.toFloat()).coerceIn(0f, 1f)
                            push()
                        }
                        apply(down.position)
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            apply(change.position)
                            change.consume()
                        }
                    }
                },
        ) {
            val pure = hsvColor(hue, 1f, 1f, 1f)
            drawRect(Brush.horizontalGradient(listOf(Color.White, pure)))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(sat * size.width, (1f - value) * size.height),
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        // Hue slider
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(50))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        fun apply(x: Float) {
                            hue = (x / size.width.toFloat()).coerceIn(0f, 1f) * 360f
                            push()
                        }
                        apply(down.position.x)
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            apply(change.position.x)
                            change.consume()
                        }
                    }
                },
        ) {
            drawRect(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                        Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF),
                        Color(0xFFFF0000),
                    ),
                ),
            )
            val cx = (hue / 360f) * size.width
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cx - 4.dp.toPx(), 0f),
                size = Size(8.dp.toPx(), size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            )
        }

        // Alpha slider with checkerboard
        if (showAlpha) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            fun apply(x: Float) {
                                alpha = (x / size.width.toFloat()).coerceIn(0f, 1f)
                                push()
                            }
                            apply(down.position.x)
                            down.consume()
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                apply(change.position.x)
                                change.consume()
                            }
                        }
                    },
            ) {
                val cell = 7.dp.toPx()
                var y = 0f
                var row = 0
                while (y < size.height) {
                    var x = 0f
                    var col = 0
                    while (x < size.width) {
                        val dark = (row + col) % 2 == 0
                        drawRect(
                            color = if (dark) Color(0xFF3A3F49) else Color(0xFF4A505C),
                            topLeft = Offset(x, y),
                            size = Size(cell, cell),
                        )
                        x += cell
                        col++
                    }
                    y += cell
                    row++
                }
                drawRect(
                    Brush.horizontalGradient(
                        listOf(hsvColor(hue, sat, value, 0f), hsvColor(hue, sat, value, 1f)),
                    ),
                )
                val cx = alpha * size.width
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - 4.dp.toPx(), 0f),
                    size = Size(8.dp.toPx(), size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                )
            }
        }

        // Footer: HEX + value + swatches
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Nl.TextDim,
                modifier = Modifier.size(16.dp),
            )
            Text("HEX", color = Nl.TextDim, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(tint.color),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = hexOf(tint.color, showAlpha),
                color = Nl.Text,
                fontSize = 11.sp,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.GridView,
                contentDescription = null,
                tint = if (quickOpen) Nl.Accent else Nl.TextDim,
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { quickOpen = !quickOpen },
            )
        }

        if (showAlpha) {
            Text(
                text = "${(alpha * 100f).roundToInt()}%",
                color = Nl.TextDim,
                fontSize = 11.sp,
            )
        }

        if (quickOpen) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                QUICK_COLORS.take(6).forEach { c -> QuickSwatch(c) { hsv -> hue = hsv[0]; sat = hsv[1]; value = hsv[2]; push() } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                QUICK_COLORS.drop(6).forEach { c -> QuickSwatch(c) { hsv -> hue = hsv[0]; sat = hsv[1]; value = hsv[2]; push() } }
            }
        }
    }
}

@Composable
private fun QuickSwatch(color: Color, onPick: (FloatArray) -> Unit) {
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Nl.Stroke, CircleShape)
            .clickable { onPick(colorToHsv(color)) },
    )
}
