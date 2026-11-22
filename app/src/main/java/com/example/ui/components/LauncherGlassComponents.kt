package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.TextureView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import com.example.ui.icons.AcUnit
import com.example.ui.icons.Apps
import com.example.ui.icons.Cloud
import com.example.ui.icons.FilterDrama
import com.example.ui.icons.Grain
import com.example.ui.icons.NightsStay
import com.example.ui.icons.Thunderstorm
import com.example.ui.icons.WaterDrop
import com.example.ui.icons.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppIconShape
import com.example.model.AppIconSize
import com.example.model.AppItem
import com.example.model.LauncherSettings
import com.example.model.ThemePreset
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import kotlin.math.roundToInt

val LocalTvFocusEnabled = compositionLocalOf { true }

fun wallpaperDrawableRes(wallpaperId: String): Int? = when (wallpaperId) {
    "earth" -> R.drawable.img_wallpaper_earth
    "aurora" -> R.drawable.img_wallpaper_aurora
    "amber_night" -> R.drawable.img_wallpaper_amber_night
    "cobalt_waves" -> R.drawable.img_wallpaper_cobalt_waves
    "sport_real_madrid" -> R.drawable.img_sport_real_madrid
    "sport_barcelona" -> R.drawable.img_sport_barcelona
    "sport_man_city" -> R.drawable.img_sport_man_city
    "sport_psg" -> R.drawable.img_sport_psg
    "sport_brazil" -> R.drawable.img_sport_brazil
    "sport_brazil_light" -> R.drawable.img_sport_brazil_light
    "sport_argentina" -> R.drawable.img_sport_argentina
    "sport_france" -> R.drawable.img_sport_france
    "sport_spain" -> R.drawable.img_sport_spain
    "sport_egypt" -> R.drawable.img_sport_egypt
    "sport_algeria" -> R.drawable.img_sport_algeria
    "sport_algeria_fennecs" -> R.drawable.img_sport_algeria_fennecs
    "sport_saudi_arabia" -> R.drawable.img_sport_saudi_arabia
    "sport_tunisia" -> R.drawable.img_sport_tunisia
    "video_earth_space" -> R.drawable.thumb_video_earth_space
    "video_emerald_nebula" -> R.drawable.thumb_video_emerald_nebula
    "video_crimson_nebula" -> R.drawable.thumb_video_crimson_nebula
    "video_cobalt_fractal" -> R.drawable.thumb_video_cobalt_fractal
    "video_violet_nebula" -> R.drawable.thumb_video_violet_nebula
    else -> null
}

fun wallpaperVideoRawRes(wallpaperId: String): Int? = when (wallpaperId) {
    "video_earth_space" -> R.raw.vid_wallpaper_earth_space
    "video_emerald_nebula" -> R.raw.vid_wallpaper_emerald_nebula
    "video_crimson_nebula" -> R.raw.vid_wallpaper_crimson_nebula
    "video_cobalt_fractal" -> R.raw.vid_wallpaper_cobalt_fractal
    "video_violet_nebula" -> R.raw.vid_wallpaper_violet_nebula
    else -> null
}

fun isBuiltInVideoWallpaper(wallpaperId: String): Boolean = wallpaperVideoRawRes(wallpaperId) != null

@Composable
fun LauncherWallpaperLayer(
    settings: LauncherSettings,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val customUri = settings.customWallpaperUri
    val builtInVideoRawRes = if (customUri.isNullOrBlank()) wallpaperVideoRawRes(settings.wallpaperId) else null
    val activeVideoUri: String? = when {
        !customUri.isNullOrBlank() && settings.customWallpaperIsVideo -> customUri
        builtInVideoRawRes != null -> "android.resource://${context.packageName}/$builtInVideoRawRes"
        else -> null
    }

    Box(modifier = modifier.fillMaxSize().background(DeepSpaceBlack)) {
        if (!customUri.isNullOrBlank() && !settings.customWallpaperIsVideo) {
            AsyncImage(
                model = customUri,
                contentDescription = "Custom wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val posterResId = wallpaperDrawableRes(settings.wallpaperId)
                ?: wallpaperDrawableRes(settings.themePreset.defaultWallpaperId)
            if (posterResId != null) {
                Image(
                    painter = painterResource(id = posterResId),
                    contentDescription = "Launcher wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(settings.themePreset.surfaceHex),
                                    DeepSpaceBlack,
                                )
                            )
                        )
                )
            }
        }

        if (activeVideoUri != null) {
            LoopingVideoWallpaper(
                uriString = activeVideoUri,
                modifier = Modifier.fillMaxSize(),
            )
        }

        val dim = settings.wallpaperDimAlpha.coerceIn(0f, 0.75f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepSpaceBlack.copy(alpha = if (dim <= 0.01f) 0f else (dim + 0.08f).coerceIn(0f, 0.85f)),
                            DeepSpaceBlack.copy(alpha = dim),
                            DeepSpaceBlack.copy(alpha = if (dim <= 0.01f) 0f else (dim + 0.15f).coerceIn(0f, 0.90f)),
                        )
                    )
                )
        )
    }
}

@Composable
private fun LoopingVideoWallpaper(
    uriString: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }
    var firstFrameRendered by remember { mutableStateOf(false) }
    var playbackFailed by remember { mutableStateOf(false) }
    val textureViewRef = remember { arrayOfNulls<TextureView>(1) }

    fun applyCenterCrop(tv: TextureView?, vw: Int, vh: Int) {
        if (tv == null || vw <= 0 || vh <= 0 || tv.width <= 0 || tv.height <= 0) return
        val viewW = tv.width.toFloat()
        val viewH = tv.height.toFloat()
        val scale = maxOf(viewW / vw.toFloat(), viewH / vh.toFloat())
        val scaledW = vw * scale
        val scaledH = vh * scale
        val dx = (viewW - scaledW) / 2f
        val dy = (viewH - scaledH) / 2f
        val matrix = Matrix().apply {
            setScale(scaledW / viewW, scaledH / viewH)
            postTranslate(dx, dy)
        }
        tv.setTransform(matrix)
    }

    val exoPlayer = remember(context) {
        runCatching {
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    setAudioAttributes(attrs, false)
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = 0f
                    playWhenReady = true
                }
        }.getOrNull()
    }

    if (exoPlayer == null) {
        return
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val playerListener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                videoWidth = videoSize.width
                videoHeight = videoSize.height
                textureViewRef[0]?.let { tv ->
                    tv.post { applyCenterCrop(tv, videoSize.width, videoSize.height) }
                }
            }

            override fun onRenderedFirstFrame() {
                firstFrameRendered = true
                playbackFailed = false
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackFailed = true
            }
        }
        exoPlayer.addListener(playerListener)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> runCatching { exoPlayer.pause() }
                Lifecycle.Event.ON_RESUME -> runCatching {
                    if (!playbackFailed) {
                        exoPlayer.playWhenReady = true
                        exoPlayer.play()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.removeListener(playerListener)
            runCatching {
                exoPlayer.setVideoTextureView(null)
                exoPlayer.stop()
                exoPlayer.release()
            }
        }
    }

    LaunchedEffect(uriString, exoPlayer) {
        firstFrameRendered = false
        playbackFailed = false
        runCatching {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(uriString)))
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            exoPlayer.volume = 0f
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
            exoPlayer.play()
        }.onFailure {
            playbackFailed = true
        }
    }

    if (playbackFailed) {
        return
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (firstFrameRendered) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "videoWallpaperFadeIn",
    )

    AndroidView(
        modifier = modifier.graphicsLayer { alpha = videoAlpha },
        factory = { ctx ->
            TextureView(ctx).apply {
                isOpaque = false
                textureViewRef[0] = this
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                        runCatching {
                            exoPlayer.setVideoTextureView(this@apply)
                            applyCenterCrop(this@apply, videoWidth, videoHeight)
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
                        applyCenterCrop(this@apply, videoWidth, videoHeight)
                    }

                    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                        runCatching { exoPlayer.setVideoTextureView(null) }
                        if (textureViewRef[0] === this@apply) {
                            textureViewRef[0] = null
                        }
                        return true
                    }

                    override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
                }
                runCatching { exoPlayer.setVideoTextureView(this) }
            }
        },
        update = { tv ->
            textureViewRef[0] = tv
            applyCenterCrop(tv, videoWidth, videoHeight)
        },
    )
}

@Composable
fun ContrastText(
    text: String,
    style: TextStyle,
    enhancedContrast: Boolean,
    modifier: Modifier = Modifier,
    color: Color = PureWhite,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
) {
    val effectiveStyle = if (enhancedContrast) {
        style.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.85f),
                offset = Offset(1.5f, 2f),
                blurRadius = 6f,
            )
        )
    } else {
        style
    }
    Text(
        text = text,
        style = effectiveStyle,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        modifier = modifier,
    )
}

/**
 * High-contrast TV D-pad focusable container that displays a bright, unmistakable
 * border, background illumination, and subtle scale lift when focused by a remote.
 */
@Composable
fun TvFocusableSurface(
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    baseColor: Color = GlassBlue.copy(alpha = 0.56f),
    focusedColor: Color = accentColor.copy(alpha = 0.42f),
    baseBorderColor: Color = Color.White.copy(alpha = 0.24f),
    isSelected: Boolean = false,
    focusRequester: FocusRequester? = null,
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
        targetValue = if (highlighted) 1.032f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "tvSurfaceScale",
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            highlighted -> focusedColor
            isSelected -> accentColor.copy(alpha = 0.26f)
            else -> baseColor
        },
        animationSpec = tween(durationMillis = 100),
        label = "tvSurfaceBg",
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            highlighted -> PureWhite
            isSelected -> accentColor
            else -> baseBorderColor
        },
        animationSpec = tween(durationMillis = 100),
        label = "tvSurfaceBorder",
    )

    val reqMod = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }

    Surface(
        shape = shape,
        color = bgColor,
        border = BorderStroke(if (highlighted) 3.dp else if (isSelected) 1.8.dp else 1.dp, borderColor),
        shadowElevation = if (highlighted) 10.dp else 2.dp,
        modifier = modifier
            .then(reqMod)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconCard(
    app: AppItem,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabelBelow: Boolean = false,
    isFavoriteBadge: Boolean = false,
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
    val highlighted = isFocused || isHovered
    var wasFocused by remember { mutableStateOf(false) }
    var longPressHandled by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (!wasFocused && isFocused && tvFocusEnabled) {
            FocusSoundPlayer.playFocusMove(context, view, navSoundEnabled)
        }
        if (!isFocused) {
            longPressHandled = false
        }
        wasFocused = isFocused
    }

    val animEnabled = settings.animationsEnabled
    val scale by animateFloatAsState(
        targetValue = if (highlighted) {
            if (settings.advancedEffectsEnabled && animEnabled) 1.08f else 1.04f
        } else 1f,
        animationSpec = if (animEnabled) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            snap()
        },
        label = "tileScale",
    )
    val lift = if (highlighted) 12.dp else 2.dp
    val accent = settings.accentColor
    val drawable = app.banner ?: app.icon
    val visual = remember(app.packageName, app.banner != null, accent, drawable) {
        extractAppIconVisual(
            drawable = drawable,
            fallbackAccent = accent,
            cacheKey = "${app.packageName}:${app.banner != null}:${System.identityHashCode(drawable)}",
        )
    }
    val bitmap = visual.bitmap
    val ambient = visual.ambientColor
    val dockScale = settings.dockScalePercent.coerceIn(60, 130) / 100f
    val cornerRadius = settings.cardCornerRadiusDp.dp
    val tileShape: Shape = when (settings.iconShape) {
        AppIconShape.CIRCLE -> CircleShape
        AppIconShape.RECTANGLE -> RoundedCornerShape(cornerRadius)
        AppIconShape.SQUARE -> RoundedCornerShape(cornerRadius)
    }
    val baseAspectRatio = when (settings.iconShape) {
        AppIconShape.RECTANGLE -> when (settings.iconSize) {
            AppIconSize.SMALL -> 1.62f
            AppIconSize.MEDIUM -> 1.56f
            AppIconSize.LARGE -> 1.24f
        }
        AppIconShape.SQUARE, AppIconShape.CIRCLE -> 1f
    }
    val tileAspectRatio = when (settings.iconShape) {
        AppIconShape.RECTANGLE -> if (dockScale > 1f) {
            (baseAspectRatio / (0.80f + 0.20f * dockScale)).coerceAtLeast(1.16f)
        } else {
            baseAspectRatio
        }
        AppIconShape.SQUARE, AppIconShape.CIRCLE -> 1f
    }
    val tileWidthFraction = when (settings.iconShape) {
        AppIconShape.RECTANGLE -> when (settings.iconSize) {
            AppIconSize.SMALL -> 0.74f
            AppIconSize.MEDIUM -> 1f
            AppIconSize.LARGE -> 1f
        }
        AppIconShape.SQUARE, AppIconShape.CIRCLE -> when (settings.iconSize) {
            AppIconSize.SMALL -> 0.68f
            AppIconSize.MEDIUM -> 0.84f
            AppIconSize.LARGE -> 1f
        }
    }
    val tileMaxWidth = when (settings.iconShape) {
        AppIconShape.RECTANGLE -> when (settings.iconSize) {
            AppIconSize.SMALL -> (148f * dockScale).dp
            AppIconSize.MEDIUM -> (240f * dockScale).dp
            AppIconSize.LARGE -> (260f * dockScale).dp
        }
        AppIconShape.SQUARE, AppIconShape.CIRCLE -> when (settings.iconSize) {
            AppIconSize.SMALL -> (52f * dockScale).dp
            AppIconSize.MEDIUM -> (66f * dockScale).dp
            AppIconSize.LARGE -> (90f * dockScale).dp
        }
    }
    val innerIconSize = when (settings.iconSize) {
        AppIconSize.SMALL -> (28f * dockScale).dp
        AppIconSize.MEDIUM -> (36f * dockScale).dp
        AppIconSize.LARGE -> (48f * dockScale).dp
    }
    val glassAlpha = settings.glassOpacity.coerceIn(0f, 0.95f)
    val reqMod = if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = (3f * dockScale).dp),
    ) {
        Surface(
            shape = tileShape,
            color = if (highlighted) {
                Color(settings.themePreset.surfaceHex).copy(alpha = (glassAlpha + 0.24f).coerceIn(0.45f, 0.94f))
            } else {
                Color(settings.themePreset.surfaceHex).copy(alpha = glassAlpha.coerceIn(0.18f, 0.90f))
            },
            border = if (highlighted) {
                BorderStroke(
                    width = 2.6.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(PureWhite, ambient, accent),
                    ),
                )
            } else {
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isFavoriteBadge) 0.50f else 0.30f),
                            ambient.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.12f),
                        )
                    ),
                )
            },
            shadowElevation = if (glassAlpha < 0.08f && !highlighted) 0.dp else lift,
            modifier = Modifier
                .fillMaxWidth(tileWidthFraction)
                .widthIn(max = tileMaxWidth)
                .aspectRatio(tileAspectRatio)
                .then(reqMod)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(tileShape)
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
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = if (highlighted) {
                                listOf(
                                    ambient.copy(alpha = 0.34f),
                                    accent.copy(alpha = 0.24f),
                                    Color(settings.themePreset.surfaceHex).copy(alpha = 0.88f),
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.14f * glassAlpha.coerceAtLeast(0.35f)),
                                    ambient.copy(alpha = 0.16f * glassAlpha.coerceAtLeast(0.45f)),
                                    Color(settings.themePreset.surfaceHex).copy(alpha = (glassAlpha * 0.65f).coerceIn(0f, 0.85f)),
                                )
                            }
                        )
                    )
                    .padding(if (app.banner != null) (4f * dockScale).dp else (5f * dockScale).dp),
            ) {
                // Soft centered ambient radial aura tinted from the app's icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ambient.copy(alpha = if (highlighted) 0.44f else 0.22f),
                                    Color.Transparent,
                                )
                            )
                        )
                )

                val isBanner = app.banner != null
                val innerClipShape: Shape = if (settings.iconShape == AppIconShape.CIRCLE) {
                    CircleShape
                } else {
                    RoundedCornerShape((settings.cardCornerRadiusDp - 5).coerceAtLeast(8).dp)
                }

                if (bitmap != null && isBanner) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(innerClipShape),
                    )
                } else {
                    val pedestalShape: Shape = if (settings.iconShape == AppIconShape.CIRCLE) {
                        CircleShape
                    } else {
                        RoundedCornerShape(12.dp)
                    }
                    val pedestalSize = innerIconSize + (10f * dockScale).dp
                    Surface(
                        shape = pedestalShape,
                        color = Color(0xFF0A1B3D).copy(alpha = 0.68f),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PureWhite.copy(alpha = if (highlighted) 0.58f else 0.28f),
                                    ambient.copy(alpha = 0.30f),
                                    Color.White.copy(alpha = 0.08f),
                                ),
                                start = Offset.Zero,
                                end = Offset(110f, 110f),
                            ),
                        ),
                        shadowElevation = if (highlighted) 8.dp else 3.dp,
                        modifier = Modifier.size(pedestalSize),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PureWhite.copy(alpha = 0.14f),
                                            ambient.copy(alpha = 0.16f),
                                            DeepSpaceBlack.copy(alpha = 0.30f),
                                        ),
                                        start = Offset.Zero,
                                        end = Offset(120f, 120f),
                                    )
                                ),
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = app.label,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(innerIconSize)
                                        .clip(
                                            if (settings.iconShape == AppIconShape.CIRCLE) CircleShape
                                            else RoundedCornerShape(9.dp)
                                        ),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Apps,
                                    contentDescription = app.label,
                                    tint = PureWhite,
                                    modifier = Modifier.size((innerIconSize - 4.dp).coerceAtLeast(22.dp)),
                                )
                            }
                        }
                    }
                }

                // Subtle top-left glass specular sheen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PureWhite.copy(alpha = if (highlighted) 0.18f else 0.10f),
                                    Color.Transparent,
                                    Color.Transparent,
                                ),
                                start = Offset.Zero,
                                end = Offset(100f, 100f),
                            )
                        )
                )

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
        }

        if (showLabelBelow) {
            Spacer(modifier = Modifier.height(6.dp))
            ContrastText(
                text = app.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium
                ),
                enhancedContrast = true,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = if (highlighted) PureWhite else PureWhite.copy(alpha = 0.9f),
            )
        } else {
            // Fixed-height label slot prevents the Favorites Dock from jumping when focus moves up/away
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((24f * dockScale).dp)
                    .padding(top = (4f * dockScale).dp),
            ) {
                if (highlighted) {
                    ContrastText(
                        text = app.label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        enhancedContrast = true,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        color = PureWhite,
                    )
                }
            }
        }
    }
}

@Composable
fun GlassSettingCardRow(
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    leadingIcon: ImageVector? = null,
    isHighlighted: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    TvFocusableSurface(
        onClick = onClick,
        accentColor = accentColor,
        shape = RoundedCornerShape(16.dp),
        baseColor = GlassBlue.copy(alpha = 0.55f),
        focusedColor = accentColor.copy(alpha = 0.42f),
        isSelected = isHighlighted,
        focusRequester = focusRequester,
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    ) { isFocused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isFocused) PureWhite else accentColor,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = PureWhite,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isFocused) PureWhite.copy(alpha = 0.92f) else MutedSlate,
                        )
                    }
                }
            }
            if (!trailingText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isFocused) PureWhite else accentColor,
                )
            }
        }
    }
}

@Composable
fun GlassToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    focusRequester: FocusRequester? = null,
) {
    TvFocusableSurface(
        onClick = { onCheckedChange(!checked) },
        accentColor = accentColor,
        shape = RoundedCornerShape(16.dp),
        baseColor = GlassBlue.copy(alpha = 0.52f),
        focusedColor = accentColor.copy(alpha = 0.42f),
        isSelected = checked,
        focusRequester = focusRequester,
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    ) { isFocused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = PureWhite,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isFocused) PureWhite.copy(alpha = 0.92f) else MutedSlate,
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = null, // handled by parent TvFocusableSurface for clean D-pad single-stop focus
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = MutedSlate,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.15f),
                ),
            )
        }
    }
}

@Composable
fun <T> GlassSegmentedSelector(
    options: List<T>,
    selected: T,
    labelProvider: (T) -> String,
    accentColor: Color,
    onSelect: (T) -> Unit,
    testTagPrefix: String,
    firstItemFocusRequester: FocusRequester? = null,
    tagSuffixProvider: (T) -> String = labelProvider,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEachIndexed { idx, option ->
            val isSelected = option == selected
            TvFocusableSurface(
                onClick = { onSelect(option) },
                accentColor = accentColor,
                shape = RoundedCornerShape(14.dp),
                baseColor = GlassBlue.copy(alpha = 0.48f),
                focusedColor = accentColor.copy(alpha = 0.50f),
                isSelected = isSelected,
                focusRequester = if (idx == 0) firstItemFocusRequester else null,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("${testTagPrefix}_${tagSuffixProvider(option)}"),
            ) { isFocused ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = labelProvider(option),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = PureWhite,
                    )
                }
            }
        }
    }
}

@Composable
fun DockSizeStepperCard(
    dockScalePercent: Int,
    title: String,
    hint: String,
    accentColor: Color,
    onChangeScale: (Int) -> Unit,
    testTagPrefix: String = "dock_scale",
) {
    val clamped = dockScalePercent.coerceIn(60, 130)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassBlue.copy(alpha = 0.50f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().testTag("${testTagPrefix}_card"),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "$title ($clamped%)",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        modifier = Modifier.testTag("${testTagPrefix}_value"),
                    )
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TvFocusableSurface(
                        onClick = { onChangeScale((clamped - 10).coerceIn(60, 130)) },
                        accentColor = accentColor,
                        shape = CircleShape,
                        baseColor = Color.White.copy(alpha = 0.14f),
                        focusedColor = accentColor.copy(alpha = 0.55f),
                        modifier = Modifier.size(48.dp).testTag("${testTagPrefix}_decrease"),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "−",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite,
                            )
                        }
                    }
                    TvFocusableSurface(
                        onClick = { onChangeScale((clamped + 10).coerceIn(60, 130)) },
                        accentColor = accentColor,
                        shape = CircleShape,
                        baseColor = Color.White.copy(alpha = 0.14f),
                        focusedColor = accentColor.copy(alpha = 0.55f),
                        modifier = Modifier.size(48.dp).testTag("${testTagPrefix}_increase"),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite,
                            )
                        }
                    }
                }
            }
            Slider(
                value = clamped.toFloat(),
                onValueChange = { v -> onChangeScale(v.roundToInt().coerceIn(60, 130)) },
                valueRange = 60f..130f,
                colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor),
                modifier = Modifier.testTag("${testTagPrefix}_slider"),
            )
        }
    }
}

fun buildFuturisticDockPath(
    size: Size,
    crestHeightPx: Float,
    cornerRadiusPx: Float,
): Path {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return Path()
    val ch = crestHeightPx.coerceIn(0f, h * 0.25f)
    val r = cornerRadiusPx.coerceIn(4f, ((h - ch) * 0.45f).coerceAtLeast(4f))
    val shoulderL = w * 0.22f
    val shoulderR = w * 0.78f
    val slopeRun = (ch * 1.40f).coerceAtMost((shoulderR - shoulderL) * 0.26f)
    val crestL = shoulderL + slopeRun
    val crestR = shoulderR - slopeRun
    val bevel = (ch * 0.45f).coerceIn(2f, 12f)

    return Path().apply {
        moveTo(0f, ch + r)
        quadraticTo(0f, ch, r, ch)
        lineTo(shoulderL - bevel, ch)
        quadraticTo(shoulderL, ch, shoulderL + bevel * 0.65f, ch - bevel * 0.65f)
        lineTo(crestL - bevel * 0.65f, bevel * 0.65f)
        quadraticTo(crestL, 0f, crestL + bevel, 0f)
        lineTo(crestR - bevel, 0f)
        quadraticTo(crestR, 0f, crestR + bevel * 0.65f, bevel * 0.65f)
        lineTo(shoulderR - bevel * 0.65f, ch - bevel * 0.65f)
        quadraticTo(shoulderR, ch, shoulderR + bevel, ch)
        lineTo(w - r, ch)
        quadraticTo(w, ch, w, ch + r)
        lineTo(w, h - r)
        quadraticTo(w, h, w - r, h)
        lineTo(r, h)
        quadraticTo(0f, h, 0f, h - r)
        close()
    }
}

@Composable
fun FuturisticDockContainer(
    surfaceColor: Color,
    glassAlpha: Float,
    accentColor: Color,
    crestHeight: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val crestPx = with(density) { crestHeight.toPx() }
    val cornerPx = with(density) { cornerRadius.toPx() }
    val clipShape = remember(crestPx, cornerPx) {
        GenericShape { size, _ ->
            addPath(buildFuturisticDockPath(size, crestPx, cornerPx))
        }
    }
    val effectiveAlpha = glassAlpha.coerceIn(0f, 0.95f)

    Box(
        modifier = modifier
            .clip(clipShape)
            .drawWithContent {
                val path = buildFuturisticDockPath(size, crestPx, cornerPx)
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor.copy(alpha = (effectiveAlpha + 0.12f).coerceIn(0.14f, 0.92f)),
                            surfaceColor.copy(alpha = effectiveAlpha.coerceIn(0.08f, 0.88f)),
                        )
                    )
                )
                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.20f * effectiveAlpha.coerceAtLeast(0.4f)),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = (size.width * 0.45f).coerceAtLeast(1f),
                    )
                )
                drawContent()
                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = 0.32f),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PureWhite.copy(alpha = 0.88f),
                            accentColor.copy(alpha = 0.95f),
                            accentColor.copy(alpha = 0.72f),
                        )
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
    ) {
        content()
    }
}

fun buildCurvedSettingsDrawerPath(
    size: Size,
    headerHeightPx: Float,
    neckInsetPx: Float,
    midBowInsetPx: Float,
    bottomInsetPx: Float,
): Path {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return Path()
    val hh = headerHeightPx.coerceIn(24f, h * 0.35f)
    val neckX = neckInsetPx.coerceIn(8f, w * 0.35f)
    val midX = midBowInsetPx.coerceIn(neckX, w * 0.48f)
    val bottomX = bottomInsetPx.coerceIn(4f, w * 0.35f)
    // Quadratic Bezier midpoint x(0.5) = 0.25*neckX + 0.5*cx + 0.25*bottomX
    val cx = ((midX - 0.25f * neckX - 0.25f * bottomX) / 0.5f).coerceIn(neckX, w * 0.65f)
    val cy = hh + (h - hh) * 0.50f

    return Path().apply {
        moveTo(w, 0f)
        lineTo(0f, 0f)
        quadraticTo(neckX * 0.55f, hh * 0.42f, neckX, hh)
        quadraticTo(cx, cy, bottomX, h)
        lineTo(w, h)
        close()
    }
}

@Composable
fun CurvedSettingsDrawerContainer(
    headerHeight: Dp,
    neckInset: Dp,
    midBowInset: Dp,
    bottomInset: Dp,
    surfaceColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val hhPx = with(density) { headerHeight.toPx() }
    val neckPx = with(density) { neckInset.toPx() }
    val midPx = with(density) { midBowInset.toPx() }
    val bottomPx = with(density) { bottomInset.toPx() }
    val clipShape = remember(hhPx, neckPx, midPx, bottomPx) {
        GenericShape { size, _ ->
            addPath(buildCurvedSettingsDrawerPath(size, hhPx, neckPx, midPx, bottomPx))
        }
    }

    Box(
        modifier = modifier
            .clip(clipShape)
            .drawWithContent {
                val path = buildCurvedSettingsDrawerPath(size, hhPx, neckPx, midPx, bottomPx)
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor.copy(alpha = 0.90f),
                            DeepSpaceBlack.copy(alpha = 0.88f),
                        )
                    )
                )
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                        startX = 0f,
                        endX = size.width * 0.55f,
                    )
                )
                drawContent()
                // Bright horizontal neon divider under top header flare
                val divY = hhPx.coerceIn(24f, size.height * 0.35f)
                val divStartX = neckPx.coerceIn(8f, size.width * 0.35f)
                drawLine(
                    color = accentColor.copy(alpha = 0.40f),
                    start = Offset(divStartX, divY),
                    end = Offset(size.width, divY),
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PureWhite.copy(alpha = 0.92f),
                            accentColor,
                            accentColor.copy(alpha = 0.65f),
                        ),
                        startX = divStartX,
                        endX = size.width,
                    ),
                    start = Offset(divStartX, divY),
                    end = Offset(size.width, divY),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                // Glowing perimeter rim
                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = 0.36f),
                    style = Stroke(width = 5.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PureWhite.copy(alpha = 0.90f),
                            accentColor,
                            accentColor.copy(alpha = 0.78f),
                        )
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
    ) {
        content()
    }
}

fun weatherIconForCode(code: Int, isDay: Boolean): ImageVector = when (code) {
    0 -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay
    1, 2 -> Icons.Rounded.FilterDrama
    3, 45, 48 -> Icons.Rounded.Cloud
    51, 53, 55 -> Icons.Rounded.Grain
    61, 63, 65, 80, 81, 82 -> Icons.Rounded.WaterDrop
    71, 73, 75 -> Icons.Rounded.AcUnit
    95, 96, 99 -> Icons.Rounded.Thunderstorm
    else -> Icons.Rounded.Cloud
}

internal fun drawableToImageBitmap(drawable: Drawable?): androidx.compose.ui.graphics.ImageBitmap? {
    if (drawable == null) return null
    return runCatching {
        val rawW = drawable.intrinsicWidth
        val rawH = drawable.intrinsicHeight
        val w = (if (rawW > 0) rawW else 96).coerceIn(64, 256)
        val h = (if (rawH > 0) rawH else 96).coerceIn(64, 256)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp.asImageBitmap()
    }.getOrNull()
}
