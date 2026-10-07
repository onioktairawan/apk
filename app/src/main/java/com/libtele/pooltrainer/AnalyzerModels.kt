package com.libtele.pooltrainer

data class ScreenPoint(val x: Float, val y: Float)

data class AnalysisResult(
    val cue: ScreenPoint,
    val target: ScreenPoint,
    val ghost: ScreenPoint,
    val pocket: ScreenPoint,
    val ballRadius: Float,
    val confidence: Float
)
