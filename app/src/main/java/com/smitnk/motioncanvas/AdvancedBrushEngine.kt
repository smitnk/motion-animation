package com.smitnk.motioncanvas

import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Stateless helpers for converting pointer samples into stable brush samples.
 * Kept independent from Compose UI state so the brush behavior can be tested.
 */
object AdvancedBrushEngine {
    data class Sample(val point: Offset, val pressure: Float = 1f)

    fun smooth(samples: List<Sample>, strength: Float): List<Sample> {
        if (samples.size < 3) return samples
        val s = strength.coerceIn(0f, 1f)
        if (s == 0f) return samples
        return samples.indices.map { i ->
            if (i == 0 || i == samples.lastIndex) samples[i]
            else {
                val p = samples[i - 1].point
                val c = samples[i].point
                val n = samples[i + 1].point
                val average = Offset((p.x + c.x + n.x) / 3f, (p.y + c.y + n.y) / 3f)
                Sample(
                    Offset(c.x + (average.x - c.x) * s, c.y + (average.y - c.y) * s),
                    samples[i].pressure
                )
            }
        }
    }

    fun resample(samples: List<Sample>, spacing: Float): List<Sample> {
        if (samples.size < 2) return samples
        val step = max(0.1f, spacing)
        val result = mutableListOf(samples.first())
        var carry = 0f
        var previous = samples.first()
        for (i in 1 until samples.size) {
            val current = samples[i]
            val dx = current.point.x - previous.point.x
            val dy = current.point.y - previous.point.y
            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
            if (distance <= 0f) continue
            var travelled = step - carry
            while (travelled <= distance) {
                val t = travelled / distance
                result += Sample(
                    Offset(
                        previous.point.x + dx * t,
                        previous.point.y + dy * t
                    ),
                    previous.pressure + (current.pressure - previous.pressure) * t
                )
                travelled += step
            }
            carry = (distance + carry) % step
            previous = current
        }
        if (result.last().point != samples.last().point) result += samples.last()
        return result
    }

    fun pressureWidth(
        baseWidth: Float,
        pressure: Float,
        enabled: Boolean,
        minFactor: Float = 0.35f,
        maxFactor: Float = 1.25f
    ): Float {
        if (!enabled) return max(0.1f, baseWidth)
        val p = pressure.coerceIn(0f, 1f)
        val factor = minFactor + (maxFactor - minFactor) * p
        return max(0.1f, baseWidth * factor)
    }

    fun taperFactor(progress: Float, taperStart: Float, taperEnd: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        val start = taperStart.coerceIn(0f, 1f)
        val end = taperEnd.coerceIn(0f, 1f)
        val startFactor = if (start == 0f) 1f else min(1f, p / start)
        val endFactor = if (end == 0f) 1f else min(1f, (1f - p) / end)
        return min(startFactor, endFactor).coerceIn(0f, 1f)
    }
}
