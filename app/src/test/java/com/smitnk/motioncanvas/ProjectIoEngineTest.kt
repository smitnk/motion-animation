package com.smitnk.motioncanvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProjectIoEngineTest {
    private fun model() = ProjectIoModel(2, 1600, 1200,
        listOf(ProjectIoLayer("Ink|Main", true, .75f, true), ProjectIoLayer("Color", false, .5f)),
        listOf(
            ProjectIoFrame(listOf(ProjectIoLayerFrame(listOf(ProjectIoStroke(listOf(1f to 2f, 3f to 4f), listOf(0f to 0f, -1f to -2f), listOf(2f to 3f, 0f to 0f), listOf(.2f, .8f), -16777216, 10f, .9f, true, true)), 3), ProjectIoLayerFrame())),
            ProjectIoFrame(listOf(ProjectIoLayerFrame(), ProjectIoLayerFrame()))
        )
    )

    @Test fun roundTripPreservesCompleteVectorState() { val m = model(); assertEquals(m, ProjectIoEngine.decode(ProjectIoEngine.encode(m))) }
    @Test fun malformedHeaderIsRejected() { assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.decode("bad") } }
    @Test fun missingLayerGetsSafeDefault() {
        val text = ProjectIoEngine.encode(model()).replace("layer|1|Color|false|0.5|false\n", "")
        assertEquals("Layer 2", ProjectIoEngine.decode(text).layers[1].name)
    }
    @Test fun duplicateLayerRecordsAreRejected() {
        val text = ProjectIoEngine.encode(model()) + "layer|0|Duplicate|true|1.0|false\n"
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.decode(text) }
    }
    @Test fun unsupportedVersionIsRejected() {
        val text = ProjectIoEngine.encode(model()).replace("version=2", "version=99")
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.decode(text) }
    }
    @Test fun invalidStrokeNumbersAreRejected() {
        val text = ProjectIoEngine.encode(model()).replace("|10.0|", "|NaN|")
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.decode(text) }
    }
    @Test fun mismatchedPressureCountIsRejected() {
        val badStroke = ProjectIoStroke(listOf(1f to 2f, 3f to 4f), pressures = listOf(1f), colorArgb = -16777216, width = 10f)
        val bad = model().copy(frames = listOf(ProjectIoFrame(listOf(ProjectIoLayerFrame(listOf(badStroke), 1), ProjectIoLayerFrame()))))
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.encode(bad) }
    }

    @Test fun mismatchedBezierHandleCountIsRejected() {
        val badStroke = ProjectIoStroke(listOf(1f to 2f, 3f to 4f), inHandles = listOf(0f to 0f), colorArgb = -16777216, width = 10f)
        val bad = model().copy(frames = listOf(ProjectIoFrame(listOf(ProjectIoLayerFrame(listOf(badStroke), 1), ProjectIoLayerFrame()))))
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.encode(bad) }
    }

    @Test fun frameLayerCountMustMatchModelOnEncode() {
        val bad = model().copy(frames = listOf(ProjectIoFrame(listOf(ProjectIoLayerFrame()))))
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.encode(bad) }
    }
    @Test fun duplicateMetadataRecordsAreRejected() {
        val text = ProjectIoEngine.encode(model()) + "version=2\n"
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.decode(text) }
    }

    @Test fun duplicateFrameRecordsAreRejected() {
        val text = ProjectIoEngine.encode(model()) + "frame|0|2\n"
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.decode(text) }
    }

    @Test fun frameRecordOutsideDeclaredRangeIsRejected() {
        val text = ProjectIoEngine.encode(model()) + "frame|9|2\n"
        assertThrows(IllegalArgumentException::class.java) { ProjectIoEngine.decode(text) }
    }


}