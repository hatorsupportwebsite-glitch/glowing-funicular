package com.nlshowcase.overlay.ui.tabs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.nlshowcase.overlay.gl.CharacterScene
import com.nlshowcase.overlay.gl.GLTextureView
import com.nlshowcase.overlay.state.EspItem
import com.nlshowcase.overlay.state.Store
import com.nlshowcase.overlay.ui.ColorPickerBody
import com.nlshowcase.overlay.ui.Nl
import com.nlshowcase.overlay.ui.NlCard
import com.nlshowcase.overlay.ui.NlChip
import com.nlshowcase.overlay.ui.NlPickField
import com.nlshowcase.overlay.ui.NlPopup
import com.nlshowcase.overlay.ui.NlRow
import com.nlshowcase.overlay.ui.NlSlider
import com.nlshowcase.overlay.ui.NlSwatch
import com.nlshowcase.overlay.ui.NlToggle
import com.nlshowcase.overlay.ui.TabColumns

@Composable
fun PlayersTab(compact: Boolean) {
    val c = Store.menu.config

    TabColumns(
        compact = compact,
        left = {
            EspPreviewCard()
            NlCard("3D Model") {
                NlRow("Animation") { NlPickField(c.animation, width = 138.dp) }
                NlRow("Auto Rotate") { NlToggle(c.autoRotate) }
                NlRow("Scale") { NlSlider(c.modelScale) }
            }
        },
        right = {
            NlCard("Enemy") {
                NlRow("Enabled") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NlToggle(c.enemyEnabled)
                        Spacer(Modifier.width(9.dp))
                        NlSwatch(c.enemyColor)
                    }
                }
                NlRow("Flags Side") { NlPickField(c.flagsSide, width = 120.dp) }
                NlRow("Out of View Arrows") { NlToggle(c.outOfViewArrows) }
            }
            NlCard("Enemy Model") {
                NlRow("Material") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NlPickField(c.enemyMaterial, width = 132.dp)
                        Spacer(Modifier.width(9.dp))
                        NlSwatch(c.enemyMaterialColor)
                    }
                }
                NlRow("Occlusion") { NlPickField(c.enemyOcclusion, width = 132.dp) }
            }
            NlCard("Teammate") {
                NlRow("Enabled") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NlToggle(c.teammateEnabled)
                        Spacer(Modifier.width(9.dp))
                        NlSwatch(c.teammateColor)
                    }
                }
            }
            NlCard("Teammate Model") {
                NlRow("Material") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NlPickField(c.teammateMaterial, width = 132.dp)
                        Spacer(Modifier.width(9.dp))
                        NlSwatch(c.teammateMaterialColor)
                    }
                }
            }
            NlCard("Local Player Model") {
                NlRow("Material") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NlPickField(c.localMaterial, width = 132.dp)
                        Spacer(Modifier.width(9.dp))
                        NlSwatch(c.localMaterialColor)
                    }
                }
            }
        },
    )
}

/* ------------------------------------------------------------------ */
/*  ESP preview: real animated 3D model + draggable ESP elements        */
/* ------------------------------------------------------------------ */

@Composable
private fun EspPreviewCard() {
    val store = Store.menu
    val c = store.config
    val density = LocalDensity.current
    var dragYaw by remember { mutableStateOf(0f) }

    val scene = remember { CharacterScene() }
    SideEffect {
        scene.animation = c.animation.selected ?: "Idle"
        scene.autoRotate = c.autoRotate.on
        scene.modelScale = c.modelScale.value / 100f
        scene.material = c.enemyMaterial.selected ?: "Glow"
        scene.userYaw = dragYaw
        val glow = c.enemyMaterialColor.color
        scene.glow = floatArrayOf(glow.red, glow.green, glow.blue)
    }

    NlCard("ESP Preview") {
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF141922), Color(0xFF0C0F15)),
                        ),
                    )
                    .border(1.dp, Nl.Stroke, RoundedCornerShape(12.dp)),
            ) {
                // --- real 3D model, rendered with OpenGL ES on its own thread ---
                AndroidView(
                    factory = { context -> GLTextureView(context, scene) },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                dragYaw += dragAmount.x * 0.4f
                                scene.userYaw = dragYaw
                            }
                        },
                )

                // --- ESP elements on top of the model ---
                c.espItems.forEach { item ->
                    if (item.flag.on) {
                        EspElement(item)
                    }
                }

                // hint like in the reference
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.Black.copy(alpha = 0.42f))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Hold to move the item", color = Nl.TextDim, fontSize = 10.sp)
                    Text("Tap to customize the item", color = Nl.TextDim, fontSize = 10.sp)
                    Text("Pinch / slider to resize", color = Nl.TextFaint, fontSize = 10.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // --- Customization button: opens the ESP ITEMS panel ---
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (store.customizationOpen) Nl.Accent.copy(alpha = 0.18f)
                        else Color.White.copy(alpha = 0.05f),
                    )
                    .border(
                        1.dp,
                        if (store.customizationOpen) Nl.Accent.copy(alpha = 0.55f) else Nl.Stroke,
                        RoundedCornerShape(11.dp),
                    )
                    .clickable { store.customizationOpen = !store.customizationOpen }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = if (store.customizationOpen) Nl.Accent else Nl.TextDim,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    "Customization",
                    color = Nl.Text,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${c.espItems.count { it.flag.on }} / ${c.espItems.size}",
                    color = Nl.TextFaint,
                    fontSize = 11.sp,
                )
            }

            // --- ESP ITEMS ---
            val open = store.customizationOpen
            val t by animateFloatAsState(if (open) 1f else 0f, tween(220), label = "custom")
            if (t > 0.01f) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = t
                            translationY = (1f - t) * -12f * density.density
                        },
                ) {
                    Spacer(Modifier.height(10.dp))
                    EspItemsPanel()
                }
            }
        }
    }
}

@Composable
private fun EspElement(item: EspItem) {
    val density = LocalDensity.current
    var customizing by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }

    Box(
        Modifier
            .offset { IntOffset(item.x.toInt(), item.y.toInt()) }
            .pointerInput(item) {
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false },
                ) { _, dragAmount ->
                    item.x += dragAmount.x
                    item.y += dragAmount.y
                }
            }
            .clickable { customizing = true },
    ) {
        val label = item.name
        val color = item.tint.color

        when (label) {
            "Bounding Box" -> Box(
                Modifier
                    .size(width = (150 * item.scale).dp, height = (300 * item.scale).dp)
                    .border(1.5.dp, color, RoundedCornerShape(2.dp)),
            )
            "Health Bar" -> Box(
                Modifier
                    .size(width = 5.dp, height = (300 * item.scale).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.78f)
                        .clip(RoundedCornerShape(50))
                        .background(color),
                )
            }
            "Ammo Bar", "Readiness Bar" -> Box(
                Modifier
                    .size(width = (120 * item.scale).dp, height = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.62f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(color),
                )
            }
            else -> Text(
                text = when (label) {
                    "Name" -> "neverlose.cc"
                    "Distance" -> "12m"
                    "Text" -> "AK-47"
                    "Icon" -> "🔪"
                    "Hit Chance" -> "HC 84%"
                    "Hitboxes" -> "HEAD"
                    else -> label
                },
                color = color,
                fontSize = (10 * item.scale).sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(if (dragging) 0.75f else 1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.34f))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }

        if (customizing) {
            NlPopup(
                onDismiss = { customizing = false },
                alignment = Alignment.TopStart,
                offset = IntOffset(0, 20),
            ) {
                Column(Modifier.width(266.dp)) {
                    ColorPickerBody(item.tint, showAlpha = true, chipLabel = item.name)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 36.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Tune, null, tint = Nl.TextDim, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Size", color = Nl.Text, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(
                            text = "${(item.scale * 100).toInt()}%",
                            color = Nl.TextDim,
                            fontSize = 11.sp,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.75f, 1f, 1.25f, 1.5f).forEach { value ->
                            NlChip("${(value * 100).toInt()}%", item.scale == value) {
                                item.scale = value
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EspItemsPanel() {
    val c = Store.menu.config
    val groups = listOf("Main", "Flags", "Weapon", "Aimbot")
    var group by remember { mutableStateOf("Main") }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, Nl.Stroke, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            "ESP ITEMS",
            color = Nl.TextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            groups.forEach { name ->
                NlChip(name, group == name) { group = name }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            c.espItems.filter { it.group == group }.forEach { item ->
                NlRow(item.name) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NlSwatch(item.tint, chipLabel = item.name)
                        Spacer(Modifier.width(9.dp))
                        NlToggle(item.flag)
                    }
                }
            }
        }
        Text(
            text = "Hold an element on the model to move it · tap it to customize",
            color = Nl.TextFaint,
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
