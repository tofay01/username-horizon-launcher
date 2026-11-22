package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ClockTheme
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.LocalizedStrings
import com.example.ui.theme.PureWhite
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class ClockModeFilter {
    ALL,
    DIGITAL,
    ANALOG,
}

data class ParsedClockParts(
    val hourStr: String,
    val minuteStr: String,
    val amPm: String,
    val hourInt: Int,
    val minuteInt: Int,
    val secondInt: Int,
)

fun parseClockTimeParts(formattedTime: String): ParsedClockParts {
    val normalized = buildString {
        formattedTime.forEach { ch ->
            append(
                when (ch) {
                    in '٠'..'٩' -> ('0' + (ch - '٠'))
                    in '۰'..'۹' -> ('0' + (ch - '۰'))
                    else -> ch
                }
            )
        }
    }.trim()
    val nums = Regex("""\d{1,2}""").findAll(normalized).map { it.value }.toList()
    val hourRaw = nums.getOrNull(0) ?: "10"
    val minRaw = (nums.getOrNull(1) ?: "08").padStart(2, '0').take(2)
    val amPm = normalized.replace(Regex("""[\d:\s]+"""), "").trim().take(4)
    val sec = Calendar.getInstance().get(Calendar.SECOND)
    return ParsedClockParts(
        hourStr = hourRaw,
        minuteStr = minRaw,
        amPm = amPm,
        hourInt = hourRaw.toIntOrNull()?.coerceIn(0, 23) ?: 10,
        minuteInt = minRaw.toIntOrNull()?.coerceIn(0, 59) ?: 8,
        secondInt = sec,
    )
}

@Composable
fun ThemedClockDisplay(
    formattedTime: String,
    formattedDate: String,
    settings: LauncherSettings,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    val theme = settings.clockTheme
    if (theme == ClockTheme.CLASSIC_HORIZON) {
        ContrastText(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isCompact) 32.sp else 44.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            ),
            enhancedContrast = settings.enhancedContrast,
            textAlign = TextAlign.Center,
            modifier = modifier.testTag("home_clock_theme_${theme.id}"),
        )
        return
    }

    val parts = remember(formattedTime) { parseClockTimeParts(formattedTime) }
    val accent = settings.accentColor
    val barHeight = when {
        theme.isAnalog && isCompact -> 62.dp
        theme.isAnalog -> 74.dp
        isCompact -> 50.dp
        else -> 60.dp
    }
    val widthModifier = if (isCompact) {
        Modifier.widthIn(min = 160.dp, max = 210.dp)
    } else {
        Modifier.widthIn(min = 180.dp, max = 216.dp).fillMaxWidth()
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.1.dp, watchThemeBorderColor(theme, accent)),
        shadowElevation = 6.dp,
        modifier = modifier
            .then(widthModifier)
            .height(barHeight)
            .testTag("home_clock_theme_${theme.id}"),
    ) {
        WatchFaceSurfaceContent(
            theme = theme,
            parts = parts,
            formattedTime = formattedTime,
            shortDate = formattedDate.take(14),
            accent = accent,
            isCompact = isCompact,
            isPreview = false,
        )
    }
}

private fun watchThemeBorderColor(theme: ClockTheme, accent: Color): Color = when (theme) {
    ClockTheme.BLACK_ORANGE -> Color(0xFFFF9500).copy(alpha = 0.70f)
    ClockTheme.PURE_GOLD_LCD -> Color(0xFFE7C56E).copy(alpha = 0.70f)
    ClockTheme.SIMPLE_RED -> Color(0xFFE63946).copy(alpha = 0.72f)
    ClockTheme.RETRO_VINTAGE -> Color(0xFF9E7B4F).copy(alpha = 0.76f)
    ClockTheme.PLAIN_WHITE -> Color.White.copy(alpha = 0.88f)
    ClockTheme.MECHANICAL_BLUE -> Color(0xFF60A5FA).copy(alpha = 0.78f)
    ClockTheme.LACK_CLASSIC -> Color(0xFFCBB199).copy(alpha = 0.72f)
    ClockTheme.THREE_D_DIGITS -> Color(0xFFF4B183).copy(alpha = 0.60f)
    ClockTheme.EXQUISITE_NEON -> Color(0xFFA855F7).copy(alpha = 0.72f)
    ClockTheme.FUNCTION_BLUE -> Color(0xFF7DD3FC).copy(alpha = 0.80f)
    ClockTheme.LCD_OFFROAD -> Color(0xFFA3E635).copy(alpha = 0.58f)
    ClockTheme.STARRY_SKY -> Color(0xFF93C5FD).copy(alpha = 0.60f)
    ClockTheme.INK_ECHOES -> Color(0xFF94A3B8).copy(alpha = 0.60f)
    ClockTheme.BIG_SIMPLE_FLIP -> Color(0xFF64748B).copy(alpha = 0.65f)
    ClockTheme.CLASSIC_HORIZON -> accent.copy(alpha = 0.6f)
}

@Composable
private fun WatchFaceSurfaceContent(
    theme: ClockTheme,
    parts: ParsedClockParts,
    formattedTime: String,
    shortDate: String,
    accent: Color,
    isCompact: Boolean,
    isPreview: Boolean,
) {
    val hFontSize = when {
        isPreview -> 22.sp
        isCompact -> 26.sp
        else -> 32.sp
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(watchThemeBrush(theme, accent)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (theme) {
                ClockTheme.STARRY_SKY -> {
                    val peak = Path().apply {
                        moveTo(0f, size.height * 0.82f)
                        lineTo(size.width * 0.22f, size.height * 0.56f)
                        lineTo(size.width * 0.44f, size.height * 0.74f)
                        lineTo(size.width * 0.72f, size.height * 0.46f)
                        lineTo(size.width, size.height * 0.78f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(peak, Color(0xFF060E24).copy(alpha = 0.82f))
                    listOf(0.10f to 0.22f, 0.26f to 0.14f, 0.76f to 0.20f, 0.90f to 0.26f, 0.52f to 0.24f).forEach { (sx, sy) ->
                        drawCircle(Color.White.copy(alpha = 0.75f), radius = 1.6f, center = Offset(size.width * sx, size.height * sy))
                    }
                }
                ClockTheme.EXQUISITE_NEON -> {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color(0xFFA855F7), Color(0xFF38BDF8), Color(0xFFEC4899), Color(0xFFA855F7))),
                        startAngle = -65f,
                        sweepAngle = 215f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.05f, size.height * 0.10f),
                        size = Size(size.height * 0.80f, size.height * 0.80f),
                        style = Stroke(width = if (isPreview) 4f else 5f, cap = StrokeCap.Round),
                    )
                }
                ClockTheme.INK_ECHOES -> {
                    drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), 1.5f)
                    drawLine(Color(0xFF38BDF8).copy(alpha = 0.35f), Offset(12f, size.height - 5f), Offset(size.width - 12f, size.height - 5f), 2f)
                }
                ClockTheme.LCD_OFFROAD -> {
                    val bracket = Color(0xFFF59E0B)
                    drawLine(bracket, Offset(8f, 7f), Offset(26f, 7f), 2.5f)
                    drawLine(bracket, Offset(8f, 7f), Offset(8f, 20f), 2.5f)
                    drawLine(Color(0xFFA3E635), Offset(size.width - 26f, size.height - 7f), Offset(size.width - 8f, size.height - 7f), 2.5f)
                    drawLine(Color(0xFFA3E635), Offset(size.width - 8f, size.height - 20f), Offset(size.width - 8f, size.height - 7f), 2.5f)
                }
                ClockTheme.THREE_D_DIGITS -> {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFFF59E0B).copy(alpha = 0.22f), Color.Transparent)),
                        radius = size.maxDimension * 0.45f,
                        center = Offset(size.width * 0.5f, size.height * 0.45f),
                    )
                }
                else -> Unit
            }
        }

        if (theme.isAnalog) {
            // PURE CLASSIC ANALOG ONLY — Zero digital digits beside the dial
            val railColor = when (theme) {
                ClockTheme.SIMPLE_RED -> Color(0xFFFDE68A).copy(alpha = 0.36f)
                ClockTheme.RETRO_VINTAGE -> Color(0xFF5C4024).copy(alpha = 0.32f)
                ClockTheme.PLAIN_WHITE -> Color(0xFF0F172A).copy(alpha = 0.18f)
                ClockTheme.LACK_CLASSIC -> Color(0xFF3F2E21).copy(alpha = 0.30f)
                else -> Color(0xFFFACC15).copy(alpha = 0.36f)
            }
            val dialSize = when {
                isPreview -> 46.dp
                isCompact -> 54.dp
                else -> 66.dp
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isPreview) 6.dp else 16.dp),
            ) {
                if (!isPreview) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.2.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, railColor)))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                }
                AnalogWatchDialCanvas(
                    theme = theme,
                    hourInt = parts.hourInt,
                    minuteInt = parts.minuteInt,
                    secondInt = parts.secondInt,
                    dialSize = dialSize,
                    isPreview = isPreview,
                )
                if (!isPreview) {
                    Spacer(modifier = Modifier.width(14.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.2.dp)
                            .background(Brush.horizontalGradient(listOf(railColor, Color.Transparent)))
                    )
                }
            }
        } else {
            // PURE DIGITAL ONLY — Centered harmoniously inside the 268.dp bar
            when (theme) {
                ClockTheme.BLACK_ORANGE -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isPreview) 8.dp else 16.dp),
                ) {
                    Text(
                        text = parts.hourStr,
                        style = TextStyle(fontSize = hFontSize, fontWeight = FontWeight.Black),
                        color = Color(0xFFFF9800),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = parts.minuteStr,
                        style = TextStyle(fontSize = hFontSize, fontWeight = FontWeight.Bold),
                        color = PureWhite,
                    )
                    if (parts.amPm.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parts.amPm,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF9800),
                        )
                    }
                }

                ClockTheme.BIG_SIMPLE_FLIP -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isPreview) 8.dp else 14.dp),
                ) {
                    FlipDigitCard(digits = parts.hourStr, isPreview = isPreview, isCompact = isCompact)
                    Spacer(modifier = Modifier.width(8.dp))
                    FlipDigitCard(digits = parts.minuteStr, isPreview = isPreview, isCompact = isCompact)
                }

                ClockTheme.THREE_D_DIGITS -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isPreview) 10.dp else 16.dp),
                ) {
                    Text(
                        text = "${parts.hourStr}:${parts.minuteStr}",
                        style = TextStyle(
                            fontSize = if (isPreview) 22.sp else if (isCompact) 26.sp else 33.sp,
                            fontWeight = FontWeight.Black,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFE4C4), Color(0xFFE09F6B), Color(0xFF8C532B))
                            ),
                        ),
                        maxLines = 1,
                    )
                }

                ClockTheme.PURE_GOLD_LCD -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isPreview) 10.dp else 16.dp),
                ) {
                    Text(
                        text = "${parts.hourStr}:${parts.minuteStr}",
                        style = TextStyle(
                            fontSize = hFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.5.sp,
                        ),
                        color = Color(0xFFEBD07A),
                        maxLines = 1,
                    )
                    if (!isPreview && parts.amPm.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parts.amPm,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                            color = Color(0xFFC49E3D),
                        )
                    }
                }

                else -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isPreview) 10.dp else 16.dp),
                ) {
                    Text(
                        text = if (theme == ClockTheme.CLASSIC_HORIZON) formattedTime else "${parts.hourStr}:${parts.minuteStr}",
                        style = TextStyle(
                            fontSize = hFontSize,
                            fontWeight = if (theme == ClockTheme.FUNCTION_BLUE) FontWeight.Black else FontWeight.ExtraBold,
                            fontFamily = if (theme == ClockTheme.LCD_OFFROAD || theme == ClockTheme.INK_ECHOES) FontFamily.Monospace else FontFamily.SansSerif,
                            letterSpacing = if (theme == ClockTheme.EXQUISITE_NEON) 1.5.sp else 0.8.sp,
                        ),
                        color = when (theme) {
                            ClockTheme.LCD_OFFROAD -> Color(0xFFD9F99D)
                            ClockTheme.INK_ECHOES -> Color(0xFFE2E8F0)
                            else -> PureWhite
                        },
                        maxLines = 1,
                    )
                    if (parts.amPm.isNotBlank() && theme != ClockTheme.CLASSIC_HORIZON) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parts.amPm,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = PureWhite.copy(alpha = 0.82f),
                        )
                    }
                    if (!isPreview && shortDate.isNotBlank() && theme == ClockTheme.FUNCTION_BLUE) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = shortDate.take(9),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = PureWhite.copy(alpha = 0.85f),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipDigitCard(digits: String, isPreview: Boolean, isCompact: Boolean) {
    val cardBg = Brush.verticalGradient(listOf(Color(0xFF2B2F3A), Color(0xFF191C24)))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(0.8.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .padding(horizontal = if (isPreview) 7.dp else 12.dp, vertical = if (isPreview) 2.dp else 4.dp),
    ) {
        Text(
            text = digits,
            style = TextStyle(
                fontSize = if (isPreview) 19.sp else if (isCompact) 22.sp else 27.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
            ),
            color = Color(0xFFD6DCE5),
            maxLines = 1,
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            drawLine(
                color = Color.Black.copy(alpha = 0.65f),
                start = Offset(0f, size.height * 0.5f),
                end = Offset(size.width, size.height * 0.5f),
                strokeWidth = 1.6f,
            )
        }
    }
}

@Composable
private fun AnalogWatchDialCanvas(
    theme: ClockTheme,
    hourInt: Int,
    minuteInt: Int,
    secondInt: Int,
    dialSize: Dp,
    isPreview: Boolean,
) {
    Canvas(modifier = Modifier.size(dialSize)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val dialBrush = when (theme) {
            ClockTheme.SIMPLE_RED -> Brush.radialGradient(listOf(Color(0xFFA81D27), Color(0xFF3A060B)))
            ClockTheme.RETRO_VINTAGE -> Brush.radialGradient(listOf(Color(0xFFEBD8B0), Color(0xFFC4A671)))
            ClockTheme.PLAIN_WHITE -> Brush.radialGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)))
            ClockTheme.LACK_CLASSIC -> Brush.radialGradient(listOf(Color(0xFFD9C5B4), Color(0xFFB59B87)))
            else -> Brush.radialGradient(listOf(Color(0xFF1E40AF), Color(0xFF0A1A44)))
        }
        drawCircle(brush = dialBrush, radius = radius, center = center)

        val tickColor = when (theme) {
            ClockTheme.SIMPLE_RED -> Color(0xFFFDE68A)
            ClockTheme.RETRO_VINTAGE -> Color(0xFF3B2B17)
            ClockTheme.PLAIN_WHITE -> Color(0xFF0F172A)
            ClockTheme.LACK_CLASSIC -> Color(0xFF2B2118)
            else -> Color(0xFFFACC15)
        }
        drawCircle(color = tickColor.copy(alpha = 0.80f), radius = radius - 1f, center = center, style = Stroke(width = 1.8f))

        if (theme == ClockTheme.MECHANICAL_BLUE) {
            drawCircle(color = Color(0xFF93C5FD).copy(alpha = 0.45f), radius = radius * 0.46f, center = center, style = Stroke(width = 1.6f))
            drawCircle(color = Color(0xFFFACC15).copy(alpha = 0.35f), radius = radius * 0.28f, center = center, style = Stroke(width = 1.2f))
        }

        for (i in 0 until 12) {
            val angle = (i * 30.0 - 90.0) * PI / 180.0
            val inner = radius * if (i % 3 == 0) 0.74f else 0.82f
            val outer = radius * 0.92f
            drawLine(
                color = tickColor,
                start = Offset(center.x + (inner * cos(angle)).toFloat(), center.y + (inner * sin(angle)).toFloat()),
                end = Offset(center.x + (outer * cos(angle)).toFloat(), center.y + (outer * sin(angle)).toFloat()),
                strokeWidth = if (i % 3 == 0) 2.2f else 1.1f,
            )
        }

        // Crisp 12/3/6/9 or XII/III/VI/IX cardinal markers on full-size dial
        if (!isPreview) {
            val markers = when (theme) {
                ClockTheme.SIMPLE_RED, ClockTheme.LACK_CLASSIC -> listOf("XII", "III", "VI", "IX")
                else -> listOf("12", "3", "6", "9")
            }
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = tickColor.toArgb()
                textSize = radius * 0.28f
                textAlign = Paint.Align.CENTER
                typeface = if (theme == ClockTheme.RETRO_VINTAGE || theme == ClockTheme.SIMPLE_RED || theme == ClockTheme.LACK_CLASSIC) {
                    Typeface.create(Typeface.SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
            }
            val labelR = radius * 0.57f
            val coords = listOf(
                Offset(center.x, center.y - labelR + textPaint.textSize * 0.35f),
                Offset(center.x + labelR, center.y + textPaint.textSize * 0.35f),
                Offset(center.x, center.y + labelR + textPaint.textSize * 0.35f),
                Offset(center.x - labelR, center.y + textPaint.textSize * 0.35f),
            )
            markers.forEachIndexed { idx, txt ->
                drawContext.canvas.nativeCanvas.drawText(txt, coords[idx].x, coords[idx].y, textPaint)
            }
        }

        val hAngle = (((hourInt % 12) + minuteInt / 60f) * 30f - 90f) * (PI / 180.0)
        val mAngle = (minuteInt * 6f - 90f) * (PI / 180.0)
        val sAngle = (secondInt * 6f - 90f) * (PI / 180.0)
        val handColor = when (theme) {
            ClockTheme.RETRO_VINTAGE, ClockTheme.PLAIN_WHITE, ClockTheme.LACK_CLASSIC -> Color(0xFF111827)
            else -> Color(0xFFFDE68A)
        }
        drawLine(
            color = handColor,
            start = center,
            end = Offset(center.x + (radius * 0.48f * cos(hAngle)).toFloat(), center.y + (radius * 0.48f * sin(hAngle)).toFloat()),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = handColor,
            start = center,
            end = Offset(center.x + (radius * 0.72f * cos(mAngle)).toFloat(), center.y + (radius * 0.72f * sin(mAngle)).toFloat()),
            strokeWidth = 2.1f,
            cap = StrokeCap.Round,
        )
        val secondColor = when (theme) {
            ClockTheme.PLAIN_WHITE, ClockTheme.LACK_CLASSIC -> Color(0xFFDC2626)
            ClockTheme.SIMPLE_RED -> Color(0xFFFFFFFF)
            else -> Color(0xFF38BDF8)
        }
        drawLine(
            color = secondColor,
            start = center,
            end = Offset(center.x + (radius * 0.78f * cos(sAngle)).toFloat(), center.y + (radius * 0.78f * sin(sAngle)).toFloat()),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round,
        )
        drawCircle(color = secondColor, radius = 2.8f, center = center)
    }
}

private fun watchThemeBrush(theme: ClockTheme, accent: Color): Brush = when (theme) {
    ClockTheme.BLACK_ORANGE -> Brush.linearGradient(listOf(Color(0xFF0A0A0D), Color(0xFF18181F)))
    ClockTheme.BIG_SIMPLE_FLIP -> Brush.verticalGradient(listOf(Color(0xFF0B0E14), Color(0xFF151923)))
    ClockTheme.PURE_GOLD_LCD -> Brush.linearGradient(listOf(Color(0xFF0D0C08), Color(0xFF241D0E)))
    ClockTheme.EXQUISITE_NEON -> Brush.linearGradient(listOf(Color(0xFF0B0B14), Color(0xFF1D1038)))
    ClockTheme.STARRY_SKY -> Brush.verticalGradient(listOf(Color(0xFF1E2949), Color(0xFF344875), Color(0xFF111A33)))
    ClockTheme.THREE_D_DIGITS -> Brush.radialGradient(listOf(Color(0xFF211710), Color(0xFF070709)))
    ClockTheme.INK_ECHOES -> Brush.verticalGradient(listOf(Color(0xFF1A202C), Color(0xFF0F131A)))
    ClockTheme.SIMPLE_RED -> Brush.linearGradient(listOf(Color(0xFF450A0F), Color(0xFF230609)))
    ClockTheme.RETRO_VINTAGE -> Brush.linearGradient(listOf(Color(0xFFE6D2AA), Color(0xFFCBB182)))
    ClockTheme.PLAIN_WHITE -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)))
    ClockTheme.MECHANICAL_BLUE -> Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF0B1C48)))
    ClockTheme.LACK_CLASSIC -> Brush.linearGradient(listOf(Color(0xFFD6C2B0), Color(0xFFB39883)))
    ClockTheme.LCD_OFFROAD -> Brush.linearGradient(listOf(Color(0xFF14181F), Color(0xFF222B24)))
    ClockTheme.FUNCTION_BLUE -> Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
    ClockTheme.CLASSIC_HORIZON -> Brush.linearGradient(listOf(accent.copy(alpha = 0.25f), Color(0xFF0A1D42)))
}

@Composable
fun ClockThemePreviewThumbnail(
    theme: ClockTheme,
    accent: Color,
    formattedTime: String,
    modifier: Modifier = Modifier,
) {
    val parts = remember(formattedTime) { parseClockTimeParts(formattedTime) }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, watchThemeBorderColor(theme, accent)),
        modifier = modifier,
    ) {
        WatchFaceSurfaceContent(
            theme = theme,
            parts = parts,
            formattedTime = formattedTime,
            shortDate = "",
            accent = accent,
            isCompact = true,
            isPreview = true,
        )
    }
}

@Composable
fun ClockThemeGalleryDialog(
    selectedTheme: ClockTheme,
    language: LauncherLanguage,
    strings: LocalizedStrings,
    accent: Color,
    sampleTime: String,
    onSelectTheme: (ClockTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    var modeFilter by remember { mutableStateOf(ClockModeFilter.ALL) }
    val filteredThemes = remember(modeFilter) {
        when (modeFilter) {
            ClockModeFilter.ALL -> ClockTheme.entries
            ClockModeFilter.DIGITAL -> ClockTheme.entries.filter { !it.isAnalog }
            ClockModeFilter.ANALOG -> ClockTheme.entries.filter { it.isAnalog }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            val cols = when {
                maxWidth >= 900.dp -> 4
                maxWidth >= 600.dp -> 3
                else -> 2
            }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0A1B42).copy(alpha = 0.96f),
                border = BorderStroke(1.5.dp, accent.copy(alpha = 0.72f)),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 880.dp)
                    .heightIn(max = (maxHeight * 0.88f))
                    .testTag("clock_theme_gallery_dialog"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.clockThemeGalleryTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = PureWhite,
                            )
                            Text(
                                text = strings.clockThemeResetNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCBD5E1),
                            )
                        }
                        TvFocusableSurface(
                            onClick = onDismiss,
                            accentColor = accent,
                            shape = CircleShape,
                            baseColor = Color.White.copy(alpha = 0.10f),
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("clock_theme_gallery_close"),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = strings.backLabel,
                                    tint = PureWhite,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassSegmentedSelector(
                        options = ClockModeFilter.entries,
                        selected = modeFilter,
                        labelProvider = { filter ->
                            when (filter) {
                                ClockModeFilter.ALL -> strings.clockFilterAll
                                ClockModeFilter.DIGITAL -> strings.clockFilterDigital
                                ClockModeFilter.ANALOG -> strings.clockFilterAnalog
                            }
                        },
                        accentColor = accent,
                        onSelect = { modeFilter = it },
                        testTagPrefix = "clock_gallery_filter",
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(cols),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        items(filteredThemes, key = { it.id }) { theme ->
                            val isSelected = theme == selectedTheme
                            TvFocusableSurface(
                                onClick = {
                                    onSelectTheme(theme)
                                    onDismiss()
                                },
                                accentColor = accent,
                                shape = RoundedCornerShape(18.dp),
                                baseColor = Color(0xFF0E2352).copy(alpha = 0.72f),
                                isSelected = isSelected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clock_theme_card_${theme.id}"),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        ClockThemePreviewThumbnail(
                                            theme = theme,
                                            accent = accent,
                                            formattedTime = sampleTime,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1.45f),
                                        )
                                        if (isSelected) {
                                            Surface(
                                                shape = CircleShape,
                                                color = accent,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(22.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF04142B),
                                                        modifier = Modifier.size(14.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = theme.displayLabel(language),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) accent else PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
