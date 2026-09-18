package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedBrushEngineTest {
    @Test fun pressureWidthScalesAndClamps() {
        assertEquals(10f, AdvancedBrushEngine.pressureWidth(10f, 0f, true), 0.001f)
        assertTrue(AdvancedBrushEngine.pressureWidth(10f, 1f, true) > 10f)
        assertEquals(10f, AdvancedBrushEngine.pressureWidth(10f, 0f, false), 0.001f)
    }

    @Test fun smoothingPreservesEndpoints() {
        val input = listOf(
            AdvancedBrushEngine.Sample(Offset(0f, 0f)),
            AdvancedBrushEngine.Sample(Offset(10f, 20f)),
            AdvancedBrushEngine.Sample(Offset(20f, 0f))
        )
        val output = AdvancedBrushEngine.smooth(input, 1f)
        assertEquals(input.first().point, output.first().point)
        assertEquals(input.last().point, output.last().point)
        assertTrue(output[1].point.y < 20f)
    }

    @Test fun resamplingKeepsFirstAndLastSamples() {
        val input = listOf(
            AdvancedBrushEngine.Sample(Offset(0f, 0f)),
            AdvancedBrushEngine.Sample(Offset(25f, 0f))
        )
        val output = AdvancedBrushEngine.resample(input, 10f)
        assertEquals(input.first().point, output.first().point)
        assertEquals(input.last().point, output.last().point)
        assertEquals(4, output.size)
    }

    @Test fun taperReturnsFullWidthInMiddle() {
        assertEquals(1f, AdvancedBrushEngine.taperFactor(0.5f, 0.1f, 0.1f), 0.001f)
        assertEquals(0f, AdvancedBrushEngine.taperFactor(0f, 0.1f, 0.1f), 0.001f)
        assertEquals(0f, AdvancedBrushEngine.taperFactor(1f, 0.1f, 0.1f), 0.001f)
    }
}
