package com.nlshowcase.overlay.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Interleaved position(3) + normal(3) triangle soup. */
class Mesh(data: FloatArray) {
    val vertexCount: Int = data.size / 6
    val buffer: FloatBuffer = ByteBuffer
        .allocateDirect(data.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(data)
            position(0)
        }
}

object MeshBuilder {

    private fun push(out: MutableList<Float>, x: Float, y: Float, z: Float, n: FloatArray) {
        out.add(x); out.add(y); out.add(z)
        out.add(n[0]); out.add(n[1]); out.add(n[2])
    }

    private fun tri(
        out: MutableList<Float>,
        a: FloatArray, b: FloatArray, c: FloatArray,
        n: FloatArray,
    ) {
        push(out, a[0], a[1], a[2], n)
        push(out, b[0], b[1], b[2], n)
        push(out, c[0], c[1], c[2], n)
    }

    private fun quad(
        out: MutableList<Float>,
        a: FloatArray, b: FloatArray, c: FloatArray, d: FloatArray,
        n: FloatArray,
    ) {
        tri(out, a, b, c, n)
        tri(out, a, c, d, n)
    }

    private fun v(x: Float, y: Float, z: Float) = floatArrayOf(x, y, z)

    /** Axis-aligned box centred on the origin. */
    fun box(w: Float = 1f, h: Float = 1f, d: Float = 1f): Mesh {
        val x = w / 2f
        val y = h / 2f
        val z = d / 2f
        val out = ArrayList<Float>(216)

        // front (+z)
        quad(out, v(-x, -y, z), v(x, -y, z), v(x, y, z), v(-x, y, z), v(0f, 0f, 1f))
        // back (-z)
        quad(out, v(x, -y, -z), v(-x, -y, -z), v(-x, y, -z), v(x, y, -z), v(0f, 0f, -1f))
        // right (+x)
        quad(out, v(x, -y, z), v(x, -y, -z), v(x, y, -z), v(x, y, z), v(1f, 0f, 0f))
        // left (-x)
        quad(out, v(-x, -y, -z), v(-x, -y, z), v(-x, y, z), v(-x, y, -z), v(-1f, 0f, 0f))
        // top (+y)
        quad(out, v(-x, y, z), v(x, y, z), v(x, y, -z), v(-x, y, -z), v(0f, 1f, 0f))
        // bottom (-y)
        quad(out, v(-x, -y, -z), v(x, -y, -z), v(x, -y, z), v(-x, -y, z), v(0f, -1f, 0f))

        return Mesh(out.toFloatArray())
    }

    /** UV sphere with radius 1, scale it with the model matrix. */
    fun sphere(segments: Int = 16, rings: Int = 12): Mesh {
        val out = ArrayList<Float>(segments * rings * 36)
        for (r in 0 until rings) {
            val phi0 = Math.PI * r / rings
            val phi1 = Math.PI * (r + 1) / rings
            for (s in 0 until segments) {
                val th0 = 2.0 * Math.PI * s / segments
                val th1 = 2.0 * Math.PI * (s + 1) / segments

                fun point(phi: Double, th: Double): FloatArray = floatArrayOf(
                    (sin(phi) * cos(th)).toFloat(),
                    cos(phi).toFloat(),
                    (sin(phi) * sin(th)).toFloat(),
                )

                val p00 = point(phi0, th0)
                val p01 = point(phi0, th1)
                val p10 = point(phi1, th0)
                val p11 = point(phi1, th1)

                push(out, p00[0], p00[1], p00[2], p00)
                push(out, p10[0], p10[1], p10[2], p10)
                push(out, p11[0], p11[1], p11[2], p11)

                push(out, p00[0], p00[1], p00[2], p00)
                push(out, p11[0], p11[1], p11[2], p11)
                push(out, p01[0], p01[1], p01[2], p01)
            }
        }
        return Mesh(out.toFloatArray())
    }

    /** Cone/cylinder along Y, centred on the origin. */
    fun cylinder(rTop: Float, rBottom: Float, height: Float, segments: Int = 18): Mesh {
        val out = ArrayList<Float>(segments * 48)
        val hy = height / 2f
        for (s in 0 until segments) {
            val a0 = 2.0 * Math.PI * s / segments
            val a1 = 2.0 * Math.PI * (s + 1) / segments
            val c0 = cos(a0).toFloat()
            val s0 = sin(a0).toFloat()
            val c1 = cos(a1).toFloat()
            val s1 = sin(a1).toFloat()

            val top0 = v(c0 * rTop, hy, s0 * rTop)
            val top1 = v(c1 * rTop, hy, s1 * rTop)
            val bot0 = v(c0 * rBottom, -hy, s0 * rBottom)
            val bot1 = v(c1 * rBottom, -hy, s1 * rBottom)

            val slope = (rBottom - rTop) / height
            fun sideNormal(c: Float, s: Float): FloatArray {
                val len = sqrt(1f + slope * slope)
                return floatArrayOf(c / len, slope / len, s / len)
            }

            val n0 = sideNormal(c0, s0)
            val n1 = sideNormal(c1, s1)

            push(out, bot0[0], bot0[1], bot0[2], n0)
            push(out, bot1[0], bot1[1], bot1[2], n1)
            push(out, top1[0], top1[1], top1[2], n1)

            push(out, bot0[0], bot0[1], bot0[2], n0)
            push(out, top1[0], top1[1], top1[2], n1)
            push(out, top0[0], top0[1], top0[2], n0)

            // caps
            val up = v(0f, 1f, 0f)
            val down = v(0f, -1f, 0f)
            tri(out, v(0f, hy, 0f), top0, top1, up)
            tri(out, v(0f, -hy, 0f), bot1, bot0, down)
        }
        return Mesh(out.toFloatArray())
    }
}
