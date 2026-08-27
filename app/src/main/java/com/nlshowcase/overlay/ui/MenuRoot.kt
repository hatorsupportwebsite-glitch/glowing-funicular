package com.nlshowcase.overlay.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlshowcase.overlay.state.Section
import com.nlshowcase.overlay.state.Store
import com.nlshowcase.overlay.ui.tabs.AimbotTab
import com.nlshowcase.overlay.ui.tabs.MiscTab
import com.nlshowcase.overlay.ui.tabs.PlayersTab
import com.nlshowcase.overlay.ui.tabs.WorldTab

const val BRAND_GAME = "Standoff 2"
const val BRAND_USER = "t.me/NeverloseCome"

@Composable
fun MenuRoot(onClose: () -> Unit) {
    val store = Store.menu

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Nl.Panel)
            .border(1.dp, Nl.Stroke, RoundedCornerShape(18.dp)),
    ) {
        val compact = maxWidth < 640.dp

        Row(Modifier.fillMaxSize()) {
            Sidebar(compact = compact)
            Column(Modifier.fillMaxSize()) {
                TopBar(compact = compact, onClose = onClose)
                Box(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    when (store.section) {
                        Section.AIMBOT -> AimbotTab(compact)
                        Section.VISUALS ->
                            if (store.visualsTab == "World") WorldTab(compact) else PlayersTab(compact)
                        Section.MISC -> MiscTab(compact)
                        Section.INVENTORY -> Unit // unavailable
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Sidebar                                                            */
/* ------------------------------------------------------------------ */

@Composable
private fun Sidebar(compact: Boolean) {
    val store = Store.menu
    val width by animateDpAsState(if (compact) 62.dp else 196.dp, tween(220), label = "sidebar")

    Column(
        Modifier
            .width(width)
            .fillMaxHeight()
            .background(Nl.Sidebar)
            .padding(vertical = 12.dp, horizontal = 8.dp),
    ) {
        // logo
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(Nl.Accent, Color(0xFF9B6BF6))),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("N", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            if (!compact) {
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("Neverlose", color = Nl.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(BRAND_GAME, color = Nl.TextFaint, fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        NavItem(Icons.Filled.GpsFixed, "Aimbot", compact, store.section == Section.AIMBOT) {
            store.section = Section.AIMBOT
        }
        NavItem(Icons.Filled.Image, "Visuals", compact, store.section == Section.VISUALS) {
            store.section = Section.VISUALS
        }
        NavItem(
            icon = Icons.Filled.Layers,
            label = "Inventory",
            compact = compact,
            selected = false,
            locked = true,
            onClick = {},
        )
        NavItem(Icons.Filled.Tune, "Miscellaneous", compact, store.section == Section.MISC) {
            store.section = Section.MISC
        }

        Spacer(Modifier.weight(1f))

        ProfileFooter(compact)
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    compact: Boolean,
    selected: Boolean,
    locked: Boolean = false,
    onClick: () -> Unit,
) {
    val t by animateFloatAsState(if (selected) 1f else 0f, tween(200), label = "nav")
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.07f * t))
            .clickable(enabled = !locked) { onClick() }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (locked) Icons.Filled.Lock else icon,
            contentDescription = null,
            tint = when {
                locked -> Nl.Disabled
                selected -> Nl.Accent
                else -> Nl.TextDim
            },
            modifier = Modifier.size(17.dp),
        )
        if (!compact) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                color = when {
                    locked -> Nl.Disabled
                    selected -> Nl.Text
                    else -> Nl.TextDim
                },
                fontSize = 12.5.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProfileFooter(compact: Boolean) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .clickable { open = true }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1D2430)),
                contentAlignment = Alignment.Center,
            ) {
                Text("NL", color = Nl.TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            if (!compact) {
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = BRAND_USER,
                        color = Nl.Text,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("15 days left", color = Nl.TextFaint, fontSize = 10.sp)
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Nl.TextDim,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (open) {
            NlPopup(
                onDismiss = { open = false },
                alignment = Alignment.BottomStart,
                offset = IntOffset(60, -6),
            ) {
                ProfileMenu()
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Top bar                                                            */
/* ------------------------------------------------------------------ */

@Composable
private fun TopBar(compact: Boolean, onClose: () -> Unit) {
    val store = Store.menu
    var presetsOpen by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (store.section == Section.VISUALS) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NlChip("Players", store.visualsTab == "Players") { store.visualsTab = "Players" }
                NlChip("World", store.visualsTab == "World") { store.visualsTab = "World" }
            }
        } else {
            Text(
                text = when (store.section) {
                    Section.AIMBOT -> "Aimbot"
                    Section.MISC -> "Miscellaneous"
                    else -> ""
                },
                color = Nl.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.weight(1f))

        // active preset chip
        Box(
            Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .clickable { presetsOpen = true }
                .padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
            Text(
                text = store.config.name,
                color = Nl.TextDim,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(if (compact) 90.dp else 150.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Box {
            Icon(
                imageVector = Icons.Filled.CloudQueue,
                contentDescription = "Presets",
                tint = if (presetsOpen) Nl.Accent else Nl.TextDim,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { presetsOpen = true }
                    .padding(6.dp),
            )
            if (presetsOpen) {
                NlPopup(
                    onDismiss = { presetsOpen = false },
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(0, 34),
                ) {
                    PresetsPanel()
                }
            }
        }
    }
}

/** Shared two column content layout. */
@Composable
fun TabColumns(
    compact: Boolean,
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    if (compact) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            left()
            right()
        }
    } else {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) { left() }
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) { right() }
        }
    }
}
