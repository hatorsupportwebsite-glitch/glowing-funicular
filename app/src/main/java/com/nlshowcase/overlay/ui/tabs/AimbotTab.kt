package com.nlshowcase.overlay.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nlshowcase.overlay.state.Config
import com.nlshowcase.overlay.state.Store
import com.nlshowcase.overlay.ui.NlCard
import com.nlshowcase.overlay.ui.NlMultiField
import com.nlshowcase.overlay.ui.NlPickField
import com.nlshowcase.overlay.ui.NlRow
import com.nlshowcase.overlay.ui.NlSlider
import com.nlshowcase.overlay.ui.NlToggle
import com.nlshowcase.overlay.ui.TabColumns

@Composable
fun AimbotTab(compact: Boolean) {
    val c = Store.menu.config

    TabColumns(
        compact = compact,
        left = {
            NlCard("Aimbot") {
                NlRow("Enabled") { NlToggle(c.aimEnabled) }
                NlRow("Prefer") { NlPickField(c.prefer, width = 138.dp) }
                NlRow("Hitboxes") { NlMultiField(c.hitboxes, width = 138.dp) }
                NlRow("Hit Chance", dots = { HitChanceOptions(c) }) { NlSlider(c.hitChance) }
                NlRow("Min. Damage") { NlSlider(c.minDamage) }
                NlRow("Quick Stop") { NlMultiField(c.quickStop, width = 138.dp) }
                NlRow("Quick Scope", dots = { QuickScopeOptions(c) }) { NlToggle(c.quickScope) }
                NlRow("History") { NlPickField(c.history, width = 138.dp) }
                NlRow("Texture Override") { NlMultiField(c.textureOverride, width = 138.dp) }
                NlRow("Hitbox Override") { NlPickField(c.hitboxOverride, width = 138.dp) }
                NlRow("Remove Spread") { NlPickField(c.removeSpread, width = 138.dp) }
                NlRow("Wallshot") { NlToggle(c.wallshot) }
                NlRow("Bullet Teleport") { NlToggle(c.bulletTeleport) }
                NlRow("Rapid Fire", dots = { RapidFireOptions(c) }) { NlToggle(c.rapidFire) }
            }
        },
        right = {
            NlCard("Anti-Aim") {
                NlRow("Enabled") { NlToggle(c.aaEnabled) }
                NlRow("Suppress Breathing Animation") { NlToggle(c.suppressBreathing) }
                NlRow("Leg Movement") { NlPickField(c.legMovement, width = 138.dp) }
                NlRow("Pitch") { NlPickField(c.pitch, width = 138.dp) }
                NlRow("Yaw") { NlPickField(c.yaw, width = 138.dp) }
                NlRow("Mouse Override") { NlToggle(c.mouseOverride) }
            }
        },
    )
}

/** "···" popup of Hit Chance. */
@Composable
private fun HitChanceOptions(c: Config) {
    Column(Modifier.width(252.dp)) {
        NlRow("Force Shot") { NlToggle(c.forceShot) }
        NlRow("Min. Hit Chance") { NlSlider(c.minHitChance, trackWidth = 78.dp) }
        NlRow("Extrapolation Safety") { NlPickField(c.extrapolationSafety, width = 104.dp) }
    }
}

/** "···" popup of Quick Scope. */
@Composable
private fun QuickScopeOptions(c: Config) {
    Column(Modifier.width(252.dp)) {
        NlRow("Only When Scoped") { NlToggle(c.quickScopeOnlyScoped) }
        NlRow("Delay") { NlSlider(c.quickScopeDelay, trackWidth = 78.dp) }
    }
}

/** "···" popup of Rapid Fire. */
@Composable
private fun RapidFireOptions(c: Config) {
    Column(Modifier.width(252.dp)) {
        NlRow("Shots") { NlSlider(c.rapidFireShots, trackWidth = 78.dp) }
        NlRow("On Key") { NlPickField(c.rapidFireKey, width = 104.dp) }
    }
}
