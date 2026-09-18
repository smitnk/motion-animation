package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import kotlin.math.round

/**
 * Pure canvas-guide utilities for professional alignment workflows.
 */
object CanvasGuideEngine {
    data class GridConfig(
        val spacing: Float = 100f,
        val snapEnabled: Boolean = false,
        val snapRadius: Float = 16f
    )

    fun gridPositions(length: Float, spacing: Float): List<Float> {
        val step = spacing.coerceAtLeast(1f)
        if (length <= 0f) return emptyList()
        val count = kotlin.math.floor(length / step).toInt()
        return (0..count).map { it * step }.filter { it <= length }
    }

    fun snapToGrid(point: Offset, config: GridConfig): Offset {
        if (!config.snapEnabled) return point
        val step = config.spacing.coerceAtLeast(1f)
        return Offset(
            round(point.x / step) * step,
            round(point.y / step) * step
        )
    }

    fun snapToPoint(point: Offset, targets: List<Offset>, radius: Float): Offset {
        val threshold = radius.coerceAtLeast(0f)
        var nearest: Offset? = null
        var nearestDistance = Float.POSITIVE_INFINITY
        targets.forEach { target ->
            val dx = target.x - point.x
            val dy = target.y - point.y
            val distanceSquared = dx * dx + dy * dy
            if (distanceSquared <= threshold * threshold && distanceSquared < nearestDistance) {
                nearestDistance = distanceSquared
                nearest = target
            }
        }
        return nearest ?: point
    }

    fun applySnapping(
        point: Offset,
        config: GridConfig,
        targets: List<Offset> = emptyList()
    ): Offset {
        if (!config.snapEnabled) return point
        val pointSnap = snapToPoint(point, targets, config.snapRadius)
        if (pointSnap != point) return pointSnap
        return snapToGrid(point, config)
    }
}
