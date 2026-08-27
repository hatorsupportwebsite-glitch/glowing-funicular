package com.nlshowcase.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.nlshowcase.overlay.ui.OverlayController
import com.nlshowcase.overlay.ui.OverlayRoot

/**
 * Foreground service that hosts the Compose overlay in a system window.
 *
 * Collapsed the window is only a thin strip at the bottom of the screen (so the
 * game keeps receiving touches) with the white iPhone-style home line. Tapping
 * or swiping that line expands the window to full screen and animates the menu
 * in, with a real system blur behind it on Android 12+.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val controller = OverlayController()
    private val handler = Handler(Looper.getMainLooper())

    private var owner: OverlayViewOwner? = null
    private var root: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null
    private var lastBlur = -1
    private var expanded = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        isRunning = true
        addOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE -> controller.open = !controller.open
            else -> scheduleAutoOpen()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        root?.let { view ->
            runCatching { windowManager.removeViewImmediate(view) }
            view.disposeComposition()
        }
        root = null
        owner?.destroy()
        owner = null
        isRunning = false
        super.onDestroy()
    }

    // ---------------------------------------------------------------- overlay

    private fun addOverlay() {
        val prefs = Prefs(this)
        val viewOwner = OverlayViewOwner().also { owner = it }

        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = collapsedHeightPx()
            gravity = Gravity.BOTTOM or Gravity.START
            flags = COLLAPSED_FLAGS
            dimAmount = 0f
        }
        params = lp

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(viewOwner)
            setViewTreeSavedStateRegistryOwner(viewOwner)
            setViewTreeViewModelStoreOwner(viewOwner)
            setContent {
                OverlayRoot(
                    controller = controller,
                    homeLineVisible = prefs.homeLineVisible,
                    blurEnabled = prefs.blurEnabled,
                    menuScale = prefs.menuScale,
                    onWindowExpand = { expand -> setExpanded(expand) },
                    onBackdropBlur = { fraction -> applyBackdropBlur(fraction, prefs) },
                )
            }
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK &&
                    event.action == KeyEvent.ACTION_UP &&
                    controller.open
                ) {
                    controller.open = false
                    true
                } else {
                    false
                }
            }
        }
        root = view

        runCatching { windowManager.addView(view, lp) }
        viewOwner.start()
        scheduleAutoOpen()
    }

    /** Grows the window to full screen while the menu is visible. */
    private fun setExpanded(expand: Boolean) {
        if (expanded == expand) return
        expanded = expand
        val view = root ?: return
        val lp = params ?: return
        lp.height = if (expand) {
            WindowManager.LayoutParams.MATCH_PARENT
        } else {
            collapsedHeightPx()
        }
        lp.flags = if (expand) EXPANDED_FLAGS else COLLAPSED_FLAGS
        if (!expand) {
            lp.dimAmount = 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                lp.blurBehindRadius = 0
            }
            lastBlur = -1
        }
        runCatching { windowManager.updateViewLayout(view, lp) }
    }

    /** Progressive real blur of whatever is behind the overlay window. */
    private fun applyBackdropBlur(fraction: Float, prefs: Prefs) {
        val view = root ?: return
        val lp = params ?: return
        if (!prefs.blurEnabled) return
        val radius = (prefs.blurRadius * fraction.coerceIn(0f, 1f)).toInt()
        if (radius == lastBlur) return
        lastBlur = radius
        lp.dimAmount = 0.3f * fraction.coerceIn(0f, 1f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lp.blurBehindRadius = radius
        }
        runCatching { windowManager.updateViewLayout(view, lp) }
    }

    private fun scheduleAutoOpen() {
        val prefs = Prefs(this)
        handler.removeCallbacksAndMessages(null)
        if (prefs.openMenuOnStart) {
            handler.postDelayed(
                { controller.open = true },
                prefs.startDelaySec.coerceAtLeast(0) * 1000L,
            )
        }
        val hide = prefs.autoHideSec
        if (hide > 0) {
            handler.postDelayed(
                { controller.open = false },
                (prefs.startDelaySec.coerceAtLeast(0) + hide) * 1000L,
            )
        }
    }

    private fun collapsedHeightPx(): Int =
        (76 * resources.displayMetrics.density).toInt()

    // ----------------------------------------------------------- notification

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
            channel.setShowBadge(false)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val toggle = PendingIntent.getService(
            this,
            1,
            Intent(this, OverlayService::class.java).setAction(ACTION_TOGGLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this,
            2,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Overlay is running")
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Toggle", toggle).build())
            .addAction(Notification.Action.Builder(null, "Stop", stop).build())
            .build()
    }

    /** Minimal lifecycle / saved-state / view-model owner for a window view. */
    private class OverlayViewOwner :
        LifecycleOwner,
        SavedStateRegistryOwner,
        ViewModelStoreOwner {

        private val registry = LifecycleRegistry(this)
        private val savedState = SavedStateRegistryController.create(this)
        private val store = ViewModelStore()

        override val lifecycle: Lifecycle get() = registry
        override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry
        override val viewModelStore: ViewModelStore get() = store

        fun start() {
            savedState.performRestore(null)
            registry.currentState = Lifecycle.State.RESUMED
        }

        fun destroy() {
            registry.currentState = Lifecycle.State.DESTROYED
            store.clear()
        }
    }

    companion object {
        const val ACTION_START = "com.nlshowcase.overlay.action.START"
        const val ACTION_STOP = "com.nlshowcase.overlay.action.STOP"
        const val ACTION_TOGGLE = "com.nlshowcase.overlay.action.TOGGLE"

        private const val CHANNEL_ID = "nl_overlay"
        private const val NOTIFICATION_ID = 4711

        private const val COLLAPSED_FLAGS =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        private const val EXPANDED_FLAGS =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        @Volatile
        var isRunning = false
            private set

        fun start(context: Context) = send(context, ACTION_START)

        fun stop(context: Context) = send(context, ACTION_STOP)

        fun toggle(context: Context) = send(context, ACTION_TOGGLE)

        private fun send(context: Context, action: String) {
            val intent = Intent(context, OverlayService::class.java).setAction(action)
            if (action == ACTION_STOP) {
                runCatching { context.startService(intent) }
            } else {
                runCatching { context.startForegroundService(intent) }
            }
        }
    }
}
