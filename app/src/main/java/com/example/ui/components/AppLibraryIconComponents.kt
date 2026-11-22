package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.LauncherSettings
import com.example.ui.icons.Apps
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.PureWhite

internal data class AppIconVisual(
    val bitmap: ImageBitmap?,
    val ambientColor: Color,
)

private data class CachedIconBase(
    val bitmap: ImageBitmap?,
    val dominantColor: Color?,
)

private val appIconVisualCache = LruCache<String, CachedIconBase>(180)

internal fun extractAppIconVisual(
    drawable: Drawable?,
    fallbackAccent: Color,
    cacheKey: String? = null,
): AppIconVisual {
    if (drawable == null) return AppIconVisual(bitmap = null, ambientColor = fallbackAccent)
    val identityKey = cacheKey ?: "${System.identityHashCode(drawable)}"
    val cached = appIconVisualCache.get(identityKey)
    if (cached != null) {
        return AppIconVisual(
            bitmap = cached.bitmap,
            ambientColor = cached.dominantColor ?: fallbackAccent,
        )
    }

    val computed = runCatching {
        val rawW = drawable.intrinsicWidth
        val rawH = drawable.intrinsicHeight
        val w = (if (rawW > 0) rawW else 128).coerceIn(96, 256)
        val h = (if (rawH > 0) rawH else 128).coerceIn(96, 256)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val hsv = FloatArray(3)
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0
        val stepX = (w / 10).coerceAtLeast(1)
        val stepY = (h / 10).coerceAtLeast(1)
        var y = stepY / 2
        while (y < h) {
            var x = stepX / 2
            while (x < w) {
                val pixel = bmp.getPixel(x, y)
                val alpha = android.graphics.Color.alpha(pixel)
                if (alpha >= 100) {
                    android.graphics.Color.colorToHSV(pixel, hsv)
                    if (hsv[1] >= 0.22f && hsv[2] >= 0.22f) {
                        rSum += android.graphics.Color.red(pixel)
                        gSum += android.graphics.Color.green(pixel)
                        bSum += android.graphics.Color.blue(pixel)
                        count++
                    }
                }
                x += stepX
            }
            y += stepY
        }

        val dominant = if (count >= 2) {
            Color(
                red = ((rSum / count).toInt()).coerceIn(0, 255),
                green = ((gSum / count).toInt()).coerceIn(0, 255),
                blue = ((bSum / count).toInt()).coerceIn(0, 255),
            )
        } else {
            null
        }
        CachedIconBase(bitmap = bmp.asImageBitmap(), dominantColor = dominant)
    }.getOrElse {
        CachedIconBase(bitmap = null, dominantColor = null)
    }

    appIconVisualCache.put(identityKey, computed)
    return AppIconVisual(
        bitmap = computed.bitmap,
        ambientColor = computed.dominantColor ?: fallbackAccent,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppLibraryTileCard(
    app: AppItem,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavoriteBadge: Boolean = false,
    isCompact: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    val tvFocusEnabled = LocalTvFocusEnabled.current
    val navSoundEnabled = LocalNavigationSoundEnabled.current
    val context = LocalContext.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val clickInteractionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val highlighted = tvFocusEnabled && (isFocused || isHovered)
    var longPressHandled by remember { mutableStateOf(false) }
    var wasFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused, tvFocusEnabled, navSoundEnabled) {
        if (isFocused && !wasFocused && tvFocusEnabled) {
            FocusSoundPlayer.playFocusMove(context, view, navSoundEnabled)
        }
        wasFocused = isFocused
    }

    val accent = settings.accentColor
    val drawable = app.banner ?: app.icon
    val visual = remember(app.packageName, app.banner != null, accent, drawable) {
        extractAppIconVisual(
            drawable = drawable,
            fallbackAccent = accent,
            cacheKey = "${app.packageName}:${app.banner != null}:${System.identityHashCode(drawable)}",
        )
    }
    val ambient = visual.ambientColor
    val isBanner = app.banner != null
    val animEnabled = settings.animationsEnabled

    val scale by animateFloatAsState(
        targetValue = if (highlighted) {
            if (settings.advancedEffectsEnabled && animEnabled) 1.07f else 1.04f
        } else 1f,
        animationSpec = if (animEnabled) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            snap()
        },
        label = "libTileScale",
    )
    val lift = if (highlighted) 12.dp else 3.dp
    val borderColor by animateColorAsState(
        targetValue = when {
            highlighted -> PureWhite
            isFavoriteBadge -> EmeraldGlow.copy(alpha = 0.65f)
            else -> Color.White.copy(alpha = 0.16f)
        },
        animationSpec = if (animEnabled) tween(durationMillis = 100) else snap(),
        label = "libTileBorder",
    )

    val cardShape = RoundedCornerShape(18.dp)
    val surfaceTint = Color(settings.themePreset.surfaceHex)
    val reqMod = if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier

    val pedestalWidth = when {
        isBanner && isCompact -> 66.dp
        isBanner -> 78.dp
        isCompact -> 50.dp
        else -> 58.dp
    }
    val pedestalHeight = when {
        isBanner && isCompact -> 44.dp
        isBanner -> 50.dp
        isCompact -> 50.dp
        else -> 58.dp
    }
    val iconSize = if (isCompact) 38.dp else 44.dp
    val pedestalShape = RoundedCornerShape(16.dp)

    Surface(
        shape = cardShape,
        color = surfaceTint.copy(alpha = if (highlighted) 0.78f else 0.46f),
        border = if (highlighted) {
            BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(PureWhite, ambient, accent),
                ),
            )
        } else {
            BorderStroke(1.dp, borderColor)
        },
        shadowElevation = lift,
        modifier = modifier
            .fillMaxWidth()
            .then(reqMod)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .onKeyEvent { keyEvent ->
                val isConfirmKey = keyEvent.key == Key.DirectionCenter ||
                    keyEvent.key == Key.Enter ||
                    keyEvent.key == Key.NumPadEnter
                when {
                    tvFocusEnabled && keyEvent.key == Key.Menu && keyEvent.type == KeyEventType.KeyUp -> {
                        onLongClick()
                        true
                    }
                    tvFocusEnabled && isConfirmKey -> {
                        when (keyEvent.type) {
                            KeyEventType.KeyDown -> {
                                if (keyEvent.nativeKeyEvent.repeatCount == 0) {
                                    longPressHandled = false
                                    true
                                } else if (!longPressHandled && (keyEvent.nativeKeyEvent.isLongPress || keyEvent.nativeKeyEvent.repeatCount >= 2)) {
                                    longPressHandled = true
                                    onLongClick()
                                    true
                                } else {
                                    true
                                }
                            }
                            KeyEventType.KeyUp -> {
                                if (longPressHandled) {
                                    longPressHandled = false
                                } else {
                                    onClick()
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
            .focusProperties { canFocus = tvFocusEnabled }
            .focusable(enabled = tvFocusEnabled, interactionSource = interactionSource)
            .focusProperties { canFocus = false }
            .combinedClickable(
                enabled = tvFocusEnabled,
                interactionSource = clickInteractionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .testTag("app_tile_${app.packageName}"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (highlighted) {
                            listOf(
                                ambient.copy(alpha = 0.34f),
                                surfaceTint.copy(alpha = 0.88f),
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                surfaceTint.copy(alpha = 0.32f),
                            )
                        }
                    )
                )
                .padding(horizontal = 8.dp, vertical = if (isCompact) 9.dp else 11.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(
                        width = pedestalWidth + 14.dp,
                        height = pedestalHeight + 10.dp,
                    ),
                ) {
                    // Ambient luminous halo behind the squircle pedestal
                    Box(
                        modifier = Modifier
                            .size(width = pedestalWidth + 12.dp, height = pedestalHeight + 8.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ambient.copy(alpha = if (highlighted) 0.56f else 0.30f),
                                        Color.Transparent,
                                    ),
                                ),
                                shape = CircleShape,
                            )
                    )

                    // Frosted squircle icon pedestal with specular glass rim
                    Surface(
                        shape = pedestalShape,
                        color = Color(0xFF0A1B3D).copy(alpha = 0.72f),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PureWhite.copy(alpha = if (highlighted) 0.65f else 0.30f),
                                    ambient.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.08f),
                                ),
                                start = Offset.Zero,
                                end = Offset(120f, 120f),
                            ),
                        ),
                        shadowElevation = if (highlighted) 10.dp else 4.dp,
                        modifier = Modifier.size(width = pedestalWidth, height = pedestalHeight),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PureWhite.copy(alpha = 0.16f),
                                            ambient.copy(alpha = 0.18f),
                                            DeepSpaceBlack.copy(alpha = 0.32f),
                                        ),
                                        start = Offset.Zero,
                                        end = Offset(140f, 140f),
                                    )
                                ),
                        ) {
                            val bmp = visual.bitmap
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp,
                                    contentDescription = app.label,
                                    contentScale = if (isBanner) ContentScale.Crop else ContentScale.Fit,
                                    modifier = if (isBanner) {
                                        Modifier.fillMaxSize().clip(pedestalShape)
                                    } else {
                                        Modifier
                                            .size(iconSize)
                                            .clip(RoundedCornerShape(11.dp))
                                    },
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Apps,
                                    contentDescription = app.label,
                                    tint = PureWhite,
                                    modifier = Modifier.size(iconSize * 0.72f),
                                )
                            }

                            // Subtle top-left glass specular highlight
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                PureWhite.copy(alpha = if (highlighted) 0.22f else 0.12f),
                                                Color.Transparent,
                                                Color.Transparent,
                                            ),
                                            start = Offset.Zero,
                                            end = Offset(90f, 90f),
                                        )
                                    )
                            )
                        }
                    }

                    if (isFavoriteBadge) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGlow,
                            border = BorderStroke(1.5.dp, PureWhite),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .testTag("app_favorite_badge_${app.packageName}"),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = app.label,
                                    tint = DeepSpaceBlack,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                ContrastText(
                    text = app.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = if (isCompact) 11.5.sp else 12.5.sp,
                        fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    enhancedContrast = true,
                    textAlign = TextAlign.Center,
                    color = if (highlighted) PureWhite else PureWhite.copy(alpha = 0.92f),
                    maxLines = 1,
                )
            }
        }
    }
}
