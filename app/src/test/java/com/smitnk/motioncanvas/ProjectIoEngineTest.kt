package com.smitnk.motioncanvas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
class ProjectIoEngineTest {
 private fun model()=ProjectIoModel(2,1600,1200,listOf(ProjectIoLayer("Ink|Main",true,.75f,true),ProjectIoLayer("Color",false,.5f)),listOf(ProjectIoFrame(listOf(ProjectIoLayerFrame(listOf(ProjectIoStroke(listOf(1f to 2f,3f to 4f),listOf(0f to 0f,-1f to -2f),listOf(2f to 3f,0f to 0f),listOf(.2f,.8f),-16777216,10f,.9f,true,true)),3),ProjectIoLayerFrame()))))
 @Test fun roundTripPreservesCompleteVectorState(){ val m=model(); assertEquals(m,ProjectIoEngine.decode(ProjectIoEngine.encode(m))) }
 @Test fun malformedHeaderIsRejected(){ assertThrows(IllegalArgumentException::class.java){ProjectIoEngine.decode("bad")} }
 @Test fun missingLayerGetsSafeDefault(){ val m=model(); val text=ProjectIoEngine.encode(m).replace("layer|1|Color|false|0.5|false\n",""); assertEquals("Layer 2",ProjectIoEngine.decode(text).layers[1].name) }
}