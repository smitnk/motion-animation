package com.smitnk.motioncanvas

import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineEngineTest {
    private fun frame(hold: Int = 1) = Frame(listOf(LayerFrame(hold = hold)))

    @Test fun insertAndDuplicatePreserveOrder() {
        val a = frame()
        val b = frame()
        val c = frame()
        val inserted = TimelineEngine.insertFrame(listOf(a, b), 1, c)
        assertEquals(listOf(a, c, b), inserted)
        assertEquals(listOf(a, c, c, b), TimelineEngine.duplicateFrame(inserted, 1))
    }

    @Test fun deleteNeverLeavesEmptyTimeline() {
        val only = frame()
        assertEquals(listOf(only), TimelineEngine.deleteFrame(listOf(only), 0))
        val a = frame()
        val b = frame()
        assertEquals(listOf(b), TimelineEngine.deleteFrame(listOf(a, b), 0))
    }

    @Test fun moveClampsTargetAndPreservesFrames() {
        val a = frame()
        val b = frame()
        val c = frame()
        assertEquals(listOf(b, c, a), TimelineEngine.moveFrame(listOf(a, b, c), 0, 99))
        assertEquals(listOf(c, a, b), TimelineEngine.moveFrame(listOf(a, b, c), 2, -5))
    }

    @Test fun setHoldUpdatesEveryLayer() {
        val input = Frame(listOf(LayerFrame(hold = 1), LayerFrame(hold = 3)))
        val result = TimelineEngine.setHold(listOf(input), 0, 4)
        assertEquals(listOf(4, 4), result[0].layers.map { it.hold })
    }

    @Test fun exposureSequenceExpandsHolds() {
        assertEquals(listOf(0, 0, 1, 2, 2, 2), TimelineEngine.exposureSequence(listOf(frame(2), frame(1), frame(3))))
    }
}
