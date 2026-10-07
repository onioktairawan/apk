package com.libtele.pooltrainer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class AimOverlayView(context: Context) : View(context) {
    private val a = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF00E6A8.toInt(); strokeWidth = 5f; style = Paint.Style.STROKE }
    private val b = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFC107.toInt(); strokeWidth = 4f; style = Paint.Style.STROKE }
    private val c = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF40C4FF.toInt(); strokeWidth = 4f; style = Paint.Style.STROKE }
    private var result: AnalysisResult? = null

    fun setResult(v: AnalysisResult?) { result = v; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val r = result ?: return
        canvas.drawLine(r.cue.x, r.cue.y, r.ghost.x, r.ghost.y, a)
        canvas.drawCircle(r.cue.x, r.cue.y, r.ballRadius, a)
        canvas.drawCircle(r.target.x, r.target.y, r.ballRadius, b)
        canvas.drawCircle(r.ghost.x, r.ghost.y, r.ballRadius, a)
        canvas.drawLine(r.target.x, r.target.y, r.pocket.x, r.pocket.y, c)
        canvas.drawCircle(r.pocket.x, r.pocket.y, r.ballRadius * 1.35f, c)
    }
}
