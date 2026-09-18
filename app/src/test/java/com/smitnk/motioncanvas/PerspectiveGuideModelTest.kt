package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerspectiveGuideModelTest {
    @Test
    fun pointCount_isClampedToSupportedRange() {
        val model = PerspectiveGuideModel()
        assertEquals(1, model.withPointCount(0, 1600f, 1200f).vanishingPoints.size)
        assertEquals(3, model.withPointCount(9, 1600f, 1200f).vanishingPoints.size)
    }

    @Test
    fun horizon_isClampedToCanvasHeight() {
        val model = PerspectiveGuideModel().moveHorizon(1500f, 1200f)
        assertEquals(1200f, model.horizonY, 0.001f)
    }

    @Test
    fun nearHorizon_detectsWithinTolerance() {
        val model = PerspectiveGuideModel(horizonY = 500f)
        assertTrue(model.isNearHorizon(Offset(300f, 530f), 45f))
    }
}
