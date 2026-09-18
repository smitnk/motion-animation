package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SelectionTransformEngineTest {
    @Test
    fun boundsFindExtentsAndCenter() {
        val b = SelectionTransformEngine.bounds(listOf(Offset(10f, 20f), Offset(50f, 80f)))
        assertNotNull(b)
        assertEquals(10f, b!!.left)
        assertEquals(20f, b.top)
        assertEquals(50f, b.right)
        assertEquals(80f, b.bottom)
        assertEquals(30f, b.center.x)
        assertEquals(50f, b.center.y)
    }

    @Test
    fun emptyPointsHaveNoBounds() {
        assertNull(SelectionTransformEngine.bounds(emptyList()))
    }

    @Test
    fun scaleKeepsSelectionCenterFixed() {
        val points = listOf(Offset(10f, 10f), Offset(30f, 30f))
        val bounds = SelectionTransformEngine.bounds(points)!!
        val result = SelectionTransformEngine.transform(points, bounds, scale = 2f)
        assertEquals(0f, result[0].x)
        assertEquals(0f, result[0].y)
        assertEquals(40f, result[1].x)
        assertEquals(40f, result[1].y)
    }

    @Test
    fun translationMovesEveryPoint() {
        val points = listOf(Offset(10f, 20f), Offset(30f, 40f))
        val bounds = SelectionTransformEngine.bounds(points)!!
        val result = SelectionTransformEngine.transform(points, bounds, delta = Offset(5f, -3f))
        assertEquals(15f, result[0].x)
        assertEquals(17f, result[0].y)
        assertEquals(35f, result[1].x)
        assertEquals(37f, result[1].y)
    }
}
