package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

enum class BrushProfile { PENCIL, PEN, MARKER, AIRBRUSH }

object CoreDrawingEngine {
    fun stabilize(points: List<Offset>, amount: Float): List<Offset> {
        if (points.size < 2 || amount <= 0f) return points
        val factor = (1f - amount.coerceIn(0f, 0.95f))
        val out = ArrayList<Offset>(points.size)
        var last = points.first()
        out += last
        points.drop(1).forEach { p ->
            last = Offset(last.x + (p.x - last.x) * factor, last.y + (p.y - last.y) * factor)
            out += last
        }
        return out
    }

    fun respace(points: List<Offset>, spacing: Float, width: Float): List<Offset> {
        if (points.size < 2 || spacing <= 0f) return points
        val out = ArrayList<Offset>()
        out += points.first()
        var carry = 0f
        val target = max(1f, width * spacing)
        for (i in 1 until points.size) {
            val a = points[i - 1]
            val b = points[i]
            carry += hypot(a, b)
            if (carry >= target) {
                out += b
                carry = 0f
            }
        }
        if (out.last() != points.last()) out += points.last()
        return out
    }

    fun quickStraighten(points: List<Offset>): List<Offset> {
        if (points.size < 6) return points
        val a = points.first()
        val b = points.last()
        val dx = b.x - a.x
        val dy = b.y - a.y
        return if (abs(dx) > abs(dy) * 6f) {
            points.map { p ->
                val t = (p.x - a.x) / (if (dx == 0f) 1f else dx)
                Offset(p.x, a.y + dy * t)
            }
        } else if (abs(dy) > abs(dx) * 6f) {
            points.map { p ->
                val t = (p.y - a.y) / (if (dy == 0f) 1f else dy)
                Offset(a.x + dx * t, p.y)
            }
        } else points
    }

    fun pressureWidth(baseWidth: Float, pressure: Float, taper: Float, progress: Float): Float =
        baseWidth * (0.45f + pressure.coerceIn(0.05f, 1.25f) * 0.85f) *
            (1f - taper.coerceIn(0f, 1f) * progress.coerceIn(0f, 1f))

    fun brushWidth(baseWidth: Float, profile: BrushProfile): Float =
        (baseWidth * when (profile) {
            BrushProfile.PENCIL -> 0.82f
            BrushProfile.PEN -> 1f
            BrushProfile.MARKER -> 1.35f
            BrushProfile.AIRBRUSH -> 1.8f
        }).coerceAtLeast(1f)

    fun ellipse(a: Offset, b: Offset, segments: Int = 48): List<Offset> {
        val safeSegments = segments.coerceAtLeast(8)
        val cx = (a.x + b.x) / 2f
        val cy = (a.y + b.y) / 2f
        val rx = abs(b.x - a.x) / 2f
        val ry = abs(b.y - a.y) / 2f
        return (0..safeSegments).map { i ->
            val t = i * 2f * Math.PI.toFloat() / safeSegments
            Offset(cx + rx * cos(t), cy + ry * sin(t))
        }
    }

    private fun hypot(a: Offset, b: Offset): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        return sqrt(dx * dx + dy * dy)
    }
}
