package com.nlshowcase.overlay.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/* ------------------------------------------------------------------ */
/*  Primitive setting holders                                          */
/* ------------------------------------------------------------------ */

class Flag(on: Boolean = false) {
    var on by mutableStateOf(on)
}

/** Single choice. Clicking the active option clears it -> shows "None". */
class Pick(
    val options: List<String>,
    selected: String? = null,
    val clearable: Boolean = true,
    val empty: String = "None",
) {
    var selected by mutableStateOf(selected)

    val text: String get() = selected ?: empty

    fun click(option: String) {
        selected = if (selected == option && clearable) null else option
    }
}

/** Multiple choice. Empty selection shows "None". */
class MultiPick(
    val options: List<String>,
    selected: List<String> = emptyList(),
    val empty: String = "None",
) {
    private val items = mutableStateListOf<String>().also { it.addAll(selected) }

    val selected: List<String> get() = items

    val text: String
        get() = if (items.isEmpty()) empty else options.filter { items.contains(it) }.joinToString(", ")

    fun click(option: String) {
        if (items.contains(option)) items.remove(option) else items.add(option)
    }
}

/** Numeric slider value. */
class Num(
    value: Float,
    val min: Float = 0f,
    val max: Float = 100f,
    val step: Float = 1f,
    val suffix: String = "",
    val decimals: Int = 0,
) {
    var value by mutableFloatStateOf(value)

    val fraction: Float
        get() = if (max <= min) 0f else ((value - min) / (max - min)).coerceIn(0f, 1f)

    fun setFraction(fraction: Float) {
        val raw = min + (max - min) * fraction.coerceIn(0f, 1f)
        val snapped = if (step > 0f) (raw / step).roundToInt() * step else raw
        value = snapped.coerceIn(min, max)
    }

    val text: String
        get() = if (decimals > 0) String.format("%.${decimals}f%s", value, suffix)
        else "${value.roundToInt()}$suffix"
}

/** Colour value used by the colour picker. */
class Tint(color: Color) {
    var color by mutableStateOf(color)
}

/* ------------------------------------------------------------------ */
/*  Draggable ESP element                                              */
/* ------------------------------------------------------------------ */

class EspItem(
    val name: String,
    val group: String,
    x: Float,
    y: Float,
    scale: Float = 1f,
    color: Color = Color.White,
    enabled: Boolean = false,
) {
    /** Position in dp relative to the top-left of the preview area. */
    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)
    var scale by mutableFloatStateOf(scale)
    val tint = Tint(color)
    val flag = Flag(enabled)
}

fun defaultEspItems(): List<EspItem> = listOf(
    // ----- Main -----
    EspItem("Bounding Box", "Main", 0f, 0f, 1f, Color(0xFFFFFFFF), true),
    EspItem("Name", "Main", 92f, -16f, 1f, Color(0xFFFFFFFF), true),
    EspItem("Distance", "Main", 92f, 236f, 1f, Color(0xFFB4C8FF), true),
    EspItem("Health Bar", "Main", 40f, 20f, 1f, Color(0xFF6FE3A1), true),
    EspItem("Ammo Bar", "Main", 60f, 228f, 1f, Color(0xFF7BA4E8), false),
    EspItem("Skeleton", "Main", 110f, 110f, 1f, Color(0xFFFFFFFF), false),
    // ----- Flags -----
    EspItem("Unarmored", "Flags", 178f, 30f, 1f, Color(0xFFFF8A8A), false),
    EspItem("Defuser", "Flags", 178f, 46f, 1f, Color(0xFF6FE3A1), false),
    EspItem("Blind", "Flags", 178f, 62f, 1f, Color(0xFFFFD479), false),
    EspItem("Scoped", "Flags", 178f, 78f, 1f, Color(0xFFB4C8FF), false),
    EspItem("Reload", "Flags", 178f, 94f, 1f, Color(0xFFFFFFFF), false),
    EspItem("Immunity", "Flags", 178f, 110f, 1f, Color(0xFF7BA4E8), false),
    EspItem("Slowed", "Flags", 178f, 126f, 1f, Color(0xFFFFD479), false),
    EspItem("Vulnerable", "Flags", 178f, 142f, 1f, Color(0xFFFF5A96), false),
    EspItem("Bomb", "Flags", 178f, 158f, 1f, Color(0xFFFF5A96), false),
    EspItem("Hostage", "Flags", 178f, 174f, 1f, Color(0xFFFFFFFF), false),
    EspItem("Defuse", "Flags", 178f, 190f, 1f, Color(0xFF6FE3A1), false),
    EspItem("Pin Pulled", "Flags", 178f, 206f, 1f, Color(0xFFFFD479), false),
    EspItem("Money", "Flags", 178f, 222f, 1f, Color(0xFF6FE3A1), false),
    EspItem("Order Priority", "Flags", 178f, 238f, 1f, Color(0xFFC7A6FF), false),
    EspItem("Delay", "Flags", 178f, 254f, 1f, Color(0xFF8A919C), false),
    // ----- Weapon -----
    EspItem("Text", "Weapon", 78f, 252f, 1f, Color(0xFFFFFFFF), true),
    EspItem("Icon", "Weapon", 78f, 270f, 1f, Color(0xFFFFFFFF), false),
    EspItem("Readiness Bar", "Weapon", 60f, 244f, 1f, Color(0xFF7BA4E8), false),
    EspItem("Bomb", "Weapon", 20f, 270f, 1f, Color(0xFFFF5A96), false),
    EspItem("Defuser", "Weapon", 20f, 286f, 1f, Color(0xFF6FE3A1), false),
    EspItem("Taser", "Weapon", 20f, 302f, 1f, Color(0xFFFFD479), false),
    // ----- Aimbot -----
    EspItem("Hit Chance", "Aimbot", 6f, 60f, 1f, Color(0xFFB4C8FF), false),
    EspItem("Hitboxes", "Aimbot", 6f, 76f, 1f, Color(0xFFFF5A96), false),
)

val MODEL_MATERIALS = listOf(
    "Disabled", "Shaded", "Solid", "Glow", "Glow Outline", "Iridescent",
)

val SCALE_OPTIONS = listOf("Auto", "100%", "125%", "150%", "175%", "200%")

val ANIMATIONS = listOf("Idle", "Breathing", "Wave", "Look Around", "Dance")

/* ------------------------------------------------------------------ */
/*  Config (preset)                                                    */
/* ------------------------------------------------------------------ */

class Config(name: String) {
    var name by mutableStateOf(name)
    var checked by mutableStateOf(false)

    // ---------------- Aimbot ----------------
    val aimEnabled = Flag(true)
    val prefer = Pick(listOf("Damage", "Hit Chance", "Safety"), "Hit Chance", clearable = false)
    val hitboxes = MultiPick(
        listOf("Head", "Chest", "Stomach", "Arms", "Legs", "Feet"),
        listOf("Head", "Chest"),
    )
    val hitChance = Num(70f, 0f, 100f, 1f, "%")
    val forceShot = Flag(false)
    val minHitChance = Num(45f, 0f, 100f, 1f, "%")
    val extrapolationSafety = Pick(listOf("Off", "Low", "High"), "Low", clearable = false)
    val minDamage = Num(25f, 0f, 130f, 1f)
    val quickStop = MultiPick(listOf("In Air", "Early", "Slow Motion"), listOf("Early"))
    val quickScope = Flag(true)
    val quickScopeOnlyScoped = Flag(false)
    val quickScopeDelay = Num(120f, 0f, 400f, 10f, " ms")
    val history = Pick(listOf("Low", "Medium", "High", "Maximum"), "Medium")
    val textureOverride = MultiPick(
        listOf("Lighting", "Explosion", "Grass", "Blood", "Molotov"),
        listOf("Lighting"),
    )
    val hitboxOverride = Pick(listOf("Head", "Stomach", "Torso", "Arms", "Legs"), "Head")
    val removeSpread = Pick(listOf("Off", "Seed Check", "Full"), "Seed Check", clearable = false)
    val wallshot = Flag(true)
    val bulletTeleport = Flag(false)
    val rapidFire = Flag(true)
    val rapidFireShots = Num(3f, 1f, 6f, 1f)
    val rapidFireKey = Pick(listOf("Off", "On Key", "Always"), "On Key", clearable = false)

    // ---------------- Anti-Aim ----------------
    val aaEnabled = Flag(true)
    val suppressBreathing = Flag(true)
    val legMovement = Pick(listOf("Off", "Sliding", "Walking", "Running"), "Sliding", clearable = false)
    val pitch = Pick(listOf("Off", "Down", "Up", "Zero", "Jitter"), "Down", clearable = false)
    val yaw = Pick(listOf("Off", "Backwards", "Sideways", "Spin", "Jitter"), "Backwards", clearable = false)
    val mouseOverride = Flag(false)

    // ---------------- Players ----------------
    val enemyEnabled = Flag(true)
    val enemyColor = Tint(Color(0xFFFF5A96))
    val flagsSide = Pick(listOf("Left", "Right"), "Left", clearable = false)
    val outOfViewArrows = Flag(true)
    val enemyMaterial = Pick(MODEL_MATERIALS, "Glow", clearable = false)
    val enemyMaterialColor = Tint(Color(0xFF7BA4E8))
    val enemyOcclusion = Pick(listOf("Disabled", "Fresnel", "Chams"), "Fresnel", clearable = false)

    val teammateEnabled = Flag(false)
    val teammateColor = Tint(Color(0xFF6FE3A1))
    val teammateMaterial = Pick(MODEL_MATERIALS, "Shaded", clearable = false)
    val teammateMaterialColor = Tint(Color(0xFF6FE3A1))

    val localMaterial = Pick(MODEL_MATERIALS, "Disabled", clearable = false)
    val localMaterialColor = Tint(Color(0xFFC7A6FF))

    // ---------------- 3D model preview ----------------
    val animation = Pick(ANIMATIONS, "Idle", clearable = false)
    val autoRotate = Flag(true)
    val modelScale = Num(100f, 60f, 160f, 5f, "%")

    val espItems = mutableStateListOf<EspItem>().also { it.addAll(defaultEspItems()) }

    // ---------------- Miscellaneous ----------------
    val bunnyHop = Flag(true)
    val airStrafe = Flag(true)
    val moveBeforeTimer = Flag(false)
    val noclip = Flag(false)
    val standaloneQuickStop = Flag(false)
    val strafeAssist = Flag(true)
    val fly = Flag(false)
    val godMode = Flag(false)
    val invisible = Flag(false)

    val instaSwitch = Flag(true)
    val teleport = Flag(false)
    val knifeBot = Flag(false)
    val preventAfkKick = Flag(false)
    val hitSound = Pick(listOf("Off", "Bell", "Metal", "Skeet"), "Bell", clearable = false)
    val freezeTime = Flag(false)
    val autoGrenadeRelease = Flag(false)
    val autoAccept = Flag(true)
    val logEvents = MultiPick(
        listOf(
            "Damage Dealt", "Damage Received", "Missed Shots",
            "Purchases", "Votes", "Lua Output",
        ),
        listOf("Damage Dealt", "Missed Shots"),
    )
    val logColor = Tint(Color(0xD2B4C8FF))
}

/* ------------------------------------------------------------------ */
/*  Profile / global settings                                          */
/* ------------------------------------------------------------------ */

class ProfileState {
    val language = Pick(listOf("English", "Russian", "Chinese", "Portuguese"), "English", clearable = false)
    val menuScale = Pick(SCALE_OPTIONS, "Auto", clearable = false)
    val espScale = Pick(SCALE_OPTIONS, "125%", clearable = false)
    val windowsScale = Pick(SCALE_OPTIONS, "100%", clearable = false)
    val units = Pick(listOf("Auto", "Metric", "Imperial"), "Auto", clearable = false)
    val styleColor = Tint(Color(0xFF7BA4E8))
    val safeMode = Pick(listOf("Disabled", "Automatic", "Forced On"), "Disabled", clearable = false)
    val synchronization = Flag(true)
}

enum class Section { AIMBOT, VISUALS, INVENTORY, MISC }

class MenuStore {
    val presets = mutableStateListOf(
        Config("MAFIA"),
        Config("vitma Community HvBH Conf"),
        Config("Unnamed"),
    )
    var activeIndex by mutableIntStateOf(0)

    val config: Config
        get() = presets.getOrNull(activeIndex) ?: presets.first()

    val profile = ProfileState()

    var section by mutableStateOf(Section.AIMBOT)
    var visualsTab by mutableStateOf("Players")
    var customizationOpen by mutableStateOf(false)
    var selectedEspItem by mutableStateOf<EspItem?>(null)

    /** > 0 while a popup is open, used to blur the menu behind it. */
    var popupDepth by mutableIntStateOf(0)

    fun createPreset(): Int {
        presets.add(Config("Unnamed"))
        return presets.lastIndex
    }

    fun duplicate(index: Int) {
        val source = presets.getOrNull(index) ?: return
        presets.add(index + 1, Config(source.name + " (copy)"))
    }

    fun remove(index: Int) {
        if (presets.size <= 1) return
        presets.removeAt(index)
        if (activeIndex >= presets.size) activeIndex = presets.lastIndex
    }
}

object Store {
    val menu = MenuStore()
}
