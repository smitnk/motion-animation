package com.smitnk.motioncanvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryEngineTest {
    @Test
    fun pushClearsRedoAndBoundsUndoHistory() {
        var state = HistoryState<Int>()
        state = HistoryEngine.push(state, 1, limit = 2)
        state = HistoryEngine.push(state, 2, limit = 2)
        state = HistoryEngine.push(state, 3, limit = 2)
        assertEquals(listOf(2, 3), state.undo)
        assertEquals(emptyList<Int>(), state.redo)
    }

    @Test
    fun undoMovesCurrentToRedo() {
        val state = HistoryState(undo = listOf(1, 2))
        val (next, value) = HistoryEngine.undo(state, 3)
        assertEquals(2, value)
        assertEquals(listOf(1), next.undo)
        assertEquals(listOf(3), next.redo)
    }

    @Test
    fun redoMovesCurrentToUndo() {
        val state = HistoryState(undo = listOf(1), redo = listOf(2))
        val (next, value) = HistoryEngine.redo(state, 3)
        assertEquals(2, value)
        assertEquals(listOf(1, 3), next.undo)
        assertEquals(emptyList<Int>(), next.redo)
    }

    @Test
    fun emptyHistoryDoesNotChangeState() {
        val state = HistoryState<Int>()
        val (afterUndo, undoValue) = HistoryEngine.undo(state, 7)
        val (afterRedo, redoValue) = HistoryEngine.redo(state, 7)
        assertNull(undoValue)
        assertNull(redoValue)
        assertEquals(state, afterUndo)
        assertEquals(state, afterRedo)
    }
}
