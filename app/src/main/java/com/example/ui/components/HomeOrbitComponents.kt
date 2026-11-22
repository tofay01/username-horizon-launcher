package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherLanguage
import com.example.ui.theme.PureWhite
import kotlin.math.min

/**
 * Frosted glowing capsule pill for the top-left greeting badge (`BONJOUR`, `GOOD MORNING`, etc.).
 */
@Composable
fun OrbitGreetingPill(
    text: String,
    surfaceHex: Long,
    glassAlpha: Float,
    accent: Color,
    enhancedContrast: Boolean,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    val barHeight = if (isCompact) 36.dp else 42.dp
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(surfaceHex).copy(alpha = glassAlpha.coerceIn(0.24f, 0.88f)),
        border = BorderStroke(1.1.dp, accent.copy(alpha = 0.62f)),
        shadowElevation = if (glassAlpha < 0.08f) 0.dp else 4.dp,
        modifier = modifier.height(barHeight),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = if (isCompact) 14.dp else 18.dp),
        ) {
            ContrastText(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 1.4.sp,
                    fontSize = if (isCompact) 11.sp else 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                enhancedContrast = enhancedContrast,
                color = PureWhite,
            )
        }
    }
}

/**
 * Matching circular frosted glass language switcher button (`FR` / `EN` / `ع`) right beside the greeting pill.
 */
@Composable
fun OrbitLanguageCycleButton(
    language: LauncherLanguage,
    surfaceHex: Long,
    glassAlpha: Float,
    accent: Color,
    contentDescription: String,
    isCompact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circleSize = if (isCompact) 36.dp else 42.dp
    TvFocusableSurface(
        onClick = onClick,
        accentColor = accent,
        shape = CircleShape,
        baseColor = Color(surfaceHex).copy(alpha = (glassAlpha + 0.08f).coerceIn(0.28f, 0.90f)),
        focusedColor = accent.copy(alpha = 0.48f),
        baseBorderColor = accent.copy(alpha = 0.65f),
        modifier = modifier
            .size(circleSize)
            .semantics { this.contentDescription = contentDescription }
            .testTag("home_language_cycle_button"),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = language.circleCode,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 11.sp else 12.5.sp,
                ),
                color = PureWhite,
                maxLines = 1,
            )
        }
    }
}

/**
 * Concentric glowing orbital rings arching behind the center Clock, curving gracefully
 * between the Prayer (`ADANE`) and Weather (`Meteo`) cards and cradling the All Apps circle above the dock.
 */
@Composable
fun ConcentricOrbitBackdrop(
    accent: Color,
    surfaceColor: Color,
    glassAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val cx = w * 0.5f
        val cy = h * 0.62f
        val baseR = min(w * 0.30f, h * 0.66f).coerceAtLeast(80f)

        val outerR = baseR * 1.12f
        val middleR = baseR * 0.80f
        val innerR = baseR * 0.56f

        val fillScale = (0.45f + glassAlpha * 0.55f).coerceIn(0.30f, 1f)

        // Outer ambient atmospheric glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = 0.22f * fillScale),
                    surfaceColor.copy(alpha = 0.14f * fillScale),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = outerR * 1.15f,
            ),
            radius = outerR * 1.15f,
            center = Offset(cx, cy),
        )

        // Outer orbit ring stroke
        drawCircle(
            color = accent.copy(alpha = 0.24f),
            radius = outerR,
            center = Offset(cx, cy),
            style = Stroke(width = 4.dp.toPx()),
        )
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    PureWhite.copy(alpha = 0.55f),
                    accent.copy(alpha = 0.42f),
                    accent.copy(alpha = 0.10f),
                ),
                startY = cy - outerR,
                endY = cy + outerR * 0.4f,
            ),
            radius = outerR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.4.dp.toPx()),
        )

        // Middle translucent orb disc + bright rim
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0A245E).copy(alpha = 0.34f * fillScale),
                    Color(0xFF0D327A).copy(alpha = 0.20f * fillScale),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = middleR,
            ),
            radius = middleR,
            center = Offset(cx, cy),
        )
        drawCircle(
            color = accent.copy(alpha = 0.28f),
            radius = middleR,
            center = Offset(cx, cy),
            style = Stroke(width = 5.dp.toPx()),
        )
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    PureWhite.copy(alpha = 0.85f),
                    accent.copy(alpha = 0.72f),
                    accent.copy(alpha = 0.18f),
                ),
                startY = cy - middleR,
                endY = cy + middleR * 0.5f,
            ),
            radius = middleR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.6.dp.toPx()),
        )

        // Inner core dark-cobalt circle cradling the All Apps circular icon
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF04102E).copy(alpha = 0.56f * fillScale),
                    Color(0xFF082056).copy(alpha = 0.38f * fillScale),
                ),
                center = Offset(cx, cy),
                radius = innerR,
            ),
            radius = innerR,
            center = Offset(cx, cy),
        )
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    PureWhite.copy(alpha = 0.65f),
                    accent.copy(alpha = 0.48f),
                    accent.copy(alpha = 0.14f),
                ),
                startY = cy - innerR,
                endY = cy + innerR * 0.4f,
            ),
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.3.dp.toPx()),
        )
    }
}

/**
 * Prayer ("ADANE") frosted card silhouette: upper edge reaches toward the central clock,
 * while its lower-right shoulder curves smoothly inward around the concentric orbit ring.
 */
fun buildOrbitPrayerCardPath(
    size: Size,
    cornerRadiusPx: Float,
    cutoutDepthPx: Float,
): Path {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return Path()

    val r = cornerRadiusPx.coerceIn(6f, min(w, h) * 0.22f)
    val notch = cutoutDepthPx.coerceIn(0f, w * 0.22f)
    val stepYTop = h * 0.30f
    val stepYBot = h * 0.64f
    val rightBottomX = w - notch

    return Path().apply {
        moveTo(r, 0f)
        lineTo(w - r, 0f)
        quadraticTo(w, 0f, w, r)
        lineTo(w, stepYTop)
        cubicTo(
            w,
            stepYTop + (stepYBot - stepYTop) * 0.45f,
            rightBottomX,
            stepYTop + (stepYBot - stepYTop) * 0.55f,
            rightBottomX,
            stepYBot,
        )
        lineTo(rightBottomX, h - r)
        quadraticTo(rightBottomX, h, rightBottomX - r, h)
        lineTo(r, h)
        quadraticTo(0f, h, 0f, h - r)
        lineTo(0f, r)
        quadraticTo(0f, 0f, r, 0f)
        close()
    }
}

/**
 * Weather ("Meteo") frosted card silhouette: exact mirror image of the Prayer ("ADANE") card,
 * keeping top edges strictly symmetrical on both sides of the central Clock while its lower-left
 * shoulder curves gracefully inward around the concentric orbit ring.
 */
fun buildOrbitWeatherCardPath(
    size: Size,
    cornerRadiusPx: Float,
    cutoutDepthPx: Float,
): Path {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return Path()

    val r = cornerRadiusPx.coerceIn(6f, min(w, h) * 0.22f)
    val notch = cutoutDepthPx.coerceIn(0f, w * 0.22f)
    val stepYTop = h * 0.30f
    val stepYBot = h * 0.64f
    val leftBottomX = notch

    return Path().apply {
        moveTo(r, 0f)
        lineTo(w - r, 0f)
        quadraticTo(w, 0f, w, r)
        lineTo(w, h - r)
        quadraticTo(w, h, w - r, h)
        lineTo(leftBottomX + r, h)
        quadraticTo(leftBottomX, h, leftBottomX, h - r)
        lineTo(leftBottomX, stepYBot)
        cubicTo(
            leftBottomX,
            stepYTop + (stepYBot - stepYTop) * 0.55f,
            0f,
            stepYTop + (stepYBot - stepYTop) * 0.45f,
            0f,
            stepYTop,
        )
        lineTo(0f, r)
        quadraticTo(0f, 0f, r, 0f)
        close()
    }
}

/**
 * Focusable futuristic glass container rendered along a custom contour path (for `ADANE` and `Meteo` panels).
 */
@Composable
fun OrbitContouredCardSurface(
    onClick: () -> Unit,
    accentColor: Color,
    surfaceColor: Color,
    glassAlpha: Float,
    cutoutDepth: Dp,
    cornerRadius: Dp,
    isLeftPrayerShape: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (isFocused: Boolean) -> Unit,
) {
    val tvFocusEnabled = LocalTvFocusEnabled.current
    val navSoundEnabled = LocalNavigationSoundEnabled.current
    val context = LocalContext.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val clickInteractionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val highlighted = isFocused || isHovered
    var wasFocused by remember { mutableStateOf(false) }
    var confirmKeyDownReceived by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (!wasFocused && isFocused && tvFocusEnabled) {
            FocusSoundPlayer.playFocusMove(context, view, navSoundEnabled)
        }
        if (!isFocused) {
            confirmKeyDownReceived = false
        }
        wasFocused = isFocused
    }

    val scale by animateFloatAsState(
        targetValue = if (highlighted) 1.024f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "orbitCardScale",
    )
    val rimColor by animateColorAsState(
        targetValue = if (highlighted) PureWhite else accentColor.copy(alpha = 0.85f),
        animationSpec = tween(durationMillis = 100),
        label = "orbitCardRim",
    )

    val density = LocalDensity.current
    val rPx = with(density) { cornerRadius.toPx() }
    val notchPx = with(density) { cutoutDepth.toPx() }
    val effectiveAlpha = glassAlpha.coerceIn(0f, 0.95f)

    val clipShape = remember(rPx, notchPx, isLeftPrayerShape) {
        GenericShape { size, _ ->
            val path = if (isLeftPrayerShape) {
                buildOrbitPrayerCardPath(size, rPx, notchPx)
            } else {
                buildOrbitWeatherCardPath(size, rPx, notchPx)
            }
            addPath(path)
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(clipShape)
            .drawWithContent {
                val path = if (isLeftPrayerShape) {
                    buildOrbitPrayerCardPath(size, rPx, notchPx)
                } else {
                    buildOrbitWeatherCardPath(size, rPx, notchPx)
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = if (highlighted) {
                            listOf(
                                accentColor.copy(alpha = 0.38f),
                                surfaceColor.copy(alpha = (effectiveAlpha + 0.16f).coerceIn(0.32f, 0.92f)),
                            )
                        } else {
                            listOf(
                                surfaceColor.copy(alpha = (effectiveAlpha + 0.08f).coerceIn(0.14f, 0.90f)),
                                surfaceColor.copy(alpha = effectiveAlpha.coerceIn(0.08f, 0.86f)),
                            )
                        }
                    ),
                )
                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (highlighted) 0.24f else 0.12f),
                            Color.Transparent,
                        ),
                        center = Offset(if (isLeftPrayerShape) size.width else 0f, size.height * 0.5f),
                        radius = size.maxDimension * 0.72f,
                    ),
                )
                drawContent()
                // Soft outer neon halo + crisp rim stroke
                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = if (highlighted) 0.45f else 0.24f),
                    style = Stroke(width = if (highlighted) 6.dp.toPx() else 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PureWhite.copy(alpha = if (highlighted) 0.95f else 0.70f),
                            rimColor,
                            accentColor.copy(alpha = 0.55f),
                        ),
                    ),
                    style = Stroke(width = if (highlighted) 2.4.dp.toPx() else 1.3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
            .onKeyEvent { keyEvent ->
                val isConfirmKey = keyEvent.key == Key.DirectionCenter ||
                    keyEvent.key == Key.Enter ||
                    keyEvent.key == Key.NumPadEnter
                if (tvFocusEnabled && isConfirmKey) {
                    when (keyEvent.type) {
                        KeyEventType.KeyDown -> {
                            if (keyEvent.nativeKeyEvent.repeatCount == 0) {
                                confirmKeyDownReceived = true
                            }
                            true
                        }
                        KeyEventType.KeyUp -> {
                            val shouldClick = confirmKeyDownReceived
                            confirmKeyDownReceived = false
                            if (shouldClick) {
                                onClick()
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusProperties { canFocus = tvFocusEnabled }
            .focusable(enabled = tvFocusEnabled, interactionSource = interactionSource)
            .focusProperties { canFocus = false }
            .clickable(
                enabled = tvFocusEnabled,
                interactionSource = clickInteractionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        content(highlighted)
    }
}

/**
 * Large circular App Library medallion framed by a translucent circular band bearing
 * curved top & bottom labels and centered glowing red 3x3 app grid squares.
 */
@Composable
fun OrbitAllAppsButton(
    label: String,
    ringLabel: String = "TOUTES LES APPLICATIONS",
    surfaceHex: Long,
    glassAlpha: Float,
    accent: Color,
    buttonSize: Dp = 114.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val ringText = remember(ringLabel) { ringLabel.trim() }
    val ringBoldTypeface = remember { Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
    val whitePaint = remember(ringBoldTypeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PureWhite.toArgb()
            textAlign = Paint.Align.CENTER
            typeface = ringBoldTypeface
            letterSpacing = 0.06f
        }
    }
    val shadowPaint = remember(ringBoldTypeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFF030712).copy(alpha = 0.72f).toArgb()
            textAlign = Paint.Align.CENTER
            typeface = ringBoldTypeface
            letterSpacing = 0.06f
        }
    }
    val topArcPath = remember { android.graphics.Path() }
    val bottomArcPath = remember { android.graphics.Path() }
    val arcRect = remember { android.graphics.RectF() }

    TvFocusableSurface(
        onClick = onClick,
        accentColor = accent,
        shape = CircleShape,
        baseColor = Color(surfaceHex).copy(alpha = (glassAlpha * 0.28f).coerceIn(0.14f, 0.36f)),
        focusedColor = Color(surfaceHex).copy(alpha = (glassAlpha + 0.16f).coerceIn(0.28f, 0.56f)),
        baseBorderColor = PureWhite.copy(alpha = 0.24f),
        modifier = modifier
            .size(buttonSize)
            .semantics { this.contentDescription = label }
            .testTag("home_all_apps_button"),
    ) { isFocused ->
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = size.minDimension / 2f
            if (outerR <= 0f) return@Canvas

            val innerR = outerR * 0.65f
            val bandWidth = (outerR - innerR).coerceAtLeast(1f)
            val bandMidR = innerR + bandWidth / 2f

            // 1. Translucent circular ribbon/band around the inner medallion
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PureWhite.copy(alpha = if (isFocused) 0.18f else 0.08f),
                        Color(surfaceHex).copy(alpha = (glassAlpha * 0.38f).coerceIn(0.18f, 0.46f)),
                        PureWhite.copy(alpha = if (isFocused) 0.22f else 0.10f),
                    ),
                    center = Offset(cx, cy),
                    radius = outerR,
                ),
                radius = bandMidR,
                center = Offset(cx, cy),
                style = Stroke(width = bandWidth),
            )
            drawCircle(
                color = if (isFocused) PureWhite.copy(alpha = 0.78f) else PureWhite.copy(alpha = 0.20f),
                radius = outerR - 0.75.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 1.1.dp.toPx()),
            )

            // 2. Inner deep indigo-cobalt glossy circular medallion
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isFocused) Color(0xFF2D226D) else Color(0xFF231954),
                        Color(0xFF191342),
                        Color(0xFF110D30),
                    ),
                    startY = cy - innerR,
                    endY = cy + innerR,
                ),
                radius = innerR,
                center = Offset(cx, cy),
                alpha = (glassAlpha + 0.34f).coerceIn(0.84f, 0.96f),
            )

            // Subtle radial spotlight inside medallion
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4C3A9E).copy(alpha = if (isFocused) 0.34f else 0.20f),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy - innerR * 0.18f),
                    radius = innerR,
                ),
                radius = innerR,
                center = Offset(cx, cy),
            )

            // Beveled inner rim border separating center medallion from outer translucent band
            val bevelWidth = (outerR * 0.065f).coerceIn(1.5.dp.toPx(), 4.dp.toPx())
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isFocused) PureWhite.copy(alpha = 0.92f) else Color(0xFFDCD6FF).copy(alpha = 0.76f),
                        Color(0xFF7B6FA8).copy(alpha = 0.45f),
                        Color(0xFF1E1945).copy(alpha = 0.85f),
                    ),
                    startY = cy - innerR,
                    endY = cy + innerR,
                ),
                radius = innerR,
                center = Offset(cx, cy),
                style = Stroke(width = bevelWidth),
            )
            drawCircle(
                color = PureWhite.copy(alpha = if (isFocused) 0.52f else 0.18f),
                radius = (innerR - bevelWidth * 0.65f).coerceAtLeast(1f),
                center = Offset(cx, cy),
                style = Stroke(width = 0.9.dp.toPx()),
            )

            // 3. Centered 3x3 vibrant glowing crimson-red rounded squares
            val gridSpan = innerR * 0.98f
            val gap = gridSpan * 0.14f
            val cell = (gridSpan - gap * 2f) / 3f
            val startX = cx - gridSpan / 2f
            val startY = cy - gridSpan / 2f
            val cr = CornerRadius(cell * 0.22f, cell * 0.22f)
            val glowPad = cell * 0.18f

            for (row in 0..2) {
                for (col in 0..2) {
                    val left = startX + col * (cell + gap)
                    val top = startY + row * (cell + gap)
                    val tileCenter = Offset(left + cell / 2f, top + cell / 2f)

                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF243D).copy(alpha = if (isFocused) 0.56f else 0.36f),
                                Color.Transparent,
                            ),
                            center = tileCenter,
                            radius = cell * 0.95f,
                        ),
                        topLeft = Offset(left - glowPad, top - glowPad),
                        size = Size(cell + glowPad * 2f, cell + glowPad * 2f),
                        cornerRadius = CornerRadius(cr.x + glowPad, cr.y + glowPad),
                    )

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF334B),
                                Color(0xFFE61938),
                            ),
                            startY = top,
                            endY = top + cell,
                        ),
                        topLeft = Offset(left, top),
                        size = Size(cell, cell),
                        cornerRadius = cr,
                    )

                    drawRoundRect(
                        color = Color(0xFFFF98A6).copy(alpha = 0.58f),
                        topLeft = Offset(left, top),
                        size = Size(cell, cell),
                        cornerRadius = cr,
                        style = Stroke(width = (cell * 0.06f).coerceAtLeast(0.75f)),
                    )
                }
            }

            // 4. Curved text on TOP half AND bottom half of the outer translucent circular band + side dots
            if (ringText.isNotEmpty()) {
                val baseTextSize = (bandWidth * 0.52f).coerceAtLeast(5f)
                val maxArcLength = bandMidR * (150f * Math.PI.toFloat() / 180f)
                whitePaint.textSize = baseTextSize
                val measured = whitePaint.measureText(ringText)
                if (measured > maxArcLength && measured > 0f) {
                    whitePaint.textSize = (baseTextSize * (maxArcLength / measured)).coerceAtLeast(4f)
                }
                shadowPaint.textSize = whitePaint.textSize

                val fm = whitePaint.fontMetrics
                val vOffset = -(fm.ascent + fm.descent) / 2f
                arcRect.set(
                    cx - bandMidR,
                    cy - bandMidR,
                    cx + bandMidR,
                    cy + bandMidR,
                )

                topArcPath.reset()
                topArcPath.addArc(arcRect, 188f, 164f)
                bottomArcPath.reset()
                bottomArcPath.addArc(arcRect, 8f, 164f)

                val native = drawContext.canvas.nativeCanvas
                native.drawTextOnPath(ringText, topArcPath, 0f, vOffset + 1.1f, shadowPaint)
                native.drawTextOnPath(ringText, topArcPath, 0f, vOffset, whitePaint)
                native.drawTextOnPath(ringText, bottomArcPath, 0f, vOffset + 1.1f, shadowPaint)
                native.drawTextOnPath(ringText, bottomArcPath, 0f, vOffset, whitePaint)

                val dotR = (bandWidth * 0.11f).coerceIn(1.3f, 4.2f)
                drawCircle(
                    color = Color(0xFF030712).copy(alpha = 0.65f),
                    radius = dotR + 0.8f,
                    center = Offset(cx - bandMidR, cy + 0.8f),
                )
                drawCircle(
                    color = PureWhite.copy(alpha = 0.94f),
                    radius = dotR,
                    center = Offset(cx - bandMidR, cy),
                )
                drawCircle(
                    color = Color(0xFF030712).copy(alpha = 0.65f),
                    radius = dotR + 0.8f,
                    center = Offset(cx + bandMidR, cy + 0.8f),
                )
                drawCircle(
                    color = PureWhite.copy(alpha = 0.94f),
                    radius = dotR,
                    center = Offset(cx + bandMidR, cy),
                )
            }
        }
    }
}
