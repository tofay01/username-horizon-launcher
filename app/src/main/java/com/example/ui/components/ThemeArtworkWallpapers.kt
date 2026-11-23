package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun hasBuiltInArtworkWallpaper(wallpaperId: String): Boolean = wallpaperId in setOf(
    "flag_algeria",
    "flag_egypt",
    "flag_saudi_arabia",
    "flag_france",
    "flag_germany",
    "flag_spain",
    "flag_uk",
    "cinema_scifi",
    "cinema_retro",
)

@Composable
fun ProceduralThemeWallpaper(
    wallpaperId: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF060B18))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (wallpaperId) {
                "flag_algeria" -> drawAlgeriaFlag()
                "flag_egypt" -> drawEgyptFlag()
                "flag_saudi_arabia" -> drawSaudiFlag()
                "flag_france" -> drawFranceFlag()
                "flag_germany" -> drawGermanyFlag()
                "flag_spain" -> drawSpainFlag()
                "flag_uk" -> drawUkFlag()
                "cinema_scifi" -> drawCinemaSciFi()
                "cinema_retro" -> drawCinemaRetro()
            }
            if (wallpaperId.startsWith("flag_")) {
                drawSilkFlagOverlay()
            }
        }
    }
}

private fun DrawScope.drawSilkFlagOverlay() {
    val w = size.width
    val h = size.height
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.14f),
                Color.Black.copy(alpha = 0.24f),
                Color.White.copy(alpha = 0.08f),
                Color.Black.copy(alpha = 0.34f),
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h * 0.85f),
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.42f),
            ),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = size.maxDimension * 0.72f,
        )
    )
}

private fun DrawScope.drawStarPath(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    innerRadius: Float,
    color: Color,
    startAngleDeg: Float = -90f,
) {
    val path = Path()
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angleRad = (startAngleDeg + i * 36f) * PI / 180.0
        val x = cx + (radius * cos(angleRad)).toFloat()
        val y = cy + (radius * sin(angleRad)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color)
}

private fun DrawScope.drawAlgeriaFlag() {
    val w = size.width
    val h = size.height
    val green = Color(0xFF006633)
    val white = Color(0xFFF3F6FA)
    val red = Color(0xFFD21034)

    drawRect(color = green, topLeft = Offset.Zero, size = Size(w * 0.5f, h))
    drawRect(color = white, topLeft = Offset(w * 0.5f, 0f), size = Size(w * 0.5f, h))

    val cx = w * 0.5f
    val cy = h * 0.5f
    val outerR = minOf(w, h) * 0.24f
    drawCircle(color = red, radius = outerR, center = Offset(cx, cy))

    val innerOffset = outerR * 0.24f
    val innerR = outerR * 0.80f
    // Split cutout matching background halves cleanly
    drawCircle(color = green, radius = innerR, center = Offset(cx + innerOffset, cy))
    // Right half cutout over white half
    val rightMask = Path().apply {
        addRect(androidx.compose.ui.geometry.Rect(cx, 0f, w, h))
    }
    drawPath(
        path = Path.combine(
            androidx.compose.ui.graphics.PathOperation.Intersect,
            Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        cx + innerOffset - innerR,
                        cy - innerR,
                        cx + innerOffset + innerR,
                        cy + innerR,
                    )
                )
            },
            rightMask,
        ),
        color = white,
    )

    drawStarPath(
        cx = cx + outerR * 0.42f,
        cy = cy,
        outerRadius = outerR * 0.48f,
        innerRadius = outerR * 0.19f,
        color = red,
        startAngleDeg = -18f,
    )
}

private fun DrawScope.drawEgyptFlag() {
    val w = size.width
    val h = size.height
    val third = h / 3f
    drawRect(color = Color(0xFFCE1126), topLeft = Offset(0f, 0f), size = Size(w, third))
    drawRect(color = Color(0xFFF5F7FA), topLeft = Offset(0f, third), size = Size(w, third))
    drawRect(color = Color(0xFF111827), topLeft = Offset(0f, third * 2f), size = Size(w, h - third * 2f))

    // Golden Eagle crest in center
    val cx = w * 0.5f
    val cy = h * 0.5f
    val s = minOf(w, h) * 0.12f
    val gold = Color(0xFFC09300)
    val darkGold = Color(0xFF8C6900)
    // Wings
    val eagle = Path().apply {
        moveTo(cx - s * 1.1f, cy - s * 0.55f)
        lineTo(cx - s * 0.35f, cy - s * 0.35f)
        lineTo(cx, cy - s * 0.85f)
        lineTo(cx + s * 0.35f, cy - s * 0.35f)
        lineTo(cx + s * 1.1f, cy - s * 0.55f)
        lineTo(cx + s * 0.85f, cy + s * 0.55f)
        lineTo(cx + s * 0.32f, cy + s * 0.48f)
        lineTo(cx, cy + s * 0.95f)
        lineTo(cx - s * 0.32f, cy + s * 0.48f)
        lineTo(cx - s * 0.85f, cy + s * 0.55f)
        close()
    }
    drawPath(eagle, color = gold)
    drawRect(
        color = darkGold,
        topLeft = Offset(cx - s * 0.24f, cy - s * 0.30f),
        size = Size(s * 0.48f, s * 0.72f),
    )
}

private fun DrawScope.drawSaudiFlag() {
    val w = size.width
    val h = size.height
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF006C35), Color(0xFF005228), Color(0xFF006C35)),
            start = Offset.Zero,
            end = Offset(w, h),
        )
    )
    val cx = w * 0.5f
    val cy = h * 0.5f
    val white = Color(0xFFF8FAFC)

    // Stylized geometric calligraphic motif above sword
    val motifY = cy - h * 0.10f
    val strokeW = minOf(w, h) * 0.022f
    val motifW = w * 0.34f
    for (i in -2..2) {
        val x = cx + i * (motifW * 0.19f)
        val topH = if (i % 2 == 0) h * 0.13f else h * 0.09f
        drawLine(
            color = white,
            start = Offset(x, motifY - topH),
            end = Offset(x, motifY + h * 0.03f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
    }
    drawLine(
        color = white,
        start = Offset(cx - motifW * 0.48f, motifY + h * 0.03f),
        end = Offset(cx + motifW * 0.48f, motifY + h * 0.03f),
        strokeWidth = strokeW * 1.15f,
        cap = StrokeCap.Round,
    )

    // White Arab Sword underneath pointing left
    val swordY = cy + h * 0.12f
    val swordLeft = cx - w * 0.22f
    val swordRight = cx + w * 0.22f
    val blade = Path().apply {
        moveTo(swordLeft, swordY)
        lineTo(swordLeft + w * 0.045f, swordY - h * 0.022f)
        lineTo(swordRight - w * 0.045f, swordY - h * 0.015f)
        lineTo(swordRight - w * 0.045f, swordY + h * 0.015f)
        lineTo(swordLeft + w * 0.045f, swordY + h * 0.022f)
        close()
    }
    drawPath(blade, color = white)
    // Hilt guard & handle
    drawRect(
        color = white,
        topLeft = Offset(swordRight - w * 0.052f, swordY - h * 0.045f),
        size = Size(w * 0.012f, h * 0.09f),
    )
    drawRect(
        color = white,
        topLeft = Offset(swordRight - w * 0.040f, swordY - h * 0.012f),
        size = Size(w * 0.042f, h * 0.024f),
    )
}

private fun DrawScope.drawFranceFlag() {
    val w = size.width
    val h = size.height
    val third = w / 3f
    drawRect(color = Color(0xFF002654), topLeft = Offset.Zero, size = Size(third, h))
    drawRect(color = Color(0xFFF5F7FA), topLeft = Offset(third, 0f), size = Size(third, h))
    drawRect(color = Color(0xFFCE1126), topLeft = Offset(third * 2f, 0f), size = Size(w - third * 2f, h))
}

private fun DrawScope.drawGermanyFlag() {
    val w = size.width
    val h = size.height
    val third = h / 3f
    drawRect(color = Color(0xFF0B0E14), topLeft = Offset(0f, 0f), size = Size(w, third))
    drawRect(color = Color(0xFFDD0000), topLeft = Offset(0f, third), size = Size(w, third))
    drawRect(color = Color(0xFFFFCE00), topLeft = Offset(0f, third * 2f), size = Size(w, h - third * 2f))
}

private fun DrawScope.drawSpainFlag() {
    val w = size.width
    val h = size.height
    val band = h * 0.25f
    drawRect(color = Color(0xFFAA151B), topLeft = Offset(0f, 0f), size = Size(w, band))
    drawRect(color = Color(0xFFF1BF00), topLeft = Offset(0f, band), size = Size(w, h * 0.50f))
    drawRect(color = Color(0xFFAA151B), topLeft = Offset(0f, h - band), size = Size(w, band))

    // Coat of Arms silhouette positioned on left-center yellow band
    val cx = w * 0.34f
    val cy = h * 0.5f
    val s = minOf(w, h) * 0.14f
    val crestRed = Color(0xFF991B1B)
    val crestGold = Color(0xFFD97706)
    drawRect(color = crestGold, topLeft = Offset(cx - s * 0.85f, cy - s * 0.65f), size = Size(s * 0.14f, s * 1.3f))
    drawRect(color = crestGold, topLeft = Offset(cx + s * 0.71f, cy - s * 0.65f), size = Size(s * 0.14f, s * 1.3f))
    drawOval(color = crestRed, topLeft = Offset(cx - s * 0.45f, cy - s * 0.55f), size = Size(s * 0.90f, s * 1.15f))
    drawOval(color = crestGold, topLeft = Offset(cx - s * 0.32f, cy - s * 0.90f), size = Size(s * 0.64f, s * 0.32f))
}

private fun DrawScope.drawUkFlag() {
    val w = size.width
    val h = size.height
    val navy = Color(0xFF012169)
    val white = Color(0xFFF5F7FA)
    val red = Color(0xFFC8102E)

    drawRect(color = navy)

    val diagWhite = minOf(w, h) * 0.18f
    val diagRed = minOf(w, h) * 0.09f
    drawLine(color = white, start = Offset(0f, 0f), end = Offset(w, h), strokeWidth = diagWhite)
    drawLine(color = white, start = Offset(w, 0f), end = Offset(0f, h), strokeWidth = diagWhite)
    drawLine(color = red, start = Offset(0f, 0f), end = Offset(w, h), strokeWidth = diagRed)
    drawLine(color = red, start = Offset(w, 0f), end = Offset(0f, h), strokeWidth = diagRed)

    val crossWhite = minOf(w, h) * 0.28f
    val crossRed = minOf(w, h) * 0.17f
    drawRect(color = white, topLeft = Offset((w - crossWhite) / 2f, 0f), size = Size(crossWhite, h))
    drawRect(color = white, topLeft = Offset(0f, (h - crossWhite) / 2f), size = Size(w, crossWhite))
    drawRect(color = red, topLeft = Offset((w - crossRed) / 2f, 0f), size = Size(crossRed, h))
    drawRect(color = red, topLeft = Offset(0f, (h - crossRed) / 2f), size = Size(w, crossRed))
}

private fun DrawScope.drawCinemaSciFi() {
    val w = size.width
    val h = size.height
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF04091D), Color(0xFF0A163D), Color(0xFF160B36), Color(0xFF050814)),
        )
    )
    // Anamorphic sci-fi cyan glow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x8822D3EE), Color(0x336366F1), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.42f),
            radius = w * 0.45f,
        ),
        topLeft = Offset(0f, h * 0.15f),
        size = Size(w, h * 0.55f),
    )
    // Horizontal anamorphic lens flare streak
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color(0xCC22D3EE), Color.White.copy(alpha = 0.85f), Color(0xCC22D3EE), Color.Transparent),
        ),
        topLeft = Offset(0f, h * 0.42f - 4f),
        size = Size(w, 8f),
    )
    // Perspective synth horizon grid
    val horizonY = h * 0.58f
    val gridColor = Color(0xFF22D3EE).copy(alpha = 0.26f)
    for (i in 1..6) {
        val t = (i / 6f)
        val y = horizonY + (h - horizonY) * (t * t)
        drawLine(color = gridColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1.5f)
    }
    for (i in -6..6) {
        drawLine(
            color = Color(0xFFA855F7).copy(alpha = 0.24f),
            start = Offset(w * 0.5f + i * (w * 0.03f), horizonY),
            end = Offset(w * 0.5f + i * (w * 0.16f), h),
            strokeWidth = 1.5f,
        )
    }
}

private fun DrawScope.drawCinemaRetro() {
    val w = size.width
    val h = size.height
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF3E2212), Color(0xFF1E110B), Color(0xFF0A0605)),
            center = Offset(w * 0.28f, h * 0.45f),
            radius = size.maxDimension * 0.78f,
        )
    )
    // Warm projector light cone
    val beam = Path().apply {
        moveTo(w * 0.12f, h * 0.46f)
        lineTo(w, h * 0.08f)
        lineTo(w, h * 0.90f)
        close()
    }
    drawPath(
        path = beam,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0x66FBBF24), Color(0x28F59E0B), Color.Transparent),
        ),
    )
    // Stylized 35mm cinema reel silhouette
    val rx = w * 0.18f
    val ry = h * 0.46f
    val rr = minOf(w, h) * 0.20f
    drawCircle(
        color = Color(0xFFF59E0B).copy(alpha = 0.28f),
        radius = rr,
        center = Offset(rx, ry),
        style = Stroke(width = rr * 0.16f),
    )
    for (i in 0 until 5) {
        rotate(degrees = i * 72f, pivot = Offset(rx, ry)) {
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.24f),
                radius = rr * 0.22f,
                center = Offset(rx, ry - rr * 0.52f),
            )
        }
    }
    // Top & bottom subtle filmstrip perforations
    val perfColor = Color(0xFFF59E0B).copy(alpha = 0.18f)
    val count = 14
    val step = w / count
    for (i in 0 until count) {
        drawRect(
            color = perfColor,
            topLeft = Offset(i * step + step * 0.25f, h * 0.04f),
            size = Size(step * 0.45f, h * 0.035f),
        )
        drawRect(
            color = perfColor,
            topLeft = Offset(i * step + step * 0.25f, h * 0.925f),
            size = Size(step * 0.45f, h * 0.035f),
        )
    }
}
