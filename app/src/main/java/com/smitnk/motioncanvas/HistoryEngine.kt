package com.smitnk.motioncanvas

data class HistoryState<T>(val undo: List<T> = emptyList(), val redo: List<T> = emptyList())

object HistoryEngine {
    fun <T> push(state: HistoryState<T>, snapshot: T, limit: Int = 20): HistoryState<T> {
        val safeLimit = limit.coerceAtLeast(1)
        return state.copy(
            undo = (state.undo + snapshot).takeLast(safeLimit),
            redo = emptyList()
        )
    }

    fun <T> undo(state: HistoryState<T>, current: T): Pair<HistoryState<T>, T?> {
        if (state.undo.isEmpty()) return state to null
        val previous = state.undo.last()
        return state.copy(
            undo = state.undo.dropLast(1),
            redo = (state.redo + current).takeLast(20)
        ) to previous
    }

    fun <T> redo(state: HistoryState<T>, current: T): Pair<HistoryState<T>, T?> {
        if (state.redo.isEmpty()) return state to null
        val next = state.redo.last()
        return state.copy(
            undo = (state.undo + current).takeLast(20),
            redo = state.redo.dropLast(1)
        ) to next
    }
}
