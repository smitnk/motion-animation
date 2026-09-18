package com.smitnk.motioncanvas

data class PlaybackState(
    val frameIndex: Int = 0,
    val direction: Int = 1,
    val holdTick: Int = 0
)

object AnimationPlaybackEngine {
    fun normalizeDirection(direction: Int): Int = if (direction < 0) -1 else 1

    fun tick(
        state: PlaybackState,
        frameCount: Int,
        pingPong: Boolean,
        hold: Int
    ): PlaybackState {
        if (frameCount <= 1) return state.copy(frameIndex = 0, direction = normalizeDirection(state.direction), holdTick = 0)
        val safeHold = hold.coerceAtLeast(1)
        if (state.holdTick + 1 < safeHold) return state.copy(holdTick = state.holdTick + 1)

        val direction = normalizeDirection(state.direction)
        val next = state.frameIndex + direction
        if (next in 0 until frameCount) return state.copy(frameIndex = next, holdTick = 0)

        return if (pingPong) {
            val bounced = -direction
            val target = (state.frameIndex + bounced).coerceIn(0, frameCount - 1)
            state.copy(frameIndex = target, direction = bounced, holdTick = 0)
        } else {
            val wrapped = if (direction > 0) 0 else frameCount - 1
            state.copy(frameIndex = wrapped, direction = direction, holdTick = 0)
        }
    }

    fun delayMillis(fps: Int, hold: Int = 1): Long {
        val safeFps = fps.coerceIn(1, 120)
        val safeHold = hold.coerceAtLeast(1)
        return ((1000L * safeHold) / safeFps).coerceAtLeast(1L)
    }
}

object OnionSkinEngine {
    fun neighborIndices(frameIndex: Int, frameCount: Int, before: Int = 1, after: Int = 1): List<Int> {
        if (frameCount <= 0 || frameIndex !in 0 until frameCount) return emptyList()
        val result = ArrayList<Int>()
        for (offset in before.coerceAtLeast(0) downTo 1) {
            val index = frameIndex - offset
            if (index >= 0) result += index
        }
        for (offset in 1..after.coerceAtLeast(0)) {
            val index = frameIndex + offset
            if (index < frameCount) result += index
        }
        return result
    }

    fun opacity(distance: Int, base: Float = 0.18f): Float {
        if (distance <= 0) return base.coerceIn(0f, 1f)
        return (base / (distance + 1)).coerceIn(0f, 1f)
    }
}
