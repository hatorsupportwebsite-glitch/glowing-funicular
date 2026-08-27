package com.nlshowcase.overlay.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.view.TextureView

/** Minimal GL scene contract. */
interface GLScene {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onDrawFrame(timeSec: Float)
}

/**
 * A [TextureView] that renders a [GLScene] on its own EGL thread.
 *
 * TextureView (instead of GLSurfaceView) keeps the 3D model inside the normal
 * view hierarchy, so the Compose ESP elements can be drawn on top of it and
 * the whole thing still supports transparency inside the overlay window.
 */
class GLTextureView(
    context: Context,
    private val scene: GLScene,
) : TextureView(context), TextureView.SurfaceTextureListener {

    private var thread: RenderThread? = null

    init {
        isOpaque = false
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        thread = RenderThread(surface, width, height, scene).also { it.start() }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        thread?.resize(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        thread?.finish()
        thread = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

    private class RenderThread(
        private val surfaceTexture: SurfaceTexture,
        @Volatile private var width: Int,
        @Volatile private var height: Int,
        private val scene: GLScene,
    ) : Thread("NlGlThread") {

        @Volatile private var running = true
        @Volatile private var sizeDirty = true

        private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
        private var context: EGLContext = EGL14.EGL_NO_CONTEXT
        private var surface: EGLSurface = EGL14.EGL_NO_SURFACE

        fun resize(w: Int, h: Int) {
            width = w
            height = h
            sizeDirty = true
        }

        fun finish() {
            running = false
        }

        override fun run() {
            if (!initEgl()) return
            scene.onSurfaceCreated()
            val start = System.nanoTime()
            while (running) {
                if (sizeDirty) {
                    sizeDirty = false
                    scene.onSurfaceChanged(width, height)
                }
                val t = (System.nanoTime() - start) / 1_000_000_000f
                scene.onDrawFrame(t)
                EGL14.eglSwapBuffers(display, surface)
                try {
                    sleep(16L)
                } catch (e: InterruptedException) {
                    break
                }
            }
            releaseEgl()
        }

        private fun initEgl(): Boolean {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL14.EGL_NO_DISPLAY) return false

            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) return false

            val attribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_NONE,
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            if (!EGL14.eglChooseConfig(display, attribs, 0, configs, 0, 1, numConfigs, 0)) return false
            val config = configs[0] ?: return false

            val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
            if (context == EGL14.EGL_NO_CONTEXT) return false

            surface = EGL14.eglCreateWindowSurface(display, config, surfaceTexture, intArrayOf(EGL14.EGL_NONE), 0)
            if (surface == EGL14.EGL_NO_SURFACE) return false

            return EGL14.eglMakeCurrent(display, surface, surface, context)
        }

        private fun releaseEgl() {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, surface)
            if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context)
            EGL14.eglTerminate(display)
        }
    }
}
