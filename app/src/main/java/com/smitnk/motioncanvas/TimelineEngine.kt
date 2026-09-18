package com.smitnk.motioncanvas

/**
 * Pure timeline operations used by the animation editor.
 * Operations return new lists so UI state can be replaced atomically.
 */
object TimelineEngine {
    fun insertFrame(frames: List<Frame>, index: Int, frame: Frame = Frame()): List<Frame> {
        val safeIndex = index.coerceIn(0, frames.size)
        return frames.toMutableList().apply { add(safeIndex, frame) }
    }

    fun duplicateFrame(frames: List<Frame>, index: Int): List<Frame> {
        if (index !in frames.indices) return frames
        return frames.toMutableList().apply { add(index + 1, frames[index]) }
    }

    fun deleteFrame(frames: List<Frame>, index: Int): List<Frame> {
        if (index !in frames.indices || frames.size <= 1) return frames
        return frames.toMutableList().apply { removeAt(index) }
    }

    fun moveFrame(frames: List<Frame>, from: Int, to: Int): List<Frame> {
        if (from !in frames.indices) return frames
        val safeTo = to.coerceIn(0, frames.lastIndex)
        if (from == safeTo) return frames
        return frames.toMutableList().apply {
            val item = removeAt(from)
            add(safeTo, item)
        }
    }

    fun setHold(frames: List<Frame>, index: Int, hold: Int): List<Frame> {
        if (index !in frames.indices) return frames
        val safeHold = hold.coerceAtLeast(1)
        return frames.toMutableList().apply {
            this[index] = this[index].copy(
                layers = this[index].layers.map { it.copy(hold = safeHold) }
            )
        }
    }

    /**
     * Expands exposure holds into the sequence of source-frame indices
     * displayed by playback/export.
     */
    fun exposureSequence(frames: List<Frame>): List<Int> {
        val result = ArrayList<Int>()
        frames.forEachIndexed { index, frame ->
            val hold = frame.layers.firstOrNull()?.hold?.coerceAtLeast(1) ?: 1
            repeat(hold) { result += index }
        }
        return result
    }
}
