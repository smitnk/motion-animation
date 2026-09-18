package com.smitnk.motioncanvas

data class HsvColor(val hue: Float, val saturation: Float, val value: Float, val alpha: Float = 1f)
data class ShapeSpec(val type: ShapeType, val start: Pair<Float,Float>, val end: Pair<Float,Float>)
enum class ShapeType { LINE, RECTANGLE, ELLIPSE }

object ColorShapeEngine {
 fun normalizeHue(h: Float)=((h%360f)+360f)%360f
 fun hsv(h: Float,s: Float,v: Float,a: Float=1f)=HsvColor(normalizeHue(h),s.coerceIn(0f,1f),v.coerceIn(0f,1f),a.coerceIn(0f,1f))
 fun hexToArgb(hex:String):Int {
   val clean=hex.trim().removePrefix("#"); require(clean.length==6 || clean.length==8) { "Hex color must be RRGGBB or AARRGGBB" }
   val value=clean.toLongOrNull(16) ?: error("Invalid hex color")
   return if(clean.length==6) (0xFF000000L or value).toInt() else value.toInt()
 }
 fun argbToHex(argb:Int, includeAlpha:Boolean=false):String {
   return if(includeAlpha) "#%08X".format(argb) else "#%06X".format(argb and 0xFFFFFF)
 }
 fun rectangle(start:Pair<Float,Float>,end:Pair<Float,Float>,closed:Boolean=true):List<Pair<Float,Float>> {
   val l=minOf(start.first,end.first); val r=maxOf(start.first,end.first); val t=minOf(start.second,end.second); val b=maxOf(start.second,end.second)
   return listOf(l to t,r to t,r to b,l to b).let { if(closed) it+it.first() else it }
 }
 fun line(start:Pair<Float,Float>,end:Pair<Float,Float>)=listOf(start,end)
 fun ellipse(start:Pair<Float,Float>,end:Pair<Float,Float>,segments:Int=32):List<Pair<Float,Float>> {
   val n=segments.coerceAtLeast(8); val cx=(start.first+end.first)/2f; val cy=(start.second+end.second)/2f; val rx=kotlin.math.abs(end.first-start.first)/2f; val ry=kotlin.math.abs(end.second-start.second)/2f
   return (0..n).map { i -> val a=2.0*Math.PI*i/n; (cx+rx*kotlin.math.cos(a)).toFloat() to (cy+ry*kotlin.math.sin(a)).toFloat() }
 }
 fun snapAngle(start:Pair<Float,Float>,end:Pair<Float,Float>,increment:Float=15f):Pair<Float,Float> {
   require(increment>0f); val dx=end.first-start.first; val dy=end.second-start.second; val length=kotlin.math.hypot(dx.toDouble(),dy.toDouble()).toFloat(); if(length==0f)return end; val angle=kotlin.math.atan2(dy,dx); val snapped=kotlin.math.round(Math.toDegrees(angle.toDouble())/increment)*increment; val rad=Math.toRadians(snapped); return (start.first+length*kotlin.math.cos(rad)).toFloat() to (start.second+length*kotlin.math.sin(rad)).toFloat()
 }
}