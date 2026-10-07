package com.libtele.pooltrainer

import android.graphics.Bitmap
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object TrainingAnalyzer {
    private data class C(val x: Float, val y: Float, val bright: Float, val sat: Float)

    fun analyze(bitmap: Bitmap): AnalysisResult? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 320 || h < 320) return null

        val step = max(8, min(w, h) / 120)
        val points = mutableListOf<C>()

        for (y in (h * 0.15f).toInt() until (h * 0.88f).toInt() step step) {
            for (x in (w * 0.07f).toInt() until (w * 0.93f).toInt() step step) {
                val p = bitmap.getPixel(x, y)
                val r = (p shr 16) and 255
                val g = (p shr 8) and 255
                val b = p and 255
                val hi = max(r, max(g, b))
                val lo = min(r, min(g, b))
                val bright = (r + g + b) / 3f
                val sat = (hi - lo).toFloat()
                if (bright > 155f || sat > 70f) points += C(x.toFloat(), y.toFloat(), bright, sat)
            }
        }

        val cue = points
            .filter { it.bright > 185f && it.sat < 65f }
            .maxByOrNull { it.bright - it.sat * 0.5f }
            ?: return null

        val target = points
            .filter {
                val d = hypot(it.x - cue.x, it.y - cue.y)
                d > step * 3f && d < min(w, h) * 0.55f
            }
            .minByOrNull { hypot(it.x - cue.x, it.y - cue.y) - it.sat * 0.6f }
            ?: return null

        val pockets = pockets(w, h)
        val pocket = pockets.minByOrNull { hypot(it.x - target.x, it.y - target.y) } ?: return null
        val dx = pocket.x - target.x
        val dy = pocket.y - target.y
        val d = hypot(dx, dy).coerceAtLeast(1f)
        val radius = (step * 1.35f).coerceIn(10f, min(w, h) * 0.04f)

        val ghost = ScreenPoint(
            target.x - (dx / d) * radius * 2f,
            target.y - (dy / d) * radius * 2f
        )

        return AnalysisResult(
            cue = ScreenPoint(cue.x, cue.y),
            target = ScreenPoint(target.x, target.y),
            ghost = ghost,
            pocket = pocket,
            ballRadius = radius,
            confidence = ((cue.bright / 255f) * 0.6f + (target.sat / 255f) * 0.4f).coerceIn(0f, 1f)
        )
    }

    private fun pockets(w: Int, h: Int): List<ScreenPoint> {
        val l = w * 0.075f
        val r = w * 0.925f
        val t = h * 0.17f
        val b = h * 0.84f
        val m = (t + b) / 2f
        return listOf(
            ScreenPoint(l,t), ScreenPoint(r,t),
            ScreenPoint(l,m), ScreenPoint(r,m),
            ScreenPoint(l,b), ScreenPoint(r,b)
        )
    }
}
