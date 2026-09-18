package com.smitnk.motioncanvas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class ColorShapeEngineTest {
 @Test fun hexRoundTripPreservesRgb(){ val c=ColorShapeEngine.hexToArgb("#12ABEF"); assertEquals("#12ABEF",ColorShapeEngine.argbToHex(c)) }
 @Test fun alphaHexIsSupported(){ val c=ColorShapeEngine.hexToArgb("#8012ABEF"); assertEquals("#8012ABEF",ColorShapeEngine.argbToHex(c,true)) }
 @Test fun hsvInputsAreClampedAndHueWraps(){ val c=ColorShapeEngine.hsv(390f,1.4f,-.2f,2f); assertEquals(30f,c.hue); assertEquals(1f,c.saturation); assertEquals(0f,c.value); assertEquals(1f,c.alpha) }
 @Test fun rectangleIsClosedAndNormalizesDragDirection(){ val p=ColorShapeEngine.rectangle(10f to 20f,0f to 0f); assertEquals(listOf(0f to 0f,10f to 0f,10f to 20f,0f to 20f,0f to 0f),p) }
 @Test fun ellipseHasClosedSampleAndExpectedCount(){ val p=ColorShapeEngine.ellipse(0f to 0f,20f to 10f,16); assertEquals(17,p.size); assertEquals(p.first(),p.last()) }
 @Test fun angleSnapUsesRequestedIncrement(){ val p=ColorShapeEngine.snapAngle(0f to 0f,10f to 8f,15f); assertTrue(kotlin.math.abs(p.second-p.first*kotlin.math.tan(Math.toRadians(45.0)).toFloat())<0.01f) }
}