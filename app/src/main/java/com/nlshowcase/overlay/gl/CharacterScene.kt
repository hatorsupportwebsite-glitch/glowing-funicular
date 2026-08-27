package com.nlshowcase.overlay.gl

import android.opengl.GLES20
import android.opengl.Matrix
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * A real, fully 3D anime-girl character built from procedural geometry and
 * animated with a small joint hierarchy (no external model files needed).
 *
 * Animations: Idle, Breathing, Wave, Look Around, Dance.
 */
class CharacterScene : GLScene {

    @Volatile var animation: String = "Idle"
    @Volatile var material: String = "Glow"
    @Volatile var autoRotate: Boolean = true
    @Volatile var userYaw: Float = 0f
    @Volatile var modelScale: Float = 1f
    @Volatile var speed: Float = 1f
    @Volatile var glow: FloatArray = floatArrayOf(0.48f, 0.64f, 0.91f)

    private var program = 0
    private var aPos = 0
    private var aNormal = 0
    private var uMvp = 0
    private var uModel = 0
    private var uColor = 0
    private var uAlpha = 0
    private var uLight = 0
    private var uRim = 0

    private var boxMesh: Mesh? = null
    private var sphereMesh: Mesh? = null
    private var limbMesh: Mesh? = null
    private var taperMesh: Mesh? = null
    private var skirtMesh: Mesh? = null

    private val proj = FloatArray(16)
    private val viewM = FloatArray(16)
    private val viewProj = FloatArray(16)
    private val mvp = FloatArray(16)

    private var model = FloatArray(16)
    private val stack = ArrayList<FloatArray>()

    private var vw = 1
    private var vh = 1

    // ---------------- palette ----------------
    private val skin = floatArrayOf(0.98f, 0.84f, 0.74f)
    private val hair = floatArrayOf(0.56f, 0.35f, 0.22f)
    private val hairDark = floatArrayOf(0.42f, 0.25f, 0.15f)
    private val jacket = floatArrayOf(0.29f, 0.34f, 0.24f)
    private val vest = floatArrayOf(0.13f, 0.14f, 0.17f)
    private val pants = floatArrayOf(0.31f, 0.33f, 0.25f)
    private val boots = floatArrayOf(0.10f, 0.11f, 0.13f)
    private val metal = floatArrayOf(0.19f, 0.20f, 0.23f)
    private val eyeDark = floatArrayOf(0.16f, 0.19f, 0.16f)
    private val eyeWhite = floatArrayOf(0.95f, 0.96f, 0.98f)
    private val shadow = floatArrayOf(0.02f, 0.02f, 0.03f)

    /* ------------------------------------------------------------ */
    /*  GL lifecycle                                                 */
    /* ------------------------------------------------------------ */

    override fun onSurfaceCreated() {
        program = buildProgram(VERTEX_SRC, FRAGMENT_SRC)
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aNormal = GLES20.glGetAttribLocation(program, "aNormal")
        uMvp = GLES20.glGetUniformLocation(program, "uMvp")
        uModel = GLES20.glGetUniformLocation(program, "uModel")
        uColor = GLES20.glGetUniformLocation(program, "uColor")
        uAlpha = GLES20.glGetUniformLocation(program, "uAlpha")
        uLight = GLES20.glGetUniformLocation(program, "uLightDir")
        uRim = GLES20.glGetUniformLocation(program, "uRim")

        boxMesh = MeshBuilder.box(1f, 1f, 1f)
        sphereMesh = MeshBuilder.sphere(18, 14)
        limbMesh = MeshBuilder.cylinder(0.5f, 0.5f, 1f, 16)
        taperMesh = MeshBuilder.cylinder(0.34f, 0.5f, 1f, 16)
        skirtMesh = MeshBuilder.cylinder(0.42f, 0.95f, 1f, 20)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(0f, 0f, 0f, 0f)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        vw = max(width, 1)
        vh = max(height, 1)
        GLES20.glViewport(0, 0, vw, vh)
        val aspect = vw.toFloat() / vh.toFloat()
        Matrix.perspectiveM(proj, 0, 32f, aspect, 0.1f, 30f)
        Matrix.setLookAtM(viewM, 0, 0f, 0.95f, 3.75f, 0f, 0.92f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(viewProj, 0, proj, 0, viewM, 0)
    }

    override fun onDrawFrame(timeSec: Float) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (program == 0) return

        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glEnableVertexAttribArray(aNormal)
        GLES20.glUniform3f(uLight, 0.45f, 0.85f, 0.65f)
        GLES20.glUniform3f(uRim, glow[0], glow[1], glow[2])

        val t = timeSec * speed
        Matrix.setIdentityM(model, 0)

        val autoYaw = if (autoRotate) sin(t * 0.25f) * 26f else 0f
        stack.clear()

        push()
        translate(0f, -0.06f, 0f)
        rotate(autoYaw + userYaw, 0f, 1f, 0f)
        scale(modelScale, modelScale, modelScale)
        drawShadow()
        drawCharacter(t)
        pop()

        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aNormal)
    }

    /* ------------------------------------------------------------ */
    /*  Character                                                    */
    /* ------------------------------------------------------------ */

    private fun drawShadow() {
        push()
        translate(0f, 0.012f, 0.02f)
        scale(0.62f, 0.02f, 0.42f)
        draw(limbMesh, shadow, 0.32f)
        pop()
    }

    private fun drawCharacter(t: Float) {
        val anim = animation

        // shared idle motion
        val breath = sin(t * 2.1f)
        val sway = sin(t * 0.85f)
        val shift = sin(t * 0.42f)

        var hipBounce = breath * 0.006f
        var hipYaw = sway * 2.4f
        var hipRoll = shift * 1.6f
        var spinePitch = -1.5f + breath * 0.9f
        var chestScale = 1f + breath * 0.012f
        var headYaw = sin(t * 0.5f) * 6f
        var headPitch = breath * 1.2f
        var armRaiseL = 0f
        var armRaiseR = 0f
        var elbowL = -14f
        var elbowR = -18f
        var legBendL = 0f
        var legBendR = 0f
        var handWave = 0f

        when (anim) {
            "Breathing" -> {
                chestScale = 1f + breath * 0.035f
                spinePitch = -2f + breath * 2.6f
                headPitch = breath * 3.2f
                hipBounce = breath * 0.012f
            }
            "Wave" -> {
                armRaiseR = -128f
                elbowR = -28f
                handWave = sin(t * 7f) * 26f
                headYaw = 8f + sin(t * 1.2f) * 5f
                hipYaw = sway * 3.5f
            }
            "Look Around" -> {
                headYaw = sin(t * 0.9f) * 36f
                headPitch = sin(t * 0.6f) * 6f
                hipYaw = sin(t * 0.9f) * 6f
            }
            "Dance" -> {
                val beat = sin(t * 5.2f)
                hipBounce = abs(beat) * 0.045f - 0.02f
                hipYaw = beat * 14f
                hipRoll = cos(t * 5.2f) * 8f
                spinePitch = -4f + beat * 5f
                headYaw = beat * 14f
                headPitch = -beat * 6f
                armRaiseL = -70f + beat * 40f
                armRaiseR = -70f - beat * 40f
                elbowL = -55f
                elbowR = -55f
                legBendL = 10f + beat * 8f
                legBendR = 10f - beat * 8f
            }
            else -> {
                // Idle: slow weight shift + rare head turn
                val glance = sin(t * 0.31f)
                headYaw = glance * 14f
                hipRoll = shift * 2.4f
            }
        }

        push()
        translate(0f, 0.86f + hipBounce, 0f)
        rotate(hipYaw, 0f, 1f, 0f)
        rotate(hipRoll, 0f, 0f, 1f)

        // pelvis
        push()
        scale(0.27f, 0.17f, 0.19f)
        draw(boxMesh, pants, 1f)
        pop()

        // combat skirt / belt
        push()
        translate(0f, -0.02f, 0f)
        scale(0.34f, 0.2f, 0.28f)
        draw(skirtMesh, jacket, 1f)
        pop()
        push()
        translate(0f, 0.08f, 0f)
        scale(0.29f, 0.05f, 0.21f)
        draw(boxMesh, metal, 1f)
        pop()

        // ---- spine / torso ----
        push()
        translate(0f, 0.1f, 0f)
        rotate(spinePitch, 1f, 0f, 0f)

        push()
        translate(0f, 0.12f, 0f)
        scale(0.3f, 0.27f * chestScale, 0.2f)
        draw(taperMesh, jacket, 1f)
        pop()

        // chest / vest
        push()
        translate(0f, 0.26f, 0.015f)
        scale(0.33f, 0.28f * chestScale, 0.225f)
        draw(boxMesh, vest, 1f)
        pop()
        // vest pouches
        for (i in -1..1) {
            push()
            translate(i * 0.085f, 0.17f, 0.125f)
            scale(0.07f, 0.08f, 0.03f)
            draw(boxMesh, floatArrayOf(0.10f, 0.11f, 0.13f), 1f)
            pop()
        }

        // ---- head ----
        push()
        translate(0f, 0.42f, 0f)
        // neck
        push()
        scale(0.09f, 0.09f, 0.09f)
        draw(limbMesh, skin, 1f)
        pop()

        translate(0f, 0.13f, 0f)
        rotate(headYaw, 0f, 1f, 0f)
        rotate(headPitch, 1f, 0f, 0f)

        // skull
        push()
        scale(0.115f, 0.135f, 0.115f)
        draw(sphereMesh, skin, 1f)
        pop()
        // hair volume
        push()
        translate(0f, 0.022f, -0.012f)
        scale(0.128f, 0.14f, 0.127f)
        draw(sphereMesh, hair, 1f)
        pop()
        // fringe
        push()
        translate(0f, 0.075f, 0.055f)
        scale(0.2f, 0.06f, 0.12f)
        draw(boxMesh, hair, 1f)
        pop()
        // eyes
        for (s in intArrayOf(-1, 1)) {
            push()
            translate(s * 0.045f, 0.0f, 0.105f)
            scale(0.032f, 0.042f, 0.02f)
            draw(boxMesh, eyeWhite, 1f)
            pop()
            push()
            translate(s * 0.045f, -0.004f, 0.116f)
            scale(0.022f, 0.03f, 0.012f)
            draw(boxMesh, eyeDark, 1f)
            pop()
        }
        // headset
        for (s in intArrayOf(-1, 1)) {
            push()
            translate(s * 0.115f, 0.01f, 0f)
            scale(0.035f, 0.06f, 0.055f)
            draw(boxMesh, metal, 1f)
            pop()
        }
        // ponytail: 4 animated segments
        push()
        translate(0f, 0.05f, -0.11f)
        var wave = 0f
        for (i in 0 until 4) {
            wave = sin(t * 2.6f - i * 0.7f) * (5f + i * 2.4f)
            rotate(24f + wave, 1f, 0f, 0f)
            rotate(wave * 0.35f, 0f, 0f, 1f)
            translate(0f, -0.055f, -0.012f)
            push()
            scale(0.075f - i * 0.008f, 0.115f, 0.075f - i * 0.008f)
            draw(limbMesh, if (i % 2 == 0) hair else hairDark, 1f)
            pop()
            translate(0f, -0.055f, 0f)
        }
        pop()
        pop() // head

        // ---- arms ----
        drawArm(t, side = -1, raise = armRaiseL, elbow = elbowL, wave = 0f, withGun = false)
        drawArm(t, side = 1, raise = armRaiseR, elbow = elbowR, wave = handWave, withGun = animation != "Wave" && animation != "Dance")

        pop() // spine

        // ---- legs ----
        drawLeg(t, -1, legBendL, shift)
        drawLeg(t, 1, legBendR, -shift)

        pop() // hips
    }

    private fun drawArm(t: Float, side: Int, raise: Float, elbow: Float, wave: Float, withGun: Boolean) {
        push()
        translate(side * 0.175f, 0.34f, 0f)
        // shoulder pad
        push()
        scale(0.1f, 0.1f, 0.1f)
        draw(sphereMesh, jacket, 1f)
        pop()

        rotate(raise, 1f, 0f, 0f)
        rotate(side * (6f + sin(t * 0.9f) * 2.5f), 0f, 0f, 1f)

        // upper arm
        push()
        translate(0f, -0.115f, 0f)
        scale(0.075f, 0.23f, 0.075f)
        draw(limbMesh, jacket, 1f)
        pop()

        // elbow -> forearm
        translate(0f, -0.23f, 0f)
        rotate(elbow + wave, 1f, 0f, 0f)
        push()
        translate(0f, -0.1f, 0f)
        scale(0.065f, 0.21f, 0.065f)
        draw(limbMesh, skin, 1f)
        pop()

        // glove
        translate(0f, -0.22f, 0f)
        push()
        scale(0.075f, 0.085f, 0.07f)
        draw(boxMesh, floatArrayOf(0.12f, 0.13f, 0.15f), 1f)
        pop()

        if (withGun) drawRifle()
        pop()
    }

    private fun drawRifle() {
        push()
        translate(0.02f, -0.1f, 0.06f)
        rotate(-8f, 1f, 0f, 0f)
        // receiver
        push()
        scale(0.055f, 0.42f, 0.075f)
        draw(boxMesh, metal, 1f)
        pop()
        // magazine
        push()
        translate(0f, -0.06f, -0.05f)
        scale(0.045f, 0.16f, 0.05f)
        draw(boxMesh, floatArrayOf(0.15f, 0.16f, 0.18f), 1f)
        pop()
        // barrel
        push()
        translate(0f, -0.3f, 0.01f)
        scale(0.03f, 0.26f, 0.03f)
        draw(limbMesh, floatArrayOf(0.14f, 0.15f, 0.17f), 1f)
        pop()
        // stock
        push()
        translate(0f, 0.26f, -0.02f)
        scale(0.05f, 0.16f, 0.07f)
        draw(boxMesh, floatArrayOf(0.16f, 0.17f, 0.19f), 1f)
        pop()
        // optic
        push()
        translate(0f, 0.12f, 0.07f)
        scale(0.03f, 0.1f, 0.035f)
        draw(boxMesh, floatArrayOf(0.09f, 0.1f, 0.12f), 1f)
        pop()
        pop()
    }

    private fun drawLeg(t: Float, side: Int, bend: Float, shiftPhase: Float) {
        push()
        translate(side * 0.085f, -0.09f, 0f)
        rotate(bend + shiftPhase * 1.5f, 1f, 0f, 0f)

        // thigh
        push()
        translate(0f, -0.2f, 0f)
        scale(0.115f, 0.4f, 0.12f)
        draw(limbMesh, pants, 1f)
        pop()

        // knee pad
        translate(0f, -0.4f, 0f)
        push()
        translate(0f, 0.01f, 0.045f)
        scale(0.1f, 0.09f, 0.06f)
        draw(boxMesh, floatArrayOf(0.14f, 0.15f, 0.17f), 1f)
        pop()

        rotate(bend * 0.6f + 4f, 1f, 0f, 0f)
        // shin
        push()
        translate(0f, -0.18f, 0f)
        scale(0.095f, 0.36f, 0.1f)
        draw(limbMesh, pants, 1f)
        pop()

        // boot
        translate(0f, -0.36f, 0f)
        push()
        translate(0f, -0.03f, 0.03f)
        scale(0.11f, 0.11f, 0.24f)
        draw(boxMesh, boots, 1f)
        pop()
        pop()
    }

    /* ------------------------------------------------------------ */
    /*  Matrix stack + draw helpers                                  */
    /* ------------------------------------------------------------ */

    private fun push() {
        stack.add(model.copyOf())
    }

    private fun pop() {
        if (stack.isNotEmpty()) model = stack.removeAt(stack.lastIndex)
    }

    private fun translate(x: Float, y: Float, z: Float) {
        Matrix.translateM(model, 0, x, y, z)
    }

    private fun rotate(deg: Float, x: Float, y: Float, z: Float) {
        if (deg != 0f) Matrix.rotateM(model, 0, deg, x, y, z)
    }

    private fun scale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(model, 0, x, y, z)
    }

    private fun materialColor(base: FloatArray): FloatArray = when (material) {
        "Disabled", "Shaded" -> base
        "Solid" -> glow
        "Glow", "Glow Outline" -> floatArrayOf(
            base[0] * 0.55f + glow[0] * 0.45f,
            base[1] * 0.55f + glow[1] * 0.45f,
            base[2] * 0.55f + glow[2] * 0.45f,
        )
        "Iridescent" -> floatArrayOf(
            0.5f + 0.5f * sin(base[0] * 6f + iridescentPhase),
            0.5f + 0.5f * sin(base[1] * 6f + iridescentPhase + 2.1f),
            0.5f + 0.5f * sin(base[2] * 6f + iridescentPhase + 4.2f),
        )
        else -> base
    }

    private var iridescentPhase = 0f

    private fun draw(mesh: Mesh?, color: FloatArray, alpha: Float) {
        val m = mesh ?: return
        val c = materialColor(color)
        Matrix.multiplyMM(mvp, 0, viewProj, 0, model, 0)
        GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0)
        GLES20.glUniformMatrix4fv(uModel, 1, false, model, 0)
        GLES20.glUniform3f(uColor, c[0], c[1], c[2])
        GLES20.glUniform1f(uAlpha, alpha)

        m.buffer.position(0)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 24, m.buffer)
        m.buffer.position(3)
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 24, m.buffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, m.vertexCount)
    }

    private fun buildProgram(vertexSrc: String, fragmentSrc: String): Int {
        val vs = compile(GLES20.GL_VERTEX_SHADER, vertexSrc)
        val fs = compile(GLES20.GL_FRAGMENT_SHADER, fragmentSrc)
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, vs)
        GLES20.glAttachShader(p, fs)
        GLES20.glLinkProgram(p)
        return p
    }

    private fun compile(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val VERTEX_SRC = """
            uniform mat4 uMvp;
            uniform mat4 uModel;
            attribute vec3 aPos;
            attribute vec3 aNormal;
            varying vec3 vNormal;
            void main() {
                vNormal = normalize((uModel * vec4(aNormal, 0.0)).xyz);
                gl_Position = uMvp * vec4(aPos, 1.0);
            }
        """

        private const val FRAGMENT_SRC = """
            precision mediump float;
            uniform vec3 uColor;
            uniform float uAlpha;
            uniform vec3 uLightDir;
            uniform vec3 uRim;
            varying vec3 vNormal;
            void main() {
                vec3 n = normalize(vNormal);
                float diff = max(dot(n, normalize(uLightDir)), 0.0);
                float toon = mix(0.55, 0.8, step(0.3, diff));
                toon = mix(toon, 1.0, step(0.66, diff));
                vec3 viewDir = vec3(0.0, 0.0, 1.0);
                float rim = pow(1.0 - max(dot(n, viewDir), 0.0), 2.5);
                vec3 col = uColor * toon + uRim * rim * 0.45;
                gl_FragColor = vec4(col * uAlpha, uAlpha);
            }
        """
    }
}
