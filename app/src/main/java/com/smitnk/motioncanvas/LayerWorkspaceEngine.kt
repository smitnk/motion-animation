package com.smitnk.motioncanvas

data class LayerMoveResult<T>(val items: List<T>, val selectedIndex: Int)

object LayerWorkspaceEngine {
    fun moveUp(size: Int, selectedIndex: Int): Int =
        if (selectedIndex > 0) selectedIndex - 1 else selectedIndex

    fun moveDown(size: Int, selectedIndex: Int): Int =
        if (selectedIndex in 0 until (size - 1)) selectedIndex + 1 else selectedIndex

    fun reorder<T>(items: List<T>, selectedIndex: Int, targetIndex: Int): LayerMoveResult<T> {
        if (selectedIndex !in items.indices || targetIndex !in items.indices || selectedIndex == targetIndex) {
            return LayerMoveResult(items, selectedIndex.coerceIn(0, (items.lastIndex).coerceAtLeast(0)))
        }
        val mutable = items.toMutableList()
        val item = mutable.removeAt(selectedIndex)
        mutable.add(targetIndex, item)
        return LayerMoveResult(mutable, targetIndex)
    }

    fun opacity(value: Float): Float = value.coerceIn(0f, 1f)
}
