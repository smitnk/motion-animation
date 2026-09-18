package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreDrawingEngineTest {
    @Test
    fun stabilize_reducesPointMovement() {
        val result = CoreDrawingEngine.stabilize(listOf(Offset.Zero, Offset(100f, 0f)), 0.5f)
        assertEquals(50f, result[1].x, 0.001f)
    }

    @Test
    fun respace_keepsEndpoints() {
        val points = (0..10).map { Offset(it * 10f, 0f) }
        val result = CoreDrawingEngine.respace(points, 1f, 10f)
        assertEquals(points.first(), result.first())
        assertEquals(points.last(), result.last())
        assertTrue(result.size <= points.size)
    }

    @Test
    fun pressureWidth_changesWithPressure() {
        val light = CoreDrawingEngine.pressureWidth(10f, 0.2f, 0f, 0f)
        val heavy = CoreDrawingEngine.pressureWidth(10f, 1f, 0f, 0f)
        assertTrue(heavy > light)
    }

    @Test
    fun brushProfiles_haveExpectedOrdering() {
        val pencil = CoreDrawingEngine.brushWidth(10f, BrushProfile.PENCIL)
        val pen = CoreDrawingEngine.brushWidth(10f, BrushProfile.PEN)
        val marker = CoreDrawingEngine.brushWidth(10f, BrushProfile.MARKER)
        val airbrush = CoreDrawingEngine.brushWidth(10f, BrushProfile.AIRBRUSH)
        assertTrue(pencil < pen && pen < marker && marker < airbrush)
    }

    @Test
    fun ellipse_returnsClosedPointSequence() {
        val result = CoreDrawingEngine.ellipse(Offset.Zero, Offset(100f, 50f))
        assertEquals(result.first(), result.last())
    }
}
