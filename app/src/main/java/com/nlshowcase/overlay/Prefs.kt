package com.nlshowcase.overlay

import android.content.Context

/** Simple SharedPreferences wrapper for the launcher settings. */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("nl_showcase", Context.MODE_PRIVATE)

    /** Delay (seconds) between service start and the menu sliding up. */
    var startDelaySec: Int
        get() = sp.getInt("start_delay", 3)
        set(value) = sp.edit().putInt("start_delay", value).apply()

    /** Open the menu automatically after the delay. */
    var openMenuOnStart: Boolean
        get() = sp.getBoolean("open_on_start", true)
        set(value) = sp.edit().putBoolean("open_on_start", value).apply()

    /** Auto-hide the menu after N seconds (0 = never). */
    var autoHideSec: Int
        get() = sp.getInt("auto_hide", 0)
        set(value) = sp.edit().putInt("auto_hide", value).apply()

    var menuScale: Float
        get() = sp.getFloat("menu_scale", 1f)
        set(value) = sp.edit().putFloat("menu_scale", value).apply()

    var blurEnabled: Boolean
        get() = sp.getBoolean("blur", true)
        set(value) = sp.edit().putBoolean("blur", value).apply()

    var blurRadius: Int
        get() = sp.getInt("blur_radius", 34)
        set(value) = sp.edit().putInt("blur_radius", value).apply()

    var homeLineVisible: Boolean
        get() = sp.getBoolean("home_line", true)
        set(value) = sp.edit().putBoolean("home_line", value).apply()

    var startOnBoot: Boolean
        get() = sp.getBoolean("on_boot", false)
        set(value) = sp.edit().putBoolean("on_boot", value).apply()

    var model3dEnabled: Boolean
        get() = sp.getBoolean("model3d", true)
        set(value) = sp.edit().putBoolean("model3d", value).apply()

    var animationSpeed: Float
        get() = sp.getFloat("anim_speed", 1f)
        set(value) = sp.edit().putFloat("anim_speed", value).apply()
}
