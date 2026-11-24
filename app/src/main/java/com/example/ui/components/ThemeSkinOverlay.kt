package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.example.model.ThemeSkinStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Architectural perimeter framing & signature atmospheric skin drawn cleanly BEHIND
 * the HomeScreen HUD so card silhouettes, sizes, padding, and interactive bounds remain 100% intact.
 */
@Composable
fun LauncherThemeFrameOverlay(
    skinStyle: ThemeSkinStyle,
    rimColor: Color,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    if (skinStyle == ThemeSkinStyle.DEFAULT) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val sideMargin = if (isCompact) 10.dp.toPx() else 20.dp.toPx()
        val topBand = if (isCompact) 8.dp.toPx() else 12.dp.toPx()
        val bottomBand = if (isCompact) 8.dp.toPx() else 12.dp.toPx()

        when (skinStyle) {
            ThemeSkinStyle.SILVER_CHROME -> drawSilverChromeSkin(w, h, rimColor)
            ThemeSkinStyle.COFFEE_GOLD -> drawCoffeeGoldSkin(w, h, rimColor)
            ThemeSkinStyle.ZELLIGE_ALGERIAN -> drawZelligeSkin(w, h, sideMargin, topBand, bottomBand, isCompact)
            ThemeSkinStyle.PHARAOH_GOLD -> drawPharaohGoldSkin(w, h, sideMargin, topBand, bottomBand, isCompact)
            ThemeSkinStyle.EGYPT_PHARAOHS -> drawEgyptPharaohsSkin(w, h, sideMargin, isCompact)
            ThemeSkinStyle.ALGERIA_FENNECS -> drawAlgeriaFennecsSkin(w, h, sideMargin, isCompact)
            ThemeSkinStyle.MAN_CITY_ETIHAD -> drawManCitySkin(w, h, sideMargin, isCompact)
            ThemeSkinStyle.BARCA_BLAUGRANA -> drawBarcaSkin(w, h, sideMargin, isCompact)
            ThemeSkinStyle.REAL_MADRID_ROYAL -> drawRealMadridSkin(w, h, sideMargin, isCompact)
            ThemeSkinStyle.DEFAULT -> Unit
        }
    }
}

/**
 * Compact preview overlay rendered inside ThemeAndWallpaperScreen cards so users can visually
 * recognize each signature interface frame at a glance.
 */
@Composable
fun ThemePreviewAccentFrame(
    skinStyle: ThemeSkinStyle,
    rimColor: Color,
    modifier: Modifier = Modifier,
) {
    if (skinStyle == ThemeSkinStyle.DEFAULT) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (skinStyle) {
            ThemeSkinStyle.SILVER_CHROME -> {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.85f), Color(0xFF94A3B8), Color.White.copy(alpha = 0.70f))
                    ),
                    cornerRadius = CornerRadius(14f, 14f),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
            ThemeSkinStyle.COFFEE_GOLD -> {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFF92400E))
                    ),
                    cornerRadius = CornerRadius(14f, 14f),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
            ThemeSkinStyle.ZELLIGE_ALGERIAN -> {
                val stripH = 10.dp.toPx()
                drawZelligeMosaicStrip(0f, 0f, w, stripH, tileSize = 10.dp.toPx())
                drawZelligeMosaicStrip(0f, h - stripH, w, stripH, tileSize = 10.dp.toPx())
                drawLine(Color(0xFFFACC15), Offset(0f, stripH), Offset(w, stripH), strokeWidth = 1.4.dp.toPx())
                drawLine(Color(0xFFFACC15), Offset(0f, h - stripH), Offset(w, h - stripH), strokeWidth = 1.4.dp.toPx())
                drawLantern(Offset(10.dp.toPx(), h * 0.45f), 8.dp.toPx())
                drawLantern(Offset(w - 10.dp.toPx(), h * 0.45f), 8.dp.toPx())
            }
            ThemeSkinStyle.PHARAOH_GOLD -> {
                val colW = 10.dp.toPx()
                drawGoldenColumn(0f, colW, h, Color(0xFFFACC15), Color(0xFF78350F))
                drawGoldenColumn(w - colW, colW, h, Color(0xFFFACC15), Color(0xFF78350F))
                drawWingedSolarDisc(Offset(w * 0.5f, 13.dp.toPx()), spanW = w * 0.42f, discR = 6.dp.toPx())
            }
            ThemeSkinStyle.EGYPT_PHARAOHS -> {
                drawGoldenColumn(0f, 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF7F1D1D))
                drawGoldenColumn(w - 8.dp.toPx(), 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF7F1D1D))
                drawTopCrestMedallion(
                    center = Offset(w * 0.5f, 15.dp.toPx()),
                    r = 10.dp.toPx(),
                    ringColor = Color(0xFFFACC15),
                    topColor = Color(0xFFCE1126),
                    midColor = Color(0xFFF8FAFC),
                    botColor = Color(0xFF111827),
                )
            }
            ThemeSkinStyle.ALGERIA_FENNECS -> {
                drawGoldenColumn(0f, 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF065F46))
                drawGoldenColumn(w - 8.dp.toPx(), 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF065F46))
                drawTopCrestMedallion(
                    center = Offset(w * 0.5f, 15.dp.toPx()),
                    r = 10.dp.toPx(),
                    ringColor = Color(0xFFFACC15),
                    topColor = Color(0xFF059669),
                    midColor = Color(0xFFF8FAFC),
                    botColor = Color(0xFFD21034),
                )
            }
            ThemeSkinStyle.MAN_CITY_ETIHAD -> {
                drawGoldenColumn(0f, 8.dp.toPx(), h, Color(0xFFBAE6FD), Color(0xFF0369A1))
                drawGoldenColumn(w - 8.dp.toPx(), 8.dp.toPx(), h, Color(0xFFBAE6FD), Color(0xFF0369A1))
                drawTopCrestMedallion(
                    center = Offset(w * 0.5f, 15.dp.toPx()),
                    r = 10.dp.toPx(),
                    ringColor = Color(0xFFE0F2FE),
                    topColor = Color(0xFF6CABDD),
                    midColor = Color(0xFF1E3A8A),
                    botColor = Color(0xFFFACC15),
                )
            }
            ThemeSkinStyle.BARCA_BLAUGRANA -> {
                drawGoldenColumn(0f, 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF881337))
                drawGoldenColumn(w - 8.dp.toPx(), 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF1E3A8A))
                drawTopCrestMedallion(
                    center = Offset(w * 0.5f, 15.dp.toPx()),
                    r = 10.dp.toPx(),
                    ringColor = Color(0xFFFACC15),
                    topColor = Color(0xFFA50044),
                    midColor = Color(0xFF004D98),
                    botColor = Color(0xFFFACC15),
                )
            }
            ThemeSkinStyle.REAL_MADRID_ROYAL -> {
                drawGoldenColumn(0f, 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF1E3A8A))
                drawGoldenColumn(w - 8.dp.toPx(), 8.dp.toPx(), h, Color(0xFFFACC15), Color(0xFF1E3A8A))
                drawTopCrestMedallion(
                    center = Offset(w * 0.5f, 15.dp.toPx()),
                    r = 10.dp.toPx(),
                    ringColor = Color(0xFFFACC15),
                    topColor = Color(0xFFFEF08A),
                    midColor = Color(0xFFF8FAFC),
                    botColor = Color(0xFF1D4ED8),
                )
            }
            ThemeSkinStyle.DEFAULT -> Unit
        }

        // Subtle signature rim highlight
        if (skinStyle != ThemeSkinStyle.SILVER_CHROME && skinStyle != ThemeSkinStyle.COFFEE_GOLD) {
            drawRect(
                color = rimColor.copy(alpha = 0.35f),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

private fun DrawScope.drawSilverChromeSkin(w: Float, h: Float, rimColor: Color) {
    // Sleek platinum-silver moonlight aura behind Earth center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.16f),
                Color(0xFF94A3B8).copy(alpha = 0.08f),
                Color.Transparent,
            ),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = w * 0.48f,
        ),
        radius = w * 0.48f,
        center = Offset(w * 0.5f, h * 0.45f),
    )

    // Brushed metallic outer perimeter hairline
    val inset = 4.dp.toPx()
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.58f),
                Color(0xFF94A3B8).copy(alpha = 0.32f),
                rimColor.copy(alpha = 0.52f),
                Color(0xFF64748B).copy(alpha = 0.25f),
            ),
            start = Offset.Zero,
            end = Offset(w, h),
        ),
        topLeft = Offset(inset, inset),
        size = Size(w - inset * 2f, h - inset * 2f),
        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
        style = Stroke(width = 1.5.dp.toPx()),
    )

    // Subtle top & bottom chrome specular sheen bar
    drawLine(
        brush = Brush.horizontalGradient(
            listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent)
        ),
        start = Offset(w * 0.20f, inset + 1.dp.toPx()),
        end = Offset(w * 0.80f, inset + 1.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
    )
}

private fun DrawScope.drawCoffeeGoldSkin(w: Float, h: Float, rimColor: Color) {
    // Warm roasted espresso outer glow + champagne gold center aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFF59E0B).copy(alpha = 0.14f),
                Color(0xFF78350F).copy(alpha = 0.10f),
                Color.Transparent,
            ),
            center = Offset(w * 0.5f, h * 0.46f),
            radius = w * 0.52f,
        ),
        radius = w * 0.52f,
        center = Offset(w * 0.5f, h * 0.46f),
    )

    val inset = 4.dp.toPx()
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFEF08A).copy(alpha = 0.55f),
                rimColor.copy(alpha = 0.45f),
                Color(0xFF92400E).copy(alpha = 0.35f),
                Color(0xFFFDE047).copy(alpha = 0.55f),
            ),
            start = Offset.Zero,
            end = Offset(w, h),
        ),
        topLeft = Offset(inset, inset),
        size = Size(w - inset * 2f, h - inset * 2f),
        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
        style = Stroke(width = 1.6.dp.toPx()),
    )

    drawLine(
        brush = Brush.horizontalGradient(
            listOf(Color.Transparent, Color(0xFFFEF08A).copy(alpha = 0.62f), Color.Transparent)
        ),
        start = Offset(w * 0.22f, inset + 1.dp.toPx()),
        end = Offset(w * 0.78f, inset + 1.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
    )
}

private fun DrawScope.drawZelligeSkin(
    w: Float,
    h: Float,
    sideMargin: Float,
    topBand: Float,
    bottomBand: Float,
    isCompact: Boolean,
) {
    // 1. Algerian geometric Zellige mosaic border along top arch & bottom strip (strictly edge margins)
    drawZelligeMosaicStrip(0f, 0f, w, topBand, tileSize = if (isCompact) 11.dp.toPx() else 14.dp.toPx())
    drawZelligeMosaicStrip(0f, h - bottomBand, w, bottomBand, tileSize = if (isCompact) 11.dp.toPx() else 14.dp.toPx())

    // Left & Right narrow mosaic borders
    drawZelligeVerticalStrip(0f, topBand, sideMargin * 0.62f, h - topBand - bottomBand, tileSize = 14.dp.toPx())
    drawZelligeVerticalStrip(w - sideMargin * 0.62f, topBand, sideMargin * 0.62f, h - topBand - bottomBand, tileSize = 14.dp.toPx())

    // 2. Pointed Moorish golden arch sweeping cleanly across the top-center open sky
    val archPath = Path().apply {
        moveTo(sideMargin * 0.62f, topBand)
        quadraticTo(w * 0.28f, topBand * 1.4f, w * 0.50f, if (isCompact) 26.dp.toPx() else 34.dp.toPx())
        quadraticTo(w * 0.72f, topBand * 1.4f, w - sideMargin * 0.62f, topBand)
    }
    drawPath(
        path = archPath,
        color = Color(0xFFFACC15).copy(alpha = 0.28f),
        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
    )
    drawPath(
        path = archPath,
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF10B981), Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFFEF08A), Color(0xFF10B981))
        ),
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
    )

    // 3. Central 8-point Maghrebi Zellige Star Rosette cleanly tucked at top center
    val rosetteY = if (isCompact) 14.dp.toPx() else 18.dp.toPx()
    val rosetteR = if (isCompact) 11.dp.toPx() else 15.dp.toPx()
    drawZelligeStarRosette(Offset(w * 0.5f, rosetteY), rosetteR)

    // 4. Twin glowing golden brass lanterns anchored on the far outer left/right edges
    val lanternRadius = if (isCompact) 7.dp.toPx() else 10.dp.toPx()
    val lanternXLeft = sideMargin * 0.55f
    val lanternXRight = w - sideMargin * 0.55f
    val lanternY = h * 0.44f
    drawLantern(Offset(lanternXLeft, lanternY), lanternRadius)
    drawLantern(Offset(lanternXRight, lanternY), lanternRadius)

    // Gold border lines separating mosaic edges from main glass workspace
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFEF08A), Color(0xFFF59E0B))),
        start = Offset(0f, topBand),
        end = Offset(w, topBand),
        strokeWidth = 1.5.dp.toPx(),
    )
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFEF08A), Color(0xFFF59E0B))),
        start = Offset(0f, h - bottomBand),
        end = Offset(w, h - bottomBand),
        strokeWidth = 1.5.dp.toPx(),
    )
}

private fun DrawScope.drawPharaohGoldSkin(
    w: Float,
    h: Float,
    sideMargin: Float,
    topBand: Float,
    bottomBand: Float,
    isCompact: Boolean,
) {
    val colW = sideMargin * 0.72f
    // Carved golden sandstone side pillars
    drawGoldenColumn(0f, colW, h, Color(0xFFFACC15), Color(0xFF78350F))
    drawGoldenColumn(w - colW, colW, h, Color(0xFFFACC15), Color(0xFF78350F))

    // Carved gold top & bottom temple friezes
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFEF08A), Color(0xFFD97706), Color(0xFF78350F))
        ),
        topLeft = Offset(colW, 0f),
        size = Size(w - colW * 2f, topBand * 0.55f),
        alpha = 0.75f,
    )
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFEF08A), Color(0xFFD97706), Color(0xFF78350F))
        ),
        topLeft = Offset(colW, h - bottomBand * 0.55f),
        size = Size(w - colW * 2f, bottomBand * 0.55f),
        alpha = 0.75f,
    )

    // Hieroglyph-inspired rhythmic vertical carved notches inside bottom frieze
    val step = 18.dp.toPx()
    var x = colW + step
    while (x < w - colW - step) {
        drawLine(
            color = Color(0xFF451A03).copy(alpha = 0.55f),
            start = Offset(x, h - bottomBand * 0.50f),
            end = Offset(x, h - 1.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
        )
        x += step
    }

    // Top-center Winged Solar Disc emblem in open top-center zone
    val sunCenterY = if (isCompact) 14.dp.toPx() else 20.dp.toPx()
    drawWingedSolarDisc(
        center = Offset(w * 0.5f, sunCenterY),
        spanW = if (isCompact) 140.dp.toPx() else 220.dp.toPx(),
        discR = if (isCompact) 7.dp.toPx() else 10.dp.toPx(),
    )

    // Subtle bottom-center scarab disc accent on bezel
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFEF08A), Color(0xFFD97706), Color(0xFF451A03))),
        radius = if (isCompact) 5.dp.toPx() else 7.dp.toPx(),
        center = Offset(w * 0.5f, h - bottomBand * 0.35f),
    )
}

private fun DrawScope.drawEgyptPharaohsSkin(
    w: Float,
    h: Float,
    sideMargin: Float,
    isCompact: Boolean,
) {
    val colW = sideMargin * 0.65f
    drawGoldenColumn(0f, colW, h, Color(0xFFFACC15), Color(0xFF7F1D1D))
    drawGoldenColumn(w - colW, colW, h, Color(0xFFFACC15), Color(0xFF7F1D1D))

    drawSweepingTeamRibbonArch(
        w = w,
        colW = colW,
        leftColor = Color(0xFFCE1126),
        centerColor = Color(0xFFF8FAFC),
        rightColor = Color(0xFF111827),
        goldAccent = Color(0xFFFACC15),
        isCompact = isCompact,
    )

    val crestR = if (isCompact) 13.dp.toPx() else 19.dp.toPx()
    val crestY = if (isCompact) 18.dp.toPx() else 24.dp.toPx()
    drawTopCrestMedallion(
        center = Offset(w * 0.5f, crestY),
        r = crestR,
        ringColor = Color(0xFFFACC15),
        topColor = Color(0xFFCE1126),
        midColor = Color(0xFFF8FAFC),
        botColor = Color(0xFF111827),
    )
}

private fun DrawScope.drawAlgeriaFennecsSkin(
    w: Float,
    h: Float,
    sideMargin: Float,
    isCompact: Boolean,
) {
    val colW = sideMargin * 0.65f
    drawGoldenColumn(0f, colW, h, Color(0xFFFACC15), Color(0xFF065F46))
    drawGoldenColumn(w - colW, colW, h, Color(0xFFFACC15), Color(0xFF065F46))

    drawSweepingTeamRibbonArch(
        w = w,
        colW = colW,
        leftColor = Color(0xFF006633),
        centerColor = Color(0xFFF8FAFC),
        rightColor = Color(0xFFD21034),
        goldAccent = Color(0xFFFACC15),
        isCompact = isCompact,
    )

    val crestR = if (isCompact) 13.dp.toPx() else 19.dp.toPx()
    val crestY = if (isCompact) 18.dp.toPx() else 24.dp.toPx()
    drawTopCrestMedallion(
        center = Offset(w * 0.5f, crestY),
        r = crestR,
        ringColor = Color(0xFFFACC15),
        topColor = Color(0xFF059669),
        midColor = Color(0xFFF8FAFC),
        botColor = Color(0xFFD21034),
    )
    // Twin golden championship stars flanking the top crest
    drawStar(Offset(w * 0.5f - crestR * 0.48f, crestY - crestR * 1.15f), 3.6.dp.toPx(), Color(0xFFFACC15))
    drawStar(Offset(w * 0.5f + crestR * 0.48f, crestY - crestR * 1.15f), 3.6.dp.toPx(), Color(0xFFFACC15))
}

private fun DrawScope.drawManCitySkin(
    w: Float,
    h: Float,
    sideMargin: Float,
    isCompact: Boolean,
) {
    val colW = sideMargin * 0.65f
    drawGoldenColumn(0f, colW, h, Color(0xFFE0F2FE), Color(0xFF0369A1))
    drawGoldenColumn(w - colW, colW, h, Color(0xFFE0F2FE), Color(0xFF0369A1))

    drawSweepingTeamRibbonArch(
        w = w,
        colW = colW,
        leftColor = Color(0xFF6CABDD),
        centerColor = Color(0xFFE0F2FE),
        rightColor = Color(0xFF0284C7),
        goldAccent = Color(0xFFF8FAFC),
        isCompact = isCompact,
    )

    val crestR = if (isCompact) 13.dp.toPx() else 19.dp.toPx()
    val crestY = if (isCompact) 18.dp.toPx() else 24.dp.toPx()
    drawTopCrestMedallion(
        center = Offset(w * 0.5f, crestY),
        r = crestR,
        ringColor = Color(0xFFE0F2FE),
        topColor = Color(0xFF6CABDD),
        midColor = Color(0xFFF8FAFC),
        botColor = Color(0xFF1E3A8A),
    )
}

private fun DrawScope.drawBarcaSkin(
    w: Float,
    h: Float,
    sideMargin: Float,
    isCompact: Boolean,
) {
    val colW = sideMargin * 0.65f
    drawGoldenColumn(0f, colW, h, Color(0xFFFACC15), Color(0xFF881337))
    drawGoldenColumn(w - colW, colW, h, Color(0xFFFACC15), Color(0xFF1E3A8A))

    drawSweepingTeamRibbonArch(
        w = w,
        colW = colW,
        leftColor = Color(0xFFA50044),
        centerColor = Color(0xFFFACC15),
        rightColor = Color(0xFF004D98),
        goldAccent = Color(0xFFFACC15),
        isCompact = isCompact,
    )

    val crestR = if (isCompact) 13.dp.toPx() else 19.dp.toPx()
    val crestY = if (isCompact) 18.dp.toPx() else 24.dp.toPx()
    drawTopCrestMedallion(
        center = Offset(w * 0.5f, crestY),
        r = crestR,
        ringColor = Color(0xFFFACC15),
        topColor = Color(0xFFA50044),
        midColor = Color(0xFF004D98),
        botColor = Color(0xFFFACC15),
    )
}

private fun DrawScope.drawRealMadridSkin(
    w: Float,
    h: Float,
    sideMargin: Float,
    isCompact: Boolean,
) {
    val colW = sideMargin * 0.65f
    drawGoldenColumn(0f, colW, h, Color(0xFFFACC15), Color(0xFF1E3A8A))
    drawGoldenColumn(w - colW, colW, h, Color(0xFFFACC15), Color(0xFF1E3A8A))

    drawSweepingTeamRibbonArch(
        w = w,
        colW = colW,
        leftColor = Color(0xFF1E3A8A),
        centerColor = Color(0xFFF8FAFC),
        rightColor = Color(0xFF1D4ED8),
        goldAccent = Color(0xFFFACC15),
        isCompact = isCompact,
    )

    val crestR = if (isCompact) 13.dp.toPx() else 19.dp.toPx()
    val crestY = if (isCompact) 18.dp.toPx() else 24.dp.toPx()
    drawTopCrestMedallion(
        center = Offset(w * 0.5f, crestY),
        r = crestR,
        ringColor = Color(0xFFFACC15),
        topColor = Color(0xFFFDE047),
        midColor = Color(0xFFF8FAFC),
        botColor = Color(0xFF1E3A8A),
    )
    // Royal crown peak above the Real Madrid medallion
    drawCrownPeak(Offset(w * 0.5f, crestY - crestR), crestR * 0.65f, Color(0xFFFACC15))
}

private fun DrawScope.drawZelligeMosaicStrip(
    startX: Float,
    startY: Float,
    width: Float,
    height: Float,
    tileSize: Float,
) {
    val palette = listOf(
        Color(0xFF065F46), // Emerald
        Color(0xFF0EA5E9), // Turquoise
        Color(0xFF1E3A8A), // Deep Cobalt
        Color(0xFFF59E0B), // Amber Gold
        Color(0xFFFEF3C7), // Ivory Cream
    )
    drawRect(color = Color(0xFF041D1B).copy(alpha = 0.90f), topLeft = Offset(startX, startY), size = Size(width, height))

    val halfT = tileSize * 0.5f
    var x = startX
    var idx = 0
    while (x < startX + width + tileSize) {
        val cx = x + halfT
        val cy = startY + height * 0.5f
        val diamond = Path().apply {
            moveTo(cx, cy - height * 0.46f)
            lineTo(cx + halfT * 0.85f, cy)
            lineTo(cx, cy + height * 0.46f)
            lineTo(cx - halfT * 0.85f, cy)
            close()
        }
        drawPath(path = diamond, color = palette[idx % palette.size].copy(alpha = 0.88f))
        drawPath(path = diamond, color = Color(0xFFFACC15).copy(alpha = 0.72f), style = Stroke(width = 0.9.dp.toPx()))
        drawCircle(color = Color(0xFFFEF3C7).copy(alpha = 0.85f), radius = tileSize * 0.11f, center = Offset(cx, cy))
        x += tileSize * 0.88f
        idx++
    }
}

private fun DrawScope.drawZelligeVerticalStrip(
    startX: Float,
    startY: Float,
    width: Float,
    height: Float,
    tileSize: Float,
) {
    val palette = listOf(Color(0xFF065F46), Color(0xFF0EA5E9), Color(0xFFF59E0B), Color(0xFF1E3A8A))
    drawRect(color = Color(0xFF041D1B).copy(alpha = 0.82f), topLeft = Offset(startX, startY), size = Size(width, height))
    var y = startY
    var idx = 0
    val halfW = width * 0.5f
    val halfT = tileSize * 0.44f
    while (y < startY + height) {
        val cx = startX + halfW
        val cy = y + halfT
        val diamond = Path().apply {
            moveTo(cx, cy - halfT)
            lineTo(cx + halfW * 0.82f, cy)
            lineTo(cx, cy + halfT)
            lineTo(cx - halfW * 0.82f, cy)
            close()
        }
        drawPath(path = diamond, color = palette[idx % palette.size].copy(alpha = 0.82f))
        drawPath(path = diamond, color = Color(0xFFFACC15).copy(alpha = 0.65f), style = Stroke(width = 0.8.dp.toPx()))
        y += tileSize * 0.88f
        idx++
    }
}

private fun DrawScope.drawZelligeStarRosette(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFACC15).copy(alpha = 0.42f), Color.Transparent), center, radius * 1.8f),
        radius = radius * 1.8f,
        center = center,
    )
    val pts = 8
    val path = Path()
    for (i in 0 until pts * 2) {
        val r = if (i % 2 == 0) radius else radius * 0.48f
        val angle = (i * PI / pts).toFloat()
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = Color(0xFF065F46))
    drawPath(path = path, color = Color(0xFFFACC15), style = Stroke(width = 1.4.dp.toPx()))
    drawCircle(color = Color(0xFF0EA5E9), radius = radius * 0.32f, center = center)
    drawCircle(color = Color(0xFFFEF3C7), radius = radius * 0.14f, center = center)
}

private fun DrawScope.drawLantern(center: Offset, r: Float) {
    // Warm amber halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFEF08A).copy(alpha = 0.65f),
                Color(0xFFF59E0B).copy(alpha = 0.25f),
                Color.Transparent,
            ),
            center = center,
            radius = r * 2.8f,
        ),
        radius = r * 2.8f,
        center = center,
    )
    val body = Path().apply {
        moveTo(center.x, center.y - r * 1.1f)
        lineTo(center.x + r * 0.65f, center.y - r * 0.35f)
        lineTo(center.x + r * 0.45f, center.y + r * 0.70f)
        lineTo(center.x, center.y + r * 1.05f)
        lineTo(center.x - r * 0.45f, center.y + r * 0.70f)
        lineTo(center.x - r * 0.65f, center.y - r * 0.35f)
        close()
    }
    drawPath(
        path = body,
        brush = Brush.verticalGradient(listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFB45309))),
    )
    drawPath(
        path = body,
        color = Color(0xFFFEF9C3),
        style = Stroke(width = 1.dp.toPx(), join = StrokeJoin.Round),
    )
}

private fun DrawScope.drawGoldenColumn(
    left: Float,
    colW: Float,
    height: Float,
    highlight: Color,
    darkShade: Color,
) {
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(darkShade.copy(alpha = 0.85f), highlight.copy(alpha = 0.78f), darkShade.copy(alpha = 0.90f)),
            startX = left,
            endX = left + colW,
        ),
        topLeft = Offset(left, 0f),
        size = Size(colW, height),
    )
    // Carved vertical flutes
    drawLine(
        color = highlight.copy(alpha = 0.75f),
        start = Offset(left + colW * 0.35f, 0f),
        end = Offset(left + colW * 0.35f, height),
        strokeWidth = 0.9.dp.toPx(),
    )
    drawLine(
        color = highlight.copy(alpha = 0.75f),
        start = Offset(left + colW * 0.65f, 0f),
        end = Offset(left + colW * 0.65f, height),
        strokeWidth = 0.9.dp.toPx(),
    )
    // Top & bottom lotus collar rings
    val collarH = 6.dp.toPx()
    drawRect(color = highlight, topLeft = Offset(left, 18.dp.toPx()), size = Size(colW, collarH), alpha = 0.85f)
    drawRect(color = highlight, topLeft = Offset(left, height - 24.dp.toPx()), size = Size(colW, collarH), alpha = 0.85f)
}

private fun DrawScope.drawWingedSolarDisc(center: Offset, spanW: Float, discR: Float) {
    val halfSpan = spanW * 0.5f
    val leftWing = Path().apply {
        moveTo(center.x - discR * 0.9f, center.y - discR * 0.2f)
        lineTo(center.x - halfSpan, center.y - discR * 0.75f)
        lineTo(center.x - halfSpan * 0.86f, center.y + discR * 0.35f)
        lineTo(center.x - discR * 0.9f, center.y + discR * 0.45f)
        close()
    }
    val rightWing = Path().apply {
        moveTo(center.x + discR * 0.9f, center.y - discR * 0.2f)
        lineTo(center.x + halfSpan, center.y - discR * 0.75f)
        lineTo(center.x + halfSpan * 0.86f, center.y + discR * 0.35f)
        lineTo(center.x + discR * 0.9f, center.y + discR * 0.45f)
        close()
    }
    val wingBrush = Brush.horizontalGradient(
        listOf(Color(0xFFB45309), Color(0xFFFACC15), Color(0xFFFEF08A), Color(0xFFFACC15), Color(0xFFB45309)),
        startX = center.x - halfSpan,
        endX = center.x + halfSpan,
    )
    drawPath(leftWing, brush = wingBrush, alpha = 0.92f)
    drawPath(rightWing, brush = wingBrush, alpha = 0.92f)
    drawPath(leftWing, color = Color(0xFF451A03), style = Stroke(width = 0.9.dp.toPx()))
    drawPath(rightWing, color = Color(0xFF451A03), style = Stroke(width = 0.9.dp.toPx()))

    // Sun disc
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFEF9C3), Color(0xFFFACC15), Color(0xFFB45309))),
        radius = discR,
        center = center,
    )
    drawCircle(
        color = Color(0xFFFEF08A),
        radius = discR,
        center = center,
        style = Stroke(width = 1.2.dp.toPx()),
    )
}

private fun DrawScope.drawSweepingTeamRibbonArch(
    w: Float,
    colW: Float,
    leftColor: Color,
    centerColor: Color,
    rightColor: Color,
    goldAccent: Color,
    isCompact: Boolean,
) {
    val peakY = if (isCompact) 22.dp.toPx() else 30.dp.toPx()
    val archPath = Path().apply {
        moveTo(colW, 2.dp.toPx())
        quadraticTo(w * 0.28f, 6.dp.toPx(), w * 0.50f, peakY)
        quadraticTo(w * 0.72f, 6.dp.toPx(), w - colW, 2.dp.toPx())
    }
    drawPath(
        path = archPath,
        brush = Brush.horizontalGradient(listOf(leftColor, centerColor, rightColor)),
        style = Stroke(width = if (isCompact) 6.dp.toPx() else 9.dp.toPx(), cap = StrokeCap.Round),
        alpha = 0.78f,
    )
    drawPath(
        path = archPath,
        color = goldAccent.copy(alpha = 0.85f),
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawTopCrestMedallion(
    center: Offset,
    r: Float,
    ringColor: Color,
    topColor: Color,
    midColor: Color,
    botColor: Color,
) {
    drawCircle(
        brush = Brush.radialGradient(listOf(ringColor.copy(alpha = 0.36f), Color.Transparent), center, r * 1.9f),
        radius = r * 1.9f,
        center = center,
    )
    val circleClip = Path().apply {
        addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r))
    }
    clipPath(circleClip) {
        drawRect(topColor, Offset(center.x - r, center.y - r), Size(r * 2f, r * 0.68f))
        drawRect(midColor, Offset(center.x - r, center.y - r * 0.32f), Size(r * 2f, r * 0.64f))
        drawRect(botColor, Offset(center.x - r, center.y + r * 0.32f), Size(r * 2f, r * 0.68f))
    }
    drawCircle(color = ringColor, radius = r, center = center, style = Stroke(width = 2.dp.toPx()))
    drawCircle(color = Color.White.copy(alpha = 0.65f), radius = r * 0.86f, center = center, style = Stroke(width = 0.8.dp.toPx()))
}

private fun DrawScope.drawStar(center: Offset, outerR: Float, color: Color) {
    val innerR = outerR * 0.42f
    val p = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) outerR else innerR
        val angle = (-90f + i * 36f) * PI / 180.0
        val x = center.x + (rad * cos(angle)).toFloat()
        val y = center.y + (rad * sin(angle)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, color = color)
}

private fun DrawScope.drawCrownPeak(baseCenter: Offset, width: Float, color: Color) {
    val half = width * 0.5f
    val h = width * 0.45f
    val p = Path().apply {
        moveTo(baseCenter.x - half, baseCenter.y)
        lineTo(baseCenter.x - half * 0.65f, baseCenter.y - h * 0.75f)
        lineTo(baseCenter.x - half * 0.25f, baseCenter.y - h * 0.35f)
        lineTo(baseCenter.x, baseCenter.y - h)
        lineTo(baseCenter.x + half * 0.25f, baseCenter.y - h * 0.35f)
        lineTo(baseCenter.x + half * 0.65f, baseCenter.y - h * 0.75f)
        lineTo(baseCenter.x + half, baseCenter.y)
        close()
    }
    drawPath(p, color = color)
}
