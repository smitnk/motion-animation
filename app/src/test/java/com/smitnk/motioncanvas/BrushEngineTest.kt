package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrushEngineTest {
    @Test
    fun process_preservesEndpoints() {
        val points = listOf(Offset.Zero, Offset(100f, 0f), Offset(200f, 0f))
        val result = BrushEngine.process(points, listOf(0.2f, 0.6f, 1f), 10f, 0f, 0f, 0.18f, false)
        assertEquals(points.first(), result.points.first())
        assertEquals(points.last(), result.points.last())
    }

    @Test
    fun process_remapsPressureToProcessedPointCount() {
        val points = (0..20).map { Offset(it * 10f, 0f) }
        val pressures = points.map { it.x / 200f }
        val result = BrushEngine.process(points, pressures, 20f, 0f, 0f, 0.18f, true)
        assertEquals(result.points.size, result.pressures.size)
        assertTrue(result.pressures.first() >= 0.05f)
        assertTrue(result.pressures.last() <= 1.25f)
    }

    @Test
    fun process_combinesStabilizationAndStreamline() {
        val points = listOf(Offset.Zero, Offset(50f, 40f), Offset(100f, 0f))
        val result = BrushEngine.process(points, listOf(1f, 1f, 1f), 10f, 0.2f, 0.5f, 0.18f, false)
        assertEquals(points.size, result.points.size)
        assertEquals(points.first(), result.points.first())
        assertTrue(result.points[1].y < 40f)
    }
}
