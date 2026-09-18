package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PrecisionRulerEngineTest {
    @Test
    fun shortDrag_doesNotSnap() {
        val result = PrecisionRulerEngine.snapAngle(Offset.Zero, Offset(0.5f, 0.5f))
        assertEquals(SnapKind.NONE, result.kind)
        assertEquals(Offset(0.5f, 0.5f), result.point)
    }

    @Test
    fun horizontalDrag_snapsToZeroDegrees() {
        val result = PrecisionRulerEngine.snapAngle(
            start = Offset.Zero,
            point = Offset(100f, 5f),
            toleranceDegrees = 7f
        )
        assertEquals(SnapKind.ANGLE, result.kind)
        assertEquals(0f, result.angleDegrees, 0.001f)
        assertEquals(0f, result.point.y, 0.001f)
    }

    @Test
    fun invalidGridSpacing_returnsOriginalPoint() {
        val point = Offset(12.3f, 45.6f)
        val result = PrecisionRulerEngine.snapGrid(point, 0f)
        assertEquals(SnapKind.NONE, result.kind)
        assertNotEquals(Offset.Zero, result.point)
        assertEquals(point, result.point)
    }
}
