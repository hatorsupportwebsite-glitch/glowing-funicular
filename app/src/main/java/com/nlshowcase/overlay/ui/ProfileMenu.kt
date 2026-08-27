package com.nlshowcase.overlay.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Web
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlshowcase.overlay.state.Pick
import com.nlshowcase.overlay.state.Store

const val TELEGRAM_URL = "https://t.me/NeverloseCome"

@Composable
fun ProfileMenu() {
    val profile = Store.menu.profile
    val context = LocalContext.current

    fun openTelegram() {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    Column(Modifier.width(282.dp)) {

        // ---- account header ----
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1D2430)),
                contentAlignment = Alignment.Center,
            ) {
                Text("NL", color = Nl.TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = BRAND_USER,
                    color = Nl.Text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("Till: September 11 2026", color = Nl.TextDim, fontSize = 11.sp)
                Text(
                    text = "Renew",
                    color = Nl.AccentSoft,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { openTelegram() },
                )
            }
        }

        Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Nl.Divider))
        Spacer(Modifier.height(4.dp))

        PickRow(Icons.Filled.Language, "Language", profile.language)
        PickRow(Icons.Filled.Height, "Menu Scale", profile.menuScale)
        PickRow(Icons.Filled.FormatSize, "ESP Scale", profile.espScale)
        PickRow(Icons.Filled.Web, "Windows Scale", profile.windowsScale)
        PickRow(Icons.Filled.Straighten, "Units", profile.units)

        // ---- style: gear + colour ----
        Row(
            Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(9.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Palette, null, tint = Nl.TextDim, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text("Style", color = Nl.Text, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Settings, null, tint = Nl.TextDim, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(10.dp))
            NlSwatch(profile.styleColor, showAlpha = false, swatchSize = 17.dp)
        }

        PickRow(Icons.Filled.Shield, "Safe Mode", profile.safeMode)

        // ---- synchronization ----
        Row(
            Modifier
                .fillMaxWidth()
                .height(38.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Cast, null, tint = Nl.TextDim, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text("Synchronization", color = Nl.Text, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
            NlToggle(profile.synchronization)
        }

        Spacer(Modifier.height(2.dp))
        Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Nl.Divider))
        Spacer(Modifier.height(2.dp))

        // ---- About -> telegram link (Chat row removed) ----
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(9.dp))
                .clickable { openTelegram() }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Info, null, tint = Nl.TextDim, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = BRAND_USER,
                color = Nl.Text,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PickRow(icon: ImageVector, label: String, pick: Pick) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (open) Color.White.copy(alpha = 0.06f) else Color.Transparent)
                .clickable { open = true }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = Nl.TextDim, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = Nl.Text, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
            Text(pick.text, color = Nl.TextDim, fontSize = 12.sp)
            Icon(
                Icons.Filled.KeyboardArrowRight,
                null,
                tint = Nl.TextDim,
                modifier = Modifier.size(16.dp),
            )
        }
        if (open) {
            NlPopup(
                onDismiss = { open = false },
                alignment = Alignment.TopEnd,
                offset = IntOffset(150, 0),
            ) {
                Column(Modifier.width(132.dp)) {
                    pick.options.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    if (pick.selected == option) Color.White.copy(alpha = 0.07f)
                                    else Color.Transparent,
                                )
                                .clickable {
                                    pick.click(option)
                                    open = false
                                }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = option,
                                color = if (pick.selected == option) Nl.Text else Nl.TextDim,
                                fontSize = 12.sp,
                                fontWeight = if (pick.selected == option) FontWeight.Medium else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}
