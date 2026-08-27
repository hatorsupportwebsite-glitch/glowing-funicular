package com.nlshowcase.overlay.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlshowcase.overlay.state.Store

/** Config (preset) manager — "+" creates a preset called "Unnamed". */
@Composable
fun PresetsPanel() {
    val store = Store.menu
    var query by remember { mutableStateOf("") }
    var ascending by remember { mutableStateOf(true) }
    var renaming by remember { mutableStateOf(-1) }

    Column(Modifier.width(320.dp)) {

        // header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Presets", color = Nl.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            IconButtonSmall(Icons.Filled.FileUpload, "Import") { }
            IconButtonSmall(Icons.Filled.DeleteOutline, "Trash") { }
            IconButtonSmall(Icons.Filled.Add, "Create") {
                val index = store.createPreset()
                store.activeIndex = index
                renaming = index
            }
        }

        // search + sort
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Nl.Field)
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, null, tint = Nl.TextFaint, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(7.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Search", color = Nl.TextFaint, fontSize = 12.sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Nl.Text, fontSize = 12.sp),
                        cursorBrush = SolidColor(Nl.Accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            IconButtonSmall(Icons.Filled.SwapVert, "Sort") { ascending = !ascending }
        }

        Spacer(Modifier.height(4.dp))

        val items = store.presets.withIndex()
            .filter { it.value.name.contains(query, ignoreCase = true) }
            .sortedBy { if (ascending) it.value.name.lowercase() else "" }

        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { entry ->
                PresetRow(
                    index = entry.index,
                    renaming = renaming == entry.index,
                    onRenameDone = { renaming = -1 },
                    onStartRename = { renaming = entry.index },
                )
            }
        }
    }
}

@Composable
private fun PresetRow(
    index: Int,
    renaming: Boolean,
    onRenameDone: () -> Unit,
    onStartRename: () -> Unit,
) {
    val store = Store.menu
    val config = store.presets.getOrNull(index) ?: return
    val active = store.activeIndex == index
    val t by animateFloatAsState(if (active) 1f else 0f, tween(200), label = "preset")
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.04f + 0.03f * t))
            .border(
                width = 1.dp,
                color = Nl.Accent.copy(alpha = 0.85f * t),
                shape = RoundedCornerShape(10.dp),
            )
            .clickable { store.activeIndex = index }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // checkbox
        Box(
            Modifier
                .size(17.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (config.checked) Nl.Green else Color.White.copy(alpha = 0.08f))
                .clickable { config.checked = !config.checked },
            contentAlignment = Alignment.Center,
        ) {
            if (config.checked) {
                Icon(Icons.Filled.Check, null, tint = Color(0xFF0B0D11), modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.width(9.dp))

        if (renaming) {
            BasicTextField(
                value = config.name,
                onValueChange = { config.name = it },
                singleLine = true,
                textStyle = TextStyle(color = Nl.Text, fontSize = 12.5.sp),
                cursorBrush = SolidColor(Nl.Accent),
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Filled.Check,
                null,
                tint = Nl.Green,
                modifier = Modifier.size(16.dp).clickable { onRenameDone() },
            )
        } else {
            Text(
                text = config.name,
                color = if (active) Nl.Text else Nl.TextDim,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).clickable { onStartRename() },
            )
            Icon(
                Icons.Filled.Save,
                null,
                tint = Nl.TextFaint,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(8.dp))
            Box {
                Icon(
                    Icons.Filled.MoreHoriz,
                    null,
                    tint = if (menuOpen) Nl.Text else Nl.TextFaint,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { menuOpen = true },
                )
                if (menuOpen) {
                    NlPopup(
                        onDismiss = { menuOpen = false },
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(0, 24),
                    ) {
                        Column(Modifier.width(150.dp)) {
                            MenuAction(Icons.Filled.Refresh, "Reload") { menuOpen = false }
                            MenuAction(Icons.Filled.Share, "Share") { menuOpen = false }
                            MenuAction(Icons.Filled.ContentCopy, "Duplicate") {
                                store.duplicate(index)
                                menuOpen = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable { onClick() }
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Nl.TextDim, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, color = Nl.Text, fontSize = 12.sp)
    }
}

@Composable
private fun IconButtonSmall(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = Nl.TextDim,
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(6.dp),
    )
}
