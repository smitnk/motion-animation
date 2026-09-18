package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometrySnapEngineTest {
    @Test
    fun emptyVanishingPoints_returnsOriginalPoint() {
        val point = Offset(40f, 60f)
        val result = GeometrySnapEngine.snapToPerspective(point, Offset.Zero, emptyList())
        assertEquals(point, result.point)
        assertFalse(result.snapped)
    }

    @Test
    fun pointNearPerspectiveLine_snapsWithinRadius() {
        val origin = Offset(0f, 0f)
        val point = Offset(100f, 12f)
        val result = GeometrySnapEngine.snapToPerspective(
            point = point,
            origin = origin,
            vanishingPoints = listOf(Offset(1000f, 0f)),
            radius = 20f
        )
        assertTrue(result.snapped)
        assertEquals(0f, result.point.y, 0.001f)
    }
}
