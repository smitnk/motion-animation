package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Stroke(
    val points: List<Offset>,
    val inHandles: List<Offset> = emptyList(),
    val outHandles: List<Offset> = emptyList(),
    val pressures: List<Float> = emptyList(),
    val color: Color,
    val width: Float,
    val opacity: Float = 1f,
    val closed: Boolean = false,
    val filled: Boolean = false
)

data class ArtLayer(
    val name: String,
    val visible: Boolean = true,
    val opacity: Float = 1f,
    val clipToBelow: Boolean = false
)

data class LayerFrame(
    val strokes: List<Stroke> = emptyList(),
    val hold: Int = 1
)

data class Frame(
    val layers: List<LayerFrame> = emptyList()
)

enum class Tool {
    BRUSH, ERASER, LINE, RECTANGLE, ELLIPSE, SELECT, FILL, EYEDROPPER
}
