package com.nlshowcase.overlay.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Design tokens copied from the reference UI. */
object Nl {
    val Bg = Color(0xFF0B0D11)
    val Panel = Color(0xE60F1218)        // translucent — real blur comes from the window
    val PanelSolid = Color(0xFF0F1218)
    val Sidebar = Color(0xD9101319)
    val Card = Color(0xCC161A21)
    val CardTop = Color(0xCC1B2029)
    val Field = Color(0xFF232833)
    val FieldHover = Color(0xFF2A303C)
    val Divider = Color(0x1FFFFFFF)
    val Stroke = Color(0x14FFFFFF)

    val Text = Color(0xFFE7E9EE)
    val TextDim = Color(0xFF9BA3AF)
    val TextFaint = Color(0xFF6B7480)
    val Disabled = Color(0xFF4C5361)

    val Accent = Color(0xFF3B82F6)
    val AccentSoft = Color(0xFF7BA4E8)
    val Green = Color(0xFF4FD37E)
    val Pink = Color(0xFFFF5A96)
    val ToggleOff = Color(0xFF39404C)

    val CardRadius = 14.dp
    val FieldRadius = 9.dp
    val RowHeight = 40.dp
}
