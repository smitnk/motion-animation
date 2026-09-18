package com.smitnk.motioncanvas

import org.junit.Assert.assertEquals
import org.junit.Test

class LayerWorkspaceEngineTest {
    @Test fun moveUpStopsAtTop() {
        assertEquals(0, LayerWorkspaceEngine.moveUp(3, 0))
        assertEquals(1, LayerWorkspaceEngine.moveUp(3, 2))
    }

    @Test fun moveDownStopsAtBottom() {
        assertEquals(2, LayerWorkspaceEngine.moveDown(3, 2))
        assertEquals(1, LayerWorkspaceEngine.moveDown(3, 0))
    }

    @Test fun reorderMovesSelectedLayer() {
        val result = LayerWorkspaceEngine.reorder(listOf("A", "B", "C"), 0, 2)
        assertEquals(listOf("B", "C", "A"), result.items)
        assertEquals(2, result.selectedIndex)
    }

    @Test fun opacityIsClamped() {
        assertEquals(0f, LayerWorkspaceEngine.opacity(-1f))
        assertEquals(1f, LayerWorkspaceEngine.opacity(2f))
        assertEquals(0.4f, LayerWorkspaceEngine.opacity(0.4f))
    }
}
