package com.nlshowcase.overlay.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.nlshowcase.overlay.state.Flag
import com.nlshowcase.overlay.state.MultiPick
import com.nlshowcase.overlay.state.Num
import com.nlshowcase.overlay.state.Pick
import com.nlshowcase.overlay.state.Store

/* ------------------------------------------------------------------ */
/*  Cards & rows                                                       */
/* ------------------------------------------------------------------ */

@Composable
fun NlCard(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Nl.CardRadius))
            .background(Nl.Card)
            .border(1.dp, Nl.Stroke, RoundedCornerShape(Nl.CardRadius)),
    ) {
        if (title != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Nl.CardTop)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = title.uppercase(),
                    color = Nl.TextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                )
            }
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) { content() }
    }
}

/**
 * A single settings row. [dots] renders the "···" sub-settings button when a
 * popup body is provided.
 */
@Composable
fun NlRow(
    label: String,
    enabled: Boolean = true,
    dots: (@Composable () -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }
    var dotsOpen by remember { mutableStateOf(false) }
    val highlight by animateFloatAsState(if (hovered) 1f else 0f, tween(160), label = "row")

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Nl.RowHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.04f * highlight))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (enabled) Nl.Text else Nl.Disabled,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (dots != null) {
            Box {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = null,
                    tint = if (dotsOpen) Nl.Text else Nl.TextFaint,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(enabled = enabled) { dotsOpen = true },
                )
                if (dotsOpen) {
                    NlPopup(onDismiss = { dotsOpen = false }, alignment = Alignment.TopEnd, offset = IntOffset(0, 26)) {
                        dots()
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
        }
        trailing()
    }
    hovered = false
}

@Composable
fun NlDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .height(1.dp)
            .background(Nl.Divider),
    )
}

/* ------------------------------------------------------------------ */
/*  Toggle                                                             */
/* ------------------------------------------------------------------ */

@Composable
fun NlToggle(flag: Flag, enabled: Boolean = true) {
    NlToggle(on = flag.on, enabled = enabled) { flag.on = it }
}

@Composable
fun NlToggle(on: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    val t by animateFloatAsState(if (on) 1f else 0f, tween(190), label = "toggle")
    val track = lerp(Nl.ToggleOff, Nl.Accent, t)
    Box(
        Modifier
            .size(width = 38.dp, height = 21.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) track else Nl.ToggleOff.copy(alpha = 0.45f))
            .clickable(enabled = enabled) { onChange(!on) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .padding(horizontal = 2.dp)
                .offset(x = 17.dp * t)
                .size(17.dp)
                .clip(CircleShape)
                .background(if (enabled) Color.White else Color(0xFF8A919C))
                .graphicsLayer { alpha = if (enabled) 1f else 0.6f },
        )
    }
}

/* ------------------------------------------------------------------ */
/*  Slider                                                             */
/* ------------------------------------------------------------------ */

@Composable
fun NlSlider(num: Num, enabled: Boolean = true, trackWidth: Dp = 92.dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(trackWidth)
                .height(22.dp)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        num.setFraction(down.position.x / size.width.toFloat())
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            num.setFraction(change.position.x / size.width.toFloat())
                            change.consume()
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Nl.Field),
            )
            Box(
                Modifier
                    .fillMaxWidth(num.fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (enabled) Nl.Accent else Nl.Disabled),
            )
            Box(
                Modifier
                    .fillMaxWidth(num.fraction)
                    .height(22.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = num.text,
            color = if (enabled) Nl.TextDim else Nl.Disabled,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 40.dp),
        )
    }
}

/* ------------------------------------------------------------------ */
/*  Dropdowns                                                          */
/* ------------------------------------------------------------------ */

@Composable
private fun DropdownButton(
    text: String,
    open: Boolean,
    enabled: Boolean,
    width: Dp,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(if (open) 180f else 0f, tween(180), label = "chev")
    Row(
        Modifier
            .width(width)
            .height(30.dp)
            .clip(RoundedCornerShape(Nl.FieldRadius))
            .background(if (open) Nl.FieldHover else Nl.Field)
            .clickable(enabled = enabled) { onClick() }
            .padding(start = 10.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = if (enabled) Nl.Text else Nl.Disabled,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = Nl.TextDim,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = rotation },
        )
    }
}

@Composable
private fun OptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) Color.White.copy(alpha = 0.07f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = if (selected) Nl.Text else Nl.TextDim,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Nl.Accent,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
fun NlPickField(pick: Pick, enabled: Boolean = true, width: Dp = 150.dp) {
    var open by remember { mutableStateOf(false) }
    Box {
        DropdownButton(pick.text, open, enabled, width) { open = true }
        if (open) {
            NlPopup(onDismiss = { open = false }, alignment = Alignment.TopEnd, offset = IntOffset(0, 34)) {
                Column(Modifier.width(width).verticalScroll(rememberScrollState())) {
                    pick.options.forEach { option ->
                        OptionRow(option, pick.selected == option) { pick.click(option) }
                    }
                }
            }
        }
    }
}

@Composable
fun NlMultiField(multi: MultiPick, enabled: Boolean = true, width: Dp = 150.dp) {
    var open by remember { mutableStateOf(false) }
    Box {
        DropdownButton(multi.text, open, enabled, width) { open = true }
        if (open) {
            NlPopup(onDismiss = { open = false }, alignment = Alignment.TopEnd, offset = IntOffset(0, 34)) {
                Column(Modifier.width(width).verticalScroll(rememberScrollState())) {
                    multi.options.forEach { option ->
                        OptionRow(option, multi.selected.contains(option)) { multi.click(option) }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Chips                                                              */
/* ------------------------------------------------------------------ */

@Composable
fun NlChip(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val t by animateFloatAsState(if (selected) 1f else 0f, tween(200), label = "chip")
    Box(
        Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(lerp(Color.White.copy(alpha = 0.04f), Nl.Accent.copy(alpha = 0.20f), t))
            .border(1.dp, lerp(Nl.Stroke, Nl.Accent.copy(alpha = 0.6f), t), RoundedCornerShape(9.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (!enabled) Nl.Disabled else if (selected) Nl.Text else Nl.TextDim,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

/* ------------------------------------------------------------------ */
/*  Animated popup container (blurs the menu behind it)                */
/* ------------------------------------------------------------------ */

@Composable
fun NlPopup(
    onDismiss: () -> Unit,
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset(0, 0),
    content: @Composable () -> Unit,
) {
    val store = Store.menu
    DisposableEffect(Unit) {
        store.popupDepth++
        onDispose { store.popupDepth-- }
    }

    var shown by remember { mutableStateOf(false) }
    val t by animateFloatAsState(if (shown) 1f else 0f, tween(170), label = "popup")
    DisposableEffect(Unit) {
        shown = true
        onDispose { }
    }

    Popup(
        alignment = alignment,
        offset = offset,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true),
    ) {
        Box(
            Modifier
                .graphicsLayer {
                    alpha = t
                    scaleX = 0.94f + 0.06f * t
                    scaleY = 0.94f + 0.06f * t
                    translationY = (1f - t) * -10f
                }
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xF21A1E26))
                .border(1.dp, Nl.Stroke, RoundedCornerShape(12.dp))
                .padding(6.dp),
        ) {
            content()
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Gesture helper: tap vs vertical drag                               */
/* ------------------------------------------------------------------ */

fun Modifier.verticalTapOrDrag(
    key: Any?,
    onTap: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = this.pointerInput(key) {
    awaitEachGesture {
        val down = awaitFirstDown()
        var total = 0f
        var dragging = false
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            val dy = change.positionChange().y
            total += dy
            if (!dragging && kotlin.math.abs(total) > 8f) {
                dragging = true
                onDragStart()
            }
            if (dragging) {
                onDrag(dy)
                change.consume()
            }
        }
        if (dragging) onDragEnd() else onTap()
    }
}

/** Uniform row separator list helper. */
@Composable
fun NlRows(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) { content() }
}
