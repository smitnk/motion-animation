package com.smitnk.motioncanvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationPlaybackEngineTest {
    @Test fun holdDelaysAdvance() {
        val s = PlaybackState(frameIndex = 0, holdTick = 0)
        assertEquals(PlaybackState(0, 1, 1), AnimationPlaybackEngine.tick(s, 3, false, 3))
        assertEquals(PlaybackState(0, 1, 2), AnimationPlaybackEngine.tick(PlaybackState(0,1,1), 3, false, 3))
        assertEquals(PlaybackState(1, 1, 0), AnimationPlaybackEngine.tick(PlaybackState(0,1,2), 3, false, 3))
    }

    @Test fun normalPlaybackLoopsAtEnd() {
        val s = PlaybackState(frameIndex = 2, direction = 1)
        assertEquals(PlaybackState(0, 1, 0), AnimationPlaybackEngine.tick(s, 3, false, 1))
    }

    @Test fun reversePlaybackLoopsAtStart() {
        val s = PlaybackState(frameIndex = 0, direction = -1)
        assertEquals(PlaybackState(2, -1, 0), AnimationPlaybackEngine.tick(s, 3, false, 1))
    }

    @Test fun pingPongBouncesWithoutSkippingEndpoint() {
        val s = PlaybackState(frameIndex = 2, direction = 1)
        assertEquals(PlaybackState(1, -1, 0), AnimationPlaybackEngine.tick(s, 3, true, 1))
        assertEquals(PlaybackState(0, -1, 0), AnimationPlaybackEngine.tick(PlaybackState(1,-1), 3, true, 1))
    }

    @Test fun singleFrameIsStable() {
        assertEquals(PlaybackState(0, 1, 0), AnimationPlaybackEngine.tick(PlaybackState(0,1,4), 1, true, 4))
    }

    @Test fun fpsDelayIsSafe() {
        assertEquals(83L, AnimationPlaybackEngine.delayMillis(12))
        assertEquals(250L, AnimationPlaybackEngine.delayMillis(12, 3))
        assertEquals(1000L, AnimationPlaybackEngine.delayMillis(1, 1))
        assertTrue(AnimationPlaybackEngine.delayMillis(0) >= 1L)
    }

    @Test fun onionNeighborsStayInBoundsAndExcludeCurrent() {
        assertEquals(listOf(1, 3), OnionSkinEngine.neighborIndices(2, 5, 2, 2))
        assertEquals(listOf(0, 1), OnionSkinEngine.neighborIndices(0, 3, 2, 2))
    }

    @Test fun onionOpacityFallsWithDistance() {
        assertTrue(OnionSkinEngine.opacity(1) < OnionSkinEngine.opacity(0))
    }
}
