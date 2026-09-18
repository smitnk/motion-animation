package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset

data class SelectionBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val center: Offset get() = Offset((left + right) / 2f, (top + bottom) / 2f)
}

object SelectionTransformEngine {
    fun bounds(points: List<Offset>): SelectionBounds? {
        if (points.isEmpty()) return null
        var left = points.first().x
        var top = points.first().y
        var right = left
        var bottom = top
        points.drop(1).forEach {
            left = minOf(left, it.x)
            top = minOf(top, it.y)
            right = maxOf(right, it.x)
            bottom = maxOf(bottom, it.y)
        }
        return SelectionBounds(left, top, right, bottom)
    }

    fun transform(
        points: List<Offset>,
        bounds: SelectionBounds,
        scale: Float = 1f,
        degrees: Float = 0f,
        delta: Offset = Offset.Zero
    ): List<Offset> {
        val safeScale = scale.coerceAtLeast(0.01f)
        val radians = degrees * Math.PI.toFloat() / 180f
        val c = kotlin.math.cos(radians)
        val s = kotlin.math.sin(radians)
        val center = bounds.center
        return points.map { p ->
            val x = (p.x - center.x) * safeScale
            val y = (p.y - center.y) * safeScale
            Offset(
                center.x + x * c - y * s + delta.x,
                center.y + x * s + y * c + delta.y
            )
        }
    }
}
