package com.smitnk.motioncanvas

import android.os.Bundle
import android.content.ContentValues
import android.provider.MediaStore
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import java.util.concurrent.TimeUnit
import com.squareup.gifencoder.GifEncoder
import com.squareup.gifencoder.ImageOptions

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
data class ArtLayer(val name: String, val visible: Boolean = true, val opacity: Float = 1f, val clipToBelow: Boolean = false)
data class LayerFrame(val strokes: List<Stroke> = emptyList(), val hold: Int = 1)
data class Frame(val layers: List<LayerFrame> = emptyList())
enum class Tool { BRUSH, ERASER, LINE, RECTANGLE, ELLIPSE, SELECT, FILL, EYEDROPPER }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MotionCanvasApp() }
    }
}

fun pointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.lastIndex
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[j]
        val intersects = ((a.y > point.y) != (b.y > point.y)) &&
            (point.x < (b.x - a.x) * (point.y - a.y) / ((b.y - a.y).takeIf { it != 0f } ?: 0.0001f) + a.x)
        if (intersects) inside = !inside
        j = i
    }
    return inside
}

fun transformPoints(
    points: List<Offset>,
    center: Offset,
    scale: Float,
    degrees: Float,
    delta: Offset = Offset.Zero
): List<Offset> {
    val r = degrees * PI.toFloat() / 180f
    val c = cos(r)
    val s = sin(r)
    return points.map { p ->
        val x = (p.x - center.x) * scale
        val y = (p.y - center.y) * scale
        Offset(
            center.x + x * c - y * s + delta.x,
            center.y + x * s + y * c + delta.y
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotionCanvasApp() {
    var layers by remember { mutableStateOf(listOf(ArtLayer("Layer 1"))) }
    var frameData by remember { mutableStateOf(listOf(Frame(listOf(LayerFrame())))) }
    var currentStrokes by remember { mutableStateOf(listOf(emptyList<Stroke>())) }
    val rasterWidth = 1600
    val rasterHeight = 1200
    var rasterLayers by remember { mutableStateOf(listOf(Bitmap.createBitmap(rasterWidth, rasterHeight, Bitmap.Config.ARGB_8888))) }
    var rasterFrames by remember { mutableStateOf(listOf(listOf(Bitmap.createBitmap(rasterWidth, rasterHeight, Bitmap.Config.ARGB_8888)))) }
    var selectedLayer by remember { mutableIntStateOf(0) }
    var current by remember { mutableStateOf(emptyList<Offset>()) }
    var currentPressures by remember { mutableStateOf(emptyList<Float>()) }
    var selection by remember { mutableStateOf(emptyList<Offset>()) }
    var selectedStrokeIds by remember { mutableStateOf(emptySet<Int>()) }
    var tool by remember { mutableStateOf(Tool.BRUSH) }
    var brush by remember { mutableStateOf(Color.Black) }
    var showColorPicker by remember { mutableStateOf(false) }
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var colorAlpha by remember { mutableFloatStateOf(1f) }
    var hexColor by remember { mutableStateOf("#000000") }
    var recentColors by remember { mutableStateOf(listOf(Color.Black, Color.White, Color.Red, Color.Blue)) }
    var brushType by remember { mutableStateOf("Pencil") }
    var pressureEnabled by remember { mutableStateOf(true) }
    var width by remember { mutableFloatStateOf(10f) }
    var opacity by remember { mutableFloatStateOf(1f) }
    var stabilization by remember { mutableFloatStateOf(0.35f) }
    var streamline by remember { mutableFloatStateOf(0.35f) }
    var deepBrushEngine by remember { mutableStateOf(true) }
    var quickShape by remember { mutableStateOf(true) }
    var editStrokeIndex by remember { mutableStateOf<Int?>(null) }
    var editNodeIndex by remember { mutableIntStateOf(-1) }
    var nodeEditorMode by remember { mutableStateOf(false) }
    var bezierHandleMode by remember { mutableStateOf(false) }
    var activeHandle by remember { mutableIntStateOf(-1) }
    var activeHandleSide by remember { mutableStateOf("out") }
    var editShapeLabel by remember { mutableStateOf("Edit Shape") }
    var weightPaintMode by remember { mutableStateOf(false) }
    var weightJoint by remember { mutableIntStateOf(-1) }
    var weightRadius by remember { mutableFloatStateOf(180f) }
    var weightStrength by remember { mutableFloatStateOf(0.75f) }
    var weightJoints by remember { mutableStateOf(listOf<Offset>()) }
    var rigJoints by remember { mutableStateOf(listOf<Offset>()) }
    var rigParents by remember { mutableStateOf(listOf<Int>()) }
    var rigMode by remember { mutableStateOf(false) }
    var rigSelected by remember { mutableIntStateOf(-1) }
    var sculptMode by remember { mutableStateOf(false) }
    var sculptTool by remember { mutableStateOf("Grab") }
    var sculptRadius by remember { mutableFloatStateOf(140f) }
    var sculptStrength by remember { mutableFloatStateOf(0.65f) }
    var sculptSnapshotTaken by remember { mutableStateOf(false) }
    var spacing by remember { mutableFloatStateOf(0.18f) }
    var taper by remember { mutableFloatStateOf(0f) }
    var shapeFilled by remember { mutableStateOf(false) }
    var symmetry by remember { mutableStateOf(false) }
    var symmetryAxis by remember { mutableFloatStateOf(0.5f) }
    var radialSymmetry by remember { mutableStateOf(false) }
    var radialCount by remember { mutableIntStateOf(6) }
    var alphaLock by remember { mutableStateOf(false) }
    var clippingMask by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(false) }
    var gridType by remember { mutableStateOf("2D") }
    var gridSpacing by remember { mutableFloatStateOf(100f) }
    var perspectivePoints by remember { mutableIntStateOf(1) }
    var pingPong by remember { mutableStateOf(false) }
    var playDirection by remember { mutableIntStateOf(1) }
    data class EditorSnapshot(
        val strokes: List<List<Stroke>>,
        val rasters: List<Bitmap>
    )
    var undo by remember { mutableStateOf(emptyList<EditorSnapshot>()) }
    var redo by remember { mutableStateOf(emptyList<EditorSnapshot>()) }
    var canvasScale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size(rasterWidth.toFloat(), rasterHeight.toFloat())) }
    var onionSkin by remember { mutableStateOf(true) }
    var fps by remember { mutableIntStateOf(12) }
    var frameIndex by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun artScale(): Float {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) return 1f
        return minOf(canvasSize.width / rasterWidth.toFloat(), canvasSize.height / rasterHeight.toFloat()) * 0.92f
    }

    fun screenToArt(p: Offset): Offset {
        val base = artScale()
        val cx = canvasSize.width / 2f + pan.x
        val cy = canvasSize.height / 2f + pan.y
        val dx = p.x - cx
        val dy = p.y - cy
        val r = -rotation * PI.toFloat() / 180f
        val c = cos(r); val s = sin(r)
        return Offset(
            (dx * c - dy * s) / (base * canvasScale) + rasterWidth / 2f,
            (dx * s + dy * c) / (base * canvasScale) + rasterHeight / 2f
        )
    }

    fun copyBitmap(source: Bitmap): Bitmap = source.copy(Bitmap.Config.ARGB_8888, true)

    fun recycleBitmaps(bitmaps: Iterable<Bitmap>) {
        bitmaps.forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    fun saveRasterFrame() {
        if (frameIndex !in rasterFrames.indices) return
        val replacement = rasterLayers.map { bitmap -> copyBitmap(bitmap) }
        val old = rasterFrames[frameIndex]
        rasterFrames = rasterFrames.toMutableList().also { it[frameIndex] = replacement }
        recycleBitmaps(old)
    }

    fun loadRasterFrame(index: Int) {
        if (index !in rasterFrames.indices) return
        val replacement = rasterFrames[index].map { bitmap -> copyBitmap(bitmap) }
        val old = rasterLayers
        rasterLayers = replacement
        recycleBitmaps(old)
    }

    fun saveFrame() {
        if (frameIndex !in frameData.indices) return
        saveRasterFrame()
        val old = frameData[frameIndex]
        frameData = frameData.toMutableList().also {
            it[frameIndex] = Frame(currentStrokes.mapIndexed { i, strokes ->
                LayerFrame(strokes, old.layers.getOrNull(i)?.hold ?: 1)
            })
        }
    }


    fun renderFrameBitmap(index: Int): Bitmap {
        val merged = Bitmap.createBitmap(rasterWidth, rasterHeight, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(merged)
        canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        rasterFrames.getOrNull(index)?.forEachIndexed { i, bitmap ->
            if (layers.getOrNull(i)?.visible == true) {
                val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)
                paint.alpha = (layers[i].opacity.coerceIn(0f, 1f) * 255f).toInt()
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
        }
        frameData.getOrNull(index)?.layers?.forEachIndexed { i, layer ->
            if (layers.getOrNull(i)?.visible != true) return@forEachIndexed
            layer.strokes.forEach { stroke ->
                if (stroke.points.isEmpty()) return@forEach
                val path = android.graphics.Path().apply {
                    moveTo(stroke.points.first().x, stroke.points.first().y)
                    if (stroke.inHandles.size == stroke.points.size && stroke.outHandles.size == stroke.points.size) {
                        for (j in 0 until stroke.points.lastIndex) {
                            val a = stroke.points[j]
                            val b = stroke.points[j + 1]
                            val c1 = android.graphics.PointF(a.x + stroke.outHandles[j].x, a.y + stroke.outHandles[j].y)
                            val c2 = android.graphics.PointF(b.x + stroke.inHandles[j + 1].x, b.y + stroke.inHandles[j + 1].y)
                            cubicTo(c1.x, c1.y, c2.x, c2.y, b.x, b.y)
                        }
                    } else stroke.points.drop(1).forEach { lineTo(it.x, it.y) }
                    if (stroke.closed) close()
                }
                val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)
                paint.color = stroke.color.copy(alpha = stroke.opacity * layers[i].opacity).toArgb()
                paint.style = if (stroke.filled) AndroidPaint.Style.FILL else AndroidPaint.Style.STROKE
                paint.strokeWidth = stroke.width
                paint.strokeCap = AndroidPaint.Cap.ROUND
                canvas.drawPath(path, paint)
            }
        }
        return merged
    }


    fun projectJson(): String {
        val model = ProjectIoModel(
            version = 2,
            width = rasterWidth,
            height = rasterHeight,
            layers = layers.map { ProjectIoLayer(it.name, it.visible, it.opacity, it.clipToBelow) },
            frames = frameData.map { frame ->
                ProjectIoFrame(frame.layers.map { lf ->
                    ProjectIoLayerFrame(
                        strokes = lf.strokes.map { s ->
                            ProjectIoStroke(
                                points = s.points.map { it.x to it.y },
                                inHandles = s.inHandles.map { it.x to it.y },
                                outHandles = s.outHandles.map { it.x to it.y },
                                pressures = s.pressures,
                                colorArgb = s.color.toArgb(),
                                width = s.width,
                                opacity = s.opacity,
                                closed = s.closed,
                                filled = s.filled
                            )
                        },
                        hold = lf.hold
                    )
                })
            }
        )
        return ProjectIoEngine.encode(model)
    }

    fun saveProject(uri: Uri) {
        try {
            saveFrame()
            context.contentResolver.openOutputStream(uri)?.use { output ->
                ZipOutputStream(output).use { zip ->
                    zip.putNextEntry(ZipEntry("project.txt"))
                    zip.write(projectJson().toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    rasterFrames.forEachIndexed { fi, frame ->
                        frame.forEachIndexed { li, bitmap ->
                            zip.putNextEntry(ZipEntry("frames/f${fi}_l${li}.png"))
                            bitmap.compress(CompressFormat.PNG, 100, zip)
                            zip.closeEntry()
                        }
                    }
                }
            } ?: error("Unable to open project output")
            exportStatus = "Project saved"
        } catch (e: Exception) {
            exportStatus = "Save failed"
        }
    }

    fun loadProject(uri: Uri) {
        try {
            val temp = mutableMapOf<String, ByteArray>()
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        if (!entry.isDirectory) temp[entry.name] = zip.readBytes()
                    }
                }
            } ?: error("Unable to open project")

            val projectBytes = temp["project.txt"] ?: temp["project.json"] ?: error("Missing project data")
            val model = if (projectBytes.toString(Charsets.UTF_8).startsWith("MOTIONCANVAS_PROJECT_V2")) {
                ProjectIoEngine.decode(projectBytes.toString(Charsets.UTF_8))
            } else {
                error("Legacy JSON project is not supported by Phase 13 loader")
            }

            require(model.width == rasterWidth && model.height == rasterHeight) { "Unsupported canvas size" }

            fun ProjectIoStroke.toStroke() = Stroke(
                points = points.map { Offset(it.first, it.second) },
                inHandles = inHandles.map { Offset(it.first, it.second) },
                outHandles = outHandles.map { Offset(it.first, it.second) },
                pressures = pressures,
                color = Color(colorArgb),
                width = width,
                opacity = opacity,
                closed = closed,
                filled = filled
            )

            val loadedLayers = model.layers.map { ArtLayer(it.name, it.visible, it.opacity, it.clipToBelow) }
            val loadedFrames = model.frames.map { frame ->
                Frame(frame.layers.map { lf ->
                    LayerFrame(lf.strokes.map { it.toStroke() }, lf.hold)
                })
            }

            val loadedRasters = model.frames.mapIndexed { fi, _ ->
                loadedLayers.indices.map { li ->
                    val bytes = temp["frames/f${fi}_l${li}.png"]
                    bytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) }
                        ?: Bitmap.createBitmap(model.width, model.height, Bitmap.Config.ARGB_8888)
                }
            }

            val oldRasterFrames = rasterFrames
            val oldRasterLayers = rasterLayers
            layers = loadedLayers
            frameData = loadedFrames
            rasterFrames = loadedRasters
            frameIndex = 0
            currentStrokes = loadedFrames.first().layers.map { it.strokes }
            rasterLayers = loadedRasters.first().map { copyBitmap(it) }
            recycleBitmaps(oldRasterFrames.flatten())
            recycleBitmaps(oldRasterLayers)
            selectedLayer = selectedLayer.coerceIn(0, loadedLayers.lastIndex)
            selectedStrokeIds = emptySet()
            selection = emptyList()
            exportStatus = "Project loaded"
        } catch (e: Exception) {
            exportStatus = "Load failed"
        }
    }
    fun exportCurrentPng() {
        try {
            saveFrame()
            val merged = renderFrameBitmap(frameIndex)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "MotionCanvas_F${frameIndex + 1}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MotionCanvas")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create image entry")
            try {
                val written = resolver.openOutputStream(uri)?.use {
                    merged.compress(CompressFormat.PNG, 100, it)
                } ?: false
                if (!written) error("Unable to write PNG")
                exportStatus = "PNG exported to Pictures/MotionCanvas"
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            } finally {
                merged.recycle()
            }
        } catch (e: Exception) {
            exportStatus = "PNG export failed: " + (e.message ?: "unknown error")
        }
    }

    fun exportGif(uri: Uri) {
        try {
            saveFrame()
            if (frameData.isEmpty()) error("No animation frames")
            context.contentResolver.openOutputStream(uri)?.use { output ->
                val encoder = GifEncoder(output, rasterWidth, rasterHeight, 0)
                val options = ImageOptions()
                options.setDelay((1000L / fps.coerceAtLeast(1)).coerceAtLeast(1L), TimeUnit.MILLISECONDS)
                val pixels = IntArray(rasterWidth * rasterHeight)
                val data = Array(rasterWidth) { IntArray(rasterHeight) }
                frameData.indices.forEach { index ->
                    val bitmap = renderFrameBitmap(index)
                    try {
                        bitmap.getPixels(pixels, 0, rasterWidth, 0, 0, rasterWidth, rasterHeight)
                        for (x in 0 until rasterWidth) {
                            val column = data[x]
                            var offset = x
                            for (y in 0 until rasterHeight) {
                                column[y] = pixels[offset]
                                offset += rasterWidth
                            }
                        }
                        val hold = frameData[index].layers.firstOrNull()?.hold?.coerceAtLeast(1) ?: 1
                        repeat(hold) { encoder.addImage(data, options) }
                    } finally {
                        bitmap.recycle()
                    }
                }
                encoder.finishEncoding()
            } ?: error("Unable to open GIF output")
            exportStatus = "GIF exported"
        } catch (e: Exception) {
            exportStatus = "GIF export failed: " + (e.message ?: "unknown error")
        }
    }}