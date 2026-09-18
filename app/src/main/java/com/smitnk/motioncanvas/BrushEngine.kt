package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import kotlin.math.max

data class BrushEngineResult(
    val points: List<Offset>,
    val pressures: List<Float>
)

object BrushEngine {
    fun process(
        points: List<Offset>,
        pressures: List<Float>,
        width: Float,
        stabilization: Float,
        streamline: Float,
        spacing: Float,
        deepEngine: Boolean
    ): BrushEngineResult {
        if (points.isEmpty()) return BrushEngineResult(emptyList(), emptyList())

        val smoothAmount = (stabilization + streamline * 0.65f).coerceIn(0f, 0.95f)
        var processed = CoreDrawingEngine.stabilize(points, smoothAmount)

        if (deepEngine && processed.size > 2) {
            processed = CoreDrawingEngine.respace(processed, spacing.coerceAtLeast(0.04f), width.coerceAtLeast(1f))
        }

        val mappedPressures = remapPressures(pressures, points.size, processed.size, deepEngine)
        return BrushEngineResult(processed, mappedPressures)
    }

    private fun remapPressures(
        pressures: List<Float>,
        sourceSize: Int,
        targetSize: Int,
        deepEngine: Boolean
    ): List<Float> {
        if (targetSize == 0) return emptyList()
        if (pressures.isEmpty()) return List(targetSize) { 1f }
        if (pressures.size == 1) return List(targetSize) { pressures.first().coerceIn(0.05f, 1.25f) }

        return List(targetSize) { index ->
            val t = if (targetSize == 1) 0f else index.toFloat() / (targetSize - 1)
            val sourcePosition = t * (sourceSize.coerceAtLeast(2) - 1)
            val lo = sourcePosition.toInt().coerceIn(0, pressures.lastIndex)
            val hi = (lo + 1).coerceAtMost(pressures.lastIndex)
            val local = sourcePosition - lo
            val raw = pressures[lo] + (pressures[hi] - pressures[lo]) * local
            val value = raw.coerceIn(0.05f, 1.25f)
            if (deepEngine) value * 0.9f + 0.1f else value
        }
    }
}
