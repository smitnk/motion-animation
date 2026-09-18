package com.smitnk.motioncanvas

data class ProjectIoStroke(val points: List<Pair<Float, Float>>, val inHandles: List<Pair<Float, Float>> = emptyList(), val outHandles: List<Pair<Float, Float>> = emptyList(), val pressures: List<Float> = emptyList(), val colorArgb: Int, val width: Float, val opacity: Float = 1f, val closed: Boolean = false, val filled: Boolean = false)
data class ProjectIoLayer(val name: String, val visible: Boolean = true, val opacity: Float = 1f, val clipToBelow: Boolean = false)
data class ProjectIoLayerFrame(val strokes: List<ProjectIoStroke> = emptyList(), val hold: Int = 1)
data class ProjectIoFrame(val layers: List<ProjectIoLayerFrame> = emptyList())
data class ProjectIoModel(val version: Int, val width: Int, val height: Int, val layers: List<ProjectIoLayer>, val frames: List<ProjectIoFrame>)

object ProjectIoEngine {
    private const val HEADER = "MOTIONCANVAS_PROJECT_V2"
    fun encode(model: ProjectIoModel): String {
        val out = StringBuilder().append(HEADER).append("\n")
        out.append("version=").append(model.version).append("\n").append("width=").append(model.width).append("\n").append("height=").append(model.height).append("\n")
        out.append("layers=").append(model.layers.size).append("\n")
        model.layers.forEachIndexed { i, l -> out.append("layer|").append(i).append("|").append(esc(l.name)).append("|").append(l.visible).append("|").append(l.opacity).append("|").append(l.clipToBelow).append("\n") }
        out.append("frames=").append(model.frames.size).append("\n")
        model.frames.forEachIndexed { fi, frame ->
            out.append("frame|").append(fi).append("|").append(frame.layers.size).append("\n")
            frame.layers.forEachIndexed { li, lf ->
                out.append("layerframe|").append(fi).append("|").append(li).append("|").append(lf.hold).append("\n")
                lf.strokes.forEach { s -> out.append("stroke|").append(fi).append("|").append(li).append("|").append(s.colorArgb).append("|").append(s.width).append("|").append(s.opacity).append("|").append(s.closed).append("|").append(s.filled).append("|").append(points(s.points)).append("|").append(points(s.inHandles)).append("|").append(points(s.outHandles)).append("|").append(floats(s.pressures)).append("\n") }
            }
        }
        return out.toString()
    }
    fun decode(text: String): ProjectIoModel {
        val lines = text.lineSequence().filter(String::isNotBlank).toList()
        require(lines.firstOrNull() == HEADER) { "Unsupported project format" }
        val meta = mutableMapOf<String, String>(); val layers = mutableMapOf<Int, ProjectIoLayer>(); val frameLayers = mutableMapOf<Pair<Int,Int>, ProjectIoLayerFrame>(); val strokes = mutableMapOf<Pair<Int,Int>, MutableList<ProjectIoStroke>>(); var frameCount = 0
        lines.drop(1).forEach { line ->
            val p = line.split("|")
            when (p[0]) {
                "version", "width", "height", "layers", "frames" -> meta[p[0]] = p.getOrElse(1) { "" }
                "layer" -> { require(p.size == 6); layers[p[1].toInt()] = ProjectIoLayer(unesc(p[2]), p[3].toBooleanStrict(), p[4].toFloat().coerceIn(0f,1f), p[5].toBooleanStrict()) }
                "frame" -> { require(p.size == 3); frameCount = maxOf(frameCount, p[1].toInt() + 1) }
                "layerframe" -> { require(p.size == 4); val k = p[1].toInt() to p[2].toInt(); frameLayers[k] = ProjectIoLayerFrame(hold=p[3].toInt().coerceAtLeast(1)); strokes.getOrPut(k){ mutableListOf() } }
                "stroke" -> { require(p.size == 12); val k=p[1].toInt() to p[2].toInt(); strokes.getOrPut(k){mutableListOf()}.add(ProjectIoStroke(parsePoints(p[8]),parsePoints(p[9]),parsePoints(p[10]),parseFloats(p[11]),p[3].toInt(),p[4].toFloat(),p[5].toFloat().coerceIn(0f,1f),p[6].toBooleanStrict(),p[7].toBooleanStrict())) }
            }
        }
        val width=meta["width"]?.toInt()?:error("Missing width"); val height=meta["height"]?.toInt()?:error("Missing height"); val layerCount=meta["layers"]?.toInt()?:error("Missing layer count"); val count=maxOf(frameCount,meta["frames"]?.toInt()?:0)
        require(width>0 && height>0 && layerCount>0 && count>0)
        val ordered=(0 until layerCount).map { layers[it] ?: ProjectIoLayer("Layer "+(it+1)) }
        val frames=(0 until count).map { fi -> ProjectIoFrame((0 until layerCount).map { li -> val k=fi to li; (frameLayers[k]?:ProjectIoLayerFrame()).copy(strokes=strokes[k]?.toList()?:emptyList()) }) }
        return ProjectIoModel(meta["version"]?.toInt()?:2,width,height,ordered,frames)
    }
    private fun esc(s:String)=s.replace("%","%25").replace("|","%7C").replace("\n","%0A")
    private fun unesc(s:String)=s.replace("%0A","\n").replace("%7C","|").replace("%25","%")
    private fun points(v:List<Pair<Float,Float>>)=v.joinToString(";"){it.first.toString()+","+it.second}
    private fun floats(v:List<Float>)=v.joinToString(";")
    private fun parsePoints(s:String)=if(s.isEmpty()) emptyList() else s.split(";").map { val p=it.split(","); require(p.size==2); p[0].toFloat() to p[1].toFloat() }
    private fun parseFloats(s:String)=if(s.isEmpty()) emptyList() else s.split(";").map(String::toFloat)
}