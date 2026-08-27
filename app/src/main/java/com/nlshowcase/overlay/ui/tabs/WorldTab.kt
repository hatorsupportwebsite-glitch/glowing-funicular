package com.nlshowcase.overlay.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlshowcase.overlay.ui.Nl
import com.nlshowcase.overlay.ui.NlCard
import com.nlshowcase.overlay.ui.TabColumns

/**
 * World tab — visual placeholder only, nothing here is interactive
 * (exactly like the reference screenshots).
 */
@Composable
fun WorldTab(compact: Boolean) {
    TabColumns(
        compact = compact,
        left = {
            NlCard("View") {
                StubDropdown("View Options", "Third Person, Fo...")
                StubDropdown("Scope Options", "Remove Scope O...")
                StubDropdown("Viewmodel Options", "Custom FOV, Off...")
                StubDropdown("Perspective Options", "Camera Offset")
                StubDropdown("Unlock Spectating", "Perspective, Enem...")
                StubDropdown("Visual Recoil", "No Shake, No Rec...")
            }
            NlCard("World ESP") {
                StubToggle("Bomb", true)
                StubToggle("Weapons", true)
                StubToggle("Grenades", false)
                StubToggle("Grenade Trajectory", false)
                StubToggle("Grenade Proximity Warnings", false)
            }
        },
        right = {
            NlCard("HUD") {
                StubDropdown("Radar", "Reveal Enemies, F...")
                StubDropdown("Scope Overlay", "Lines")
                StubToggle("Inaccuracy Overlay", true)
                StubToggle("Death Notices", false)
                StubToggle("Scoreboard", true)
                StubDropdown("Crosshairs", "Static, Dynamic")
            }
            NlCard("Miscellaneous") {
                StubDropdown("Windows", "Keybinds, Spectat...")
                StubDropdown("Removals", "Flash, Smoke, Fog")
                StubDropdown("Ambience", "Night Mode")
                StubToggle("Hit Marker", true)
                StubToggle("Bullet Tracers", false)
                StubToggle("Bullet Impacts", false)
            }
        },
    )
}

@Composable
private fun StubRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Nl.RowHeight)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Nl.Text,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
private fun StubDropdown(label: String, value: String) {
    StubRow(label) {
        Row(
            Modifier
                .width(146.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(Nl.FieldRadius))
                .background(Nl.Field)
                .padding(start = 10.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                color = Nl.TextDim,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Nl.TextFaint,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun StubToggle(label: String, on: Boolean) {
    StubRow(label) {
        Box(
            Modifier
                .size(width = 38.dp, height = 21.dp)
                .clip(RoundedCornerShape(50))
                .background(if (on) Nl.Accent.copy(alpha = 0.75f) else Nl.ToggleOff),
            contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Spacer(
                Modifier
                    .padding(horizontal = 2.dp)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
            )
        }
    }
}
