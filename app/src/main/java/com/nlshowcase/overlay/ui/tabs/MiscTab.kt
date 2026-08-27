package com.nlshowcase.overlay.ui.tabs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nlshowcase.overlay.state.Store
import com.nlshowcase.overlay.ui.NlCard
import com.nlshowcase.overlay.ui.NlMultiField
import com.nlshowcase.overlay.ui.NlPickField
import com.nlshowcase.overlay.ui.NlRow
import com.nlshowcase.overlay.ui.NlSwatch
import com.nlshowcase.overlay.ui.NlToggle
import com.nlshowcase.overlay.ui.TabColumns

@Composable
fun MiscTab(compact: Boolean) {
    val c = Store.menu.config

    TabColumns(
        compact = compact,
        left = {
            NlCard("Movement") {
                NlRow("Bunny Hop") { NlToggle(c.bunnyHop) }
                NlRow("Air Strafe") { NlToggle(c.airStrafe) }
                NlRow("Move Before Timer") { NlToggle(c.moveBeforeTimer) }
                NlRow("Noclip") { NlToggle(c.noclip) }
                NlRow("Standalone Quick Stop") { NlToggle(c.standaloneQuickStop) }
                NlRow("Strafe Assist") { NlToggle(c.strafeAssist) }
                NlRow("Fly") { NlToggle(c.fly) }
                NlRow("God Mode") { NlToggle(c.godMode) }
                NlRow("Invisible") { NlToggle(c.invisible) }
            }
        },
        right = {
            NlCard("Features") {
                NlRow("Insta Switch") { NlToggle(c.instaSwitch) }
                NlRow("Teleport") { NlToggle(c.teleport) }
                NlRow("Knife Bot", enabled = false) { NlToggle(c.knifeBot, enabled = false) }
                NlRow("Prevent AFK Kick") { NlToggle(c.preventAfkKick) }
                NlRow("Hit Sound") { NlPickField(c.hitSound, width = 138.dp) }
                NlRow("Freeze Time") { NlToggle(c.freezeTime) }
                NlRow("Automatic Grenade Release") { NlToggle(c.autoGrenadeRelease) }
                NlRow("Auto-Accept Matchmaking") { NlToggle(c.autoAccept) }
                NlRow("Log Events") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NlMultiField(c.logEvents, width = 138.dp)
                        Spacer(Modifier.width(9.dp))
                        NlSwatch(c.logColor)
                    }
                }
            }
        },
    )
}
