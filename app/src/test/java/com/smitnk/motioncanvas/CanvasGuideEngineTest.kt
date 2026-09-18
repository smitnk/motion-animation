package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class CanvasGuideEngineTest {
    @Test fun gridPositionsUsesPositiveSpacing() {
        assertEquals(listOf(0f, 10f, 20f, 30f), CanvasGuideEngine.gridPositions(35f, 10f))
        assertEquals(listOf(0f, 35f), CanvasGuideEngine.gridPositions(35f, 35f))
    }

    @Test fun gridSnapRoundsToNearestGridIntersection() {
        val config = CanvasGuideEngine.GridConfig(spacing = 20f, snapEnabled = true)
        assertEquals(Offset(40f, 20f), CanvasGuideEngine.snapToGrid(Offset(31f, 29f), config))
    }

    @Test fun pointSnapChoosesNearestTargetWithinRadius() {
        val point = Offset(9f, 9f)
        val targets = listOf(Offset(20f, 20f), Offset(10f, 10f))
        assertEquals(Offset(10f, 10f), CanvasGuideEngine.snapToPoint(point, targets, 5f))
    }

    @Test fun snappingDisabledLeavesPointUnchanged() {
        val point = Offset(13f, 17f)
        val config = CanvasGuideEngine.GridConfig(spacing = 10f, snapEnabled = false)
        assertEquals(point, CanvasGuideEngine.applySnapping(point, config, listOf(Offset.Zero)))
    }
}
