package com.smitnk.motioncanvas

data class ProjectIoStroke(val points: List<Pair<Float, Float>>, val inHandles: List<Pair<Float, Float>> = emptyList(), val outHandles: List<Pair<Float, Float>> = emptyList(), val pressures: List<Float> = emptyList(), val colorArgb: Int, val width: Float, val opacity: Float = 1f, val closed: Boolean = false, val filled: Boolean = false)
data class ProjectIoLayer(val name: String, val visible: Boolean = true, val opacity: Float = 1f, val clipToBelow: Boolean = false)
data class ProjectIoLayerFrame(val strokes: List<ProjectIoStroke> = emptyList(), val hold: Int = 1)
data class ProjectIoFrame(val layers: List<ProjectIoLayerFrame> = emptyList())
data class ProjectIoModel(val version: Int, val width: Int, val height: Int, val layers: List<ProjectIoLayer>, val frames: List<ProjectIoFrame>)

object ProjectIoEngine {
    private const val HEADER = "MOTIONCANVAS_PROJECT_V2"
    private const val CURRENT_VERSION = 2

    fun encode(model: ProjectIoModel): String {
        validate(model)
        val out = StringBuilder().append(HEADER).append("\n")
        out.append("version=").append(model.version).append("\n").append("width=").append(model.width).append("\n").append("height=").append(model.height).append("\n").append("layers=").append(model.layers.size).append("\n")
        model.layers.forEachIndexed { i, l -> out.append("layer|").append(i).append("|").append(esc(l.name)).append("|").append(l.visible).append("|").append(l.opacity).append("|").append(l.clipToBelow).append("\n") }
        out.append("frames=").append(model.frames.size).append("\n")
        model.frames.forEachIndexed { fi, frame ->
            require(frame.layers.size == model.layers.size) { "Frame layer count mismatch" }
            out.append("frame|").append(fi).append("|").append(frame.layers.size).append("\n")
            frame.layers.forEachIndexed { li, lf ->
                require(lf.hold >= 1)
                out.append("layerframe|").append(fi).append("|").append(li).append("|").append(lf.hold).append("\n")
                lf.strokes.forEach { s ->
                    validateStroke(s)
                    out.append("stroke|").append(fi).append("|").append(li).append("|").append(s.colorArgb).append("|").append(s.width).append("|").append(s.opacity).append("|").append(s.closed).append("|").append(s.filled).append("|").append(points(s.points)).append("|").append(points(s.inHandles)).append("|").append(points(s.outHandles)).append("|").append(floats(s.pressures)).append("\n")
                }
            }
        }
        return out.toString()
    }

    fun decode(text: String): ProjectIoModel {
        val lines = text.lineSequence().filter(String::isNotBlank).toList()
        require(lines.firstOrNull() == HEADER) { "Unsupported project format" }
        val meta = mutableMapOf<String, String>()
        val layers = mutableMapOf<Int, ProjectIoLayer>()
        val frameLayers = mutableMapOf<Pair<Int, Int>, ProjectIoLayerFrame>()
        val strokes = mutableMapOf<Pair<Int, Int>, MutableList<ProjectIoStroke>>()
        var frameCount = 0

        lines.drop(1).forEach { line ->
            val p = line.split("|")
            when (p[0]) {
                "version", "width", "height", "layers", "frames" -> { require(p.size == 2); meta[p[0]] = p[1] }
                "layer" -> {
                    require(p.size == 6)
                    val index = p[1].toInt().also { require(it >= 0) }
                    require(index !in layers)
                    layers[index] = ProjectIoLayer(unesc(p[2]), p[3].toBooleanStrict(), p[4].toFloat().coerceIn(0f, 1f), p[5].toBooleanStrict())
                }
                "frame" -> {
                    require(p.size == 3)
                    val index = p[1].toInt().also { require(it >= 0) }
                    frameCount = maxOf(frameCount, index + 1)
                }
                "layerframe" -> {
                    require(p.size == 4)
                    val fi = p[1].toInt().also { require(it >= 0) }
                    val li = p[2].toInt().also { require(it >= 0) }
                    val k = fi to li
                    require(k !in frameLayers)
                    val hold = p[3].toInt()
                    require(hold >= 1)
                    frameLayers[k] = ProjectIoLayerFrame(hold = hold)
                    strokes.getOrPut(k) { mutableListOf() }
                }
                "stroke" -> {
                    require(p.size == 12)
                    val fi = p[1].toInt().also { require(it >= 0) }
                    val li = p[2].toInt().also { require(it >= 0) }
                    val k = fi to li
                    require(k in frameLayers)
                    val stroke = ProjectIoStroke(parsePoints(p[8]), parsePoints(p[9]), parsePoints(p[10]), parseFloats(p[11]), p[3].toInt(), p[4].toFloat(), p[5].toFloat().coerceIn(0f, 1f), p[6].toBooleanStrict(), p[7].toBooleanStrict())
                    validateStroke(stroke)
                    strokes.getOrPut(k) { mutableListOf() }.add(stroke)
                }
                else -> error("Unknown project record")
            }
        }

        val version = meta["version"]?.toInt() ?: error("Missing version")
        require(version in 1..CURRENT_VERSION) { "Unsupported project version: $version" }
        val width = meta["width"]?.toInt() ?: error("Missing width")
        val height = meta["height"]?.toInt() ?: error("Missing height")
        val layerCount = meta["layers"]?.toInt() ?: error("Missing layer count")
        val declaredFrames = meta["frames"]?.toInt() ?: error("Missing frame count")
        require(width > 0 && height > 0 && layerCount > 0 && declaredFrames > 0)
        require(frameCount <= declaredFrames)
        require(layers.keys.all { it < layerCount })

        val ordered = (0 until layerCount).map { layers[it] ?: ProjectIoLayer("Layer " + (it + 1)) }
        val frames = (0 until declaredFrames).map { fi ->
            ProjectIoFrame((0 until layerCount).map { li ->
                val k = fi to li
                (frameLayers[k] ?: ProjectIoLayerFrame()).copy(strokes = strokes[k]?.toList() ?: emptyList())
            })
        }
        return ProjectIoModel(version, width, height, ordered, frames)
    }

    private fun validate(model: ProjectIoModel) {
        require(model.version in 1..CURRENT_VERSION)
        require(model.width > 0 && model.height > 0)
        require(model.layers.isNotEmpty() && model.frames.isNotEmpty())
        model.layers.forEach { require(it.opacity.isFinite() && it.opacity in 0f..1f) }
        model.frames.forEach { frame ->
            require(frame.layers.size == model.layers.size)
            frame.layers.forEach { lf -> require(lf.hold >= 1); lf.strokes.forEach(::validateStroke) }
        }
    }

    private fun validateStroke(s: ProjectIoStroke) {
        require(s.width.isFinite() && s.width > 0f)
        require(s.opacity.isFinite() && s.opacity in 0f..1f)
        require(s.pressures.all { it.isFinite() })
        require(s.points.all { it.first.isFinite() && it.second.isFinite() })
        require(s.inHandles.all { it.first.isFinite() && it.second.isFinite() })
        require(s.outHandles.all { it.first.isFinite() && it.second.isFinite() })
    }

    private fun esc(s: String) = s.replace("%", "%25").replace("|", "%7C").replace("\n", "%0A")
    private fun unesc(s: String) = s.replace("%0A", "\n").replace("%7C", "|").replace("%25", "%")
    private fun points(v: List<Pair<Float, Float>>) = v.joinToString(";") { it.first.toString() + "," + it.second }
    private fun floats(v: List<Float>) = v.joinToString(";")
    private fun parsePoints(s: String) = if (s.isEmpty()) emptyList() else s.split(";").map { val p = it.split(","); require(p.size == 2); p[0].toFloat() to p[1].toFloat() }
    private fun parseFloats(s: String) = if (s.isEmpty()) emptyList() else s.split(";").map(String::toFloat)
}