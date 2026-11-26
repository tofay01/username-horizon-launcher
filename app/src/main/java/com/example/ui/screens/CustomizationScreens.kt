package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import com.example.ui.icons.AddPhotoAlternate
import com.example.ui.icons.FolderOpen
import com.example.ui.icons.Gesture
import com.example.ui.icons.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppIconShape
import com.example.model.AppIconSize
import com.example.model.DockStyle
import com.example.model.GestureAction
import com.example.model.GestureType
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.LauncherStrings
import com.example.model.ThemeCategory
import com.example.model.ThemePreset
import com.example.ui.LauncherUiState
import com.example.ui.components.GlassSegmentedSelector
import com.example.ui.components.GlassSettingCardRow
import com.example.ui.components.GlassToggleRow
import com.example.ui.components.TvFocusableSurface
import com.example.ui.components.wallpaperDrawableRes
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import kotlin.math.abs
import kotlin.math.roundToInt

private data class BuiltInWallpaper(
    val id: String,
    val title: String,
    val drawableRes: Int?,
    val isVideo: Boolean = false,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeAndWallpaperScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onApplyThemePreset: (ThemePreset) -> Unit,
    onSelectCustomWallpaper: (Uri, Boolean?) -> Unit,
) {
    BackHandler { onBack() }

    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onSelectCustomWallpaper(uri, false)
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onSelectCustomWallpaper(uri, true)
            }
        }
    )

    val docImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onSelectCustomWallpaper(uri, false)
            }
        }
    )

    val docVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onSelectCustomWallpaper(uri, true)
            }
        }
    )

    val browseAllMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onSelectCustomWallpaper(uri, null)
            }
        }
    )

    val videoWallpapers = remember(settings.language) {
        listOf(
            BuiltInWallpaper(
                id = "video_earth_space",
                title = when (settings.language) {
                    LauncherLanguage.AR -> "الأرض من الفضاء"
                    LauncherLanguage.FR -> "Terre depuis l'espace"
                    LauncherLanguage.EN -> "Earth from Space"
                },
                drawableRes = R.drawable.thumb_video_earth_space,
                isVideo = true,
            ),
            BuiltInWallpaper(
                id = "video_emerald_nebula",
                title = when (settings.language) {
                    LauncherLanguage.AR -> "سديم زمردي متحرك"
                    LauncherLanguage.FR -> "Nébuleuse Émeraude"
                    LauncherLanguage.EN -> "Emerald Nebula"
                },
                drawableRes = R.drawable.thumb_video_emerald_nebula,
                isVideo = true,
            ),
            BuiltInWallpaper(
                id = "video_crimson_nebula",
                title = when (settings.language) {
                    LauncherLanguage.AR -> "سديم قرمزي متحرك"
                    LauncherLanguage.FR -> "Nébuleuse Rubis"
                    LauncherLanguage.EN -> "Ruby Nebula"
                },
                drawableRes = R.drawable.thumb_video_crimson_nebula,
                isVideo = true,
            ),
            BuiltInWallpaper(
                id = "video_cobalt_fractal",
                title = when (settings.language) {
                    LauncherLanguage.AR -> "موجات كوبالت متوهجة"
                    LauncherLanguage.FR -> "Ondes Fractales Cobalt"
                    LauncherLanguage.EN -> "Cobalt Light Waves"
                },
                drawableRes = R.drawable.thumb_video_cobalt_fractal,
                isVideo = true,
            ),
            BuiltInWallpaper(
                id = "video_violet_nebula",
                title = when (settings.language) {
                    LauncherLanguage.AR -> "فضاء بنفسجي متألق"
                    LauncherLanguage.FR -> "Cosmos Violet"
                    LauncherLanguage.EN -> "Violet Cosmos"
                },
                drawableRes = R.drawable.thumb_video_violet_nebula,
                isVideo = true,
            ),
        )
    }

    // Only wallpapers that are NOT already represented by a ThemePreset card above (no duplicates)
    val exclusiveStaticWallpapers = remember(settings.language) {
        listOf(
            BuiltInWallpaper("earth", "Terre Nocturne", R.drawable.img_wallpaper_earth),
            BuiltInWallpaper("aurora", "Aurore Boréale", R.drawable.img_wallpaper_aurora),
            BuiltInWallpaper("amber_night", "Dunes Crépuscule", R.drawable.img_wallpaper_amber_night),
            BuiltInWallpaper("cobalt_waves", "Ondes Cobalt", R.drawable.img_wallpaper_cobalt_waves),
            BuiltInWallpaper("gradient_dark", "Dégradé Thème", null),
            BuiltInWallpaper(
                id = "sport_algeria_fennecs",
                title = when (settings.language) {
                    LauncherLanguage.AR -> "الجزائر · محاربو الصحراء"
                    LauncherLanguage.FR -> "Algérie · Fennecs"
                    LauncherLanguage.EN -> "Algeria · Fennecs"
                },
                drawableRes = R.drawable.img_sport_algeria_fennecs,
            ),
            BuiltInWallpaper(
                id = "sport_brazil_light",
                title = when (settings.language) {
                    LauncherLanguage.AR -> "البرازيل · نسخة فاتحة"
                    LauncherLanguage.FR -> "Brésil · Édition Claire"
                    LauncherLanguage.EN -> "Brazil · Light Edition"
                },
                drawableRes = R.drawable.img_sport_brazil_light,
            ),
        )
    }

    val accentSwatches = listOf(
        0xFF38BDF8L,
        0xFF00E5FFL,
        0xFF34D399L,
        0xFFFBBF24L,
        0xFFA855F7L,
        0xFFF43F5EL,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xEE081B44), Color(0xF6040D22))))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val backFocus = remember { FocusRequester() }
            LaunchedEffect(Unit) { runCatching { backFocus.requestFocus() } }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TvFocusableSurface(
                    onClick = onBack,
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    focusRequester = backFocus,
                    modifier = Modifier.size(48.dp).testTag("ui_customizer_back_btn"),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backLabel, tint = PureWhite)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(strings.themeAndWallpaperTitle, style = MaterialTheme.typography.headlineLarge, color = PureWhite)
            }

            // 1. Categorized Theme Presets (Classic + Sports Clubs)
            val themeGroups = remember(strings) {
                listOf(
                    strings.themeCategoryClassic to ThemePreset.entries.filter { it.category == ThemeCategory.CLASSIC },
                    strings.themeCategoryClubs to ThemePreset.entries.filter { it.category == ThemeCategory.SPORTS_CLUBS },
                )
            }
            themeGroups.forEach { (categoryTitle, presets) ->
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                    modifier = Modifier.padding(top = 6.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    presets.forEach { preset ->
                        val selected = settings.themePreset == preset
                        val label = when (settings.language) {
                            LauncherLanguage.FR -> preset.labelFr
                            LauncherLanguage.AR -> preset.labelAr
                            LauncherLanguage.EN -> preset.labelEn
                        }
                        val previewRes = wallpaperDrawableRes(preset.defaultWallpaperId)
                        TvFocusableSurface(
                            onClick = { onApplyThemePreset(preset) },
                            accentColor = Color(preset.accentHex),
                            shape = RoundedCornerShape(16.dp),
                            baseColor = Color(preset.surfaceHex).copy(alpha = 0.72f),
                            focusedColor = Color(preset.accentHex).copy(alpha = 0.36f),
                            baseBorderColor = Color.White.copy(alpha = 0.24f),
                            isSelected = selected,
                            modifier = Modifier
                                .width(184.dp)
                                .aspectRatio(16f / 9f)
                                .testTag("theme_preset_${preset.id}"),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (previewRes != null) {
                                    Image(
                                        painter = painterResource(id = previewRes),
                                        contentDescription = label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(preset.surfaceHex), DeepSpaceBlack)
                                                )
                                            )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.08f),
                                                    Color.Black.copy(alpha = 0.72f),
                                                )
                                            )
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.BottomStart),
                                    )
                                    if (selected) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(preset.accentHex)),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = DeepSpaceBlack,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Wallpapers, Device Media Pickers, Videos & Background Dimming
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.16f)),
            )
            Text(
                text = strings.wallpapersSectionTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PureWhite,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TvFocusableSurface(
                    onClick = {
                        val launched = runCatching {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }.isSuccess
                        if (!launched) {
                            runCatching { docImageLauncher.launch(arrayOf("image/*")) }
                        }
                    },
                    accentColor = accent,
                    shape = RoundedCornerShape(14.dp),
                    baseColor = accent,
                    focusedColor = PureWhite,
                    modifier = Modifier.weight(1f, fill = false).widthIn(min = 210.dp).height(52.dp).testTag("pick_wallpaper_image_btn"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 18.dp).height(52.dp),
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = DeepSpaceBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.pickWallpaperImage,
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSpaceBlack,
                        )
                    }
                }

                TvFocusableSurface(
                    onClick = {
                        val launched = runCatching {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }.isSuccess
                        if (!launched) {
                            runCatching { docVideoLauncher.launch(arrayOf("video/*")) }
                        }
                    },
                    accentColor = accent,
                    shape = RoundedCornerShape(14.dp),
                    baseColor = GlassBlue.copy(alpha = 0.72f),
                    focusedColor = accent,
                    baseBorderColor = accent.copy(alpha = 0.65f),
                    modifier = Modifier.weight(1f, fill = false).widthIn(min = 210.dp).height(52.dp).testTag("pick_wallpaper_video_btn"),
                ) { isFocused ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 18.dp).height(52.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            tint = if (isFocused) DeepSpaceBlack else accent,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.pickWallpaperVideo,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isFocused) DeepSpaceBlack else PureWhite,
                        )
                    }
                }

                TvFocusableSurface(
                    onClick = {
                        runCatching {
                            browseAllMediaLauncher.launch(arrayOf("image/*", "video/*"))
                        }
                    },
                    accentColor = accent,
                    shape = RoundedCornerShape(14.dp),
                    baseColor = Color.White.copy(alpha = 0.12f),
                    focusedColor = accent,
                    baseBorderColor = Color.White.copy(alpha = 0.28f),
                    modifier = Modifier.weight(1f, fill = false).widthIn(min = 220.dp).height(52.dp).testTag("browse_wallpaper_files_btn"),
                ) { isFocused ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 18.dp).height(52.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = if (isFocused) DeepSpaceBlack else PureWhite,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.browseWallpaperFiles,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isFocused) DeepSpaceBlack else PureWhite,
                        )
                    }
                }
            }

            if (settings.customWallpaperUri != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldGlow.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth().testTag("custom_wallpaper_active_banner"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = if (settings.customWallpaperIsVideo) Icons.Rounded.VideoLibrary else Icons.Rounded.AddPhotoAlternate,
                                contentDescription = null,
                                tint = EmeraldGlow,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (settings.customWallpaperIsVideo) strings.customVideoActive else strings.customImageActive,
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TvFocusableSurface(
                            onClick = {
                                onUpdateSettings {
                                    it.copy(
                                        wallpaperId = "earth",
                                        customWallpaperUri = null,
                                        customWallpaperIsVideo = false,
                                    )
                                }
                            },
                            accentColor = accent,
                            shape = RoundedCornerShape(12.dp),
                            baseColor = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.testTag("clear_custom_wallpaper_btn"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.clearCustomWallpaper,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PureWhite,
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = strings.animatedVideoWallpapersSection,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accent,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                videoWallpapers.forEach { wp ->
                    BuiltInWallpaperCard(
                        wp = wp,
                        selected = settings.customWallpaperUri == null && settings.wallpaperId == wp.id,
                        surfaceHex = settings.themePreset.surfaceHex,
                        accent = accent,
                        onSelect = {
                            onUpdateSettings {
                                it.copy(
                                    wallpaperId = wp.id,
                                    customWallpaperUri = null,
                                    customWallpaperIsVideo = false,
                                )
                            }
                        },
                    )
                }
            }

            Text(
                text = strings.staticWallpapersSection,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accent,
                modifier = Modifier.padding(top = 4.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                exclusiveStaticWallpapers.forEach { wp ->
                    BuiltInWallpaperCard(
                        wp = wp,
                        selected = settings.customWallpaperUri == null && settings.wallpaperId == wp.id,
                        surfaceHex = settings.themePreset.surfaceHex,
                        accent = accent,
                        onSelect = {
                            onUpdateSettings {
                                it.copy(
                                    wallpaperId = wp.id,
                                    customWallpaperUri = null,
                                    customWallpaperIsVideo = false,
                                )
                            }
                        },
                    )
                }
            }

            // Wallpaper Dimming
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GlassBlue.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().testTag("wallpaper_dim_card"),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val dimPercent = (settings.wallpaperDimAlpha * 100f).roundToInt().coerceIn(0, 75)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "${strings.wallpaperDimTitle} ($dimPercent%)",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                                modifier = Modifier.testTag("wallpaper_dim_value"),
                            )
                            Text(
                                text = strings.wallpaperDimHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TvFocusableSurface(
                                onClick = {
                                    onUpdateSettings { current ->
                                        val next = ((current.wallpaperDimAlpha * 100f).roundToInt() - 5)
                                            .coerceIn(0, 75) / 100f
                                        current.copy(wallpaperDimAlpha = next)
                                    }
                                },
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.14f),
                                focusedColor = accent.copy(alpha = 0.55f),
                                modifier = Modifier.size(48.dp).testTag("wallpaper_dim_decrease"),
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
                                onClick = {
                                    onUpdateSettings { current ->
                                        val next = ((current.wallpaperDimAlpha * 100f).roundToInt() + 5)
                                            .coerceIn(0, 75) / 100f
                                        current.copy(wallpaperDimAlpha = next)
                                    }
                                },
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.14f),
                                focusedColor = accent.copy(alpha = 0.55f),
                                modifier = Modifier.size(48.dp).testTag("wallpaper_dim_increase"),
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
                        value = settings.wallpaperDimAlpha.coerceIn(0f, 0.75f),
                        onValueChange = { alpha ->
                            onUpdateSettings { it.copy(wallpaperDimAlpha = alpha.coerceIn(0f, 0.75f)) }
                        },
                        valueRange = 0f..0.75f,
                        colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
                        modifier = Modifier.testTag("wallpaper_dim_slider"),
                    )
                }
            }

            // 3. Colors, Icons & Glass Card Style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.16f)),
            )
            Text(
                text = strings.appearanceSectionTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PureWhite,
            )

            Text(strings.accentColorTitle, style = MaterialTheme.typography.titleMedium, color = accent)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                accentSwatches.forEach { hex ->
                    val selected = settings.accentColorHex == hex
                    TvFocusableSurface(
                        onClick = { onUpdateSettings { it.copy(accentColorHex = hex) } },
                        accentColor = PureWhite,
                        shape = CircleShape,
                        baseColor = Color(hex),
                        focusedColor = Color(hex),
                        isSelected = selected,
                        modifier = Modifier.size(48.dp).testTag("accent_swatch_$hex"),
                    ) {
                        if (selected) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = DeepSpaceBlack)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = settings.customSectionTitle,
                onValueChange = { title -> onUpdateSettings { it.copy(customSectionTitle = title) } },
                label = { Text(strings.mainRowTitleLabel, color = MutedSlate) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                ),
                modifier = Modifier.fillMaxWidth().testTag("custom_section_title_input"),
            )

            Text(strings.dockStyleTitle, style = MaterialTheme.typography.titleMedium, color = accent)
            GlassSegmentedSelector(
                options = DockStyle.entries,
                selected = settings.dockStyle,
                labelProvider = {
                    when (it) {
                        DockStyle.FLOATING_GLASS -> strings.dockStyleFloating
                        DockStyle.FULL_WIDTH -> strings.dockStyleFullWidth
                        DockStyle.MINIMAL_CARDS -> strings.dockStyleMinimal
                    }
                },
                accentColor = accent,
                onSelect = { style -> onUpdateSettings { it.copy(dockStyle = style) } },
                testTagPrefix = "dock_style",
            )

            Text(strings.iconSizeTitle, style = MaterialTheme.typography.titleMedium, color = accent)
            GlassSegmentedSelector(
                options = AppIconSize.entries,
                selected = settings.iconSize,
                labelProvider = {
                    when (it) {
                        AppIconSize.SMALL -> strings.iconSizeSmall
                        AppIconSize.MEDIUM -> strings.iconSizeMedium
                        AppIconSize.LARGE -> strings.iconSizeLarge
                    }
                },
                accentColor = accent,
                onSelect = { size -> onUpdateSettings { it.copy(iconSize = size) } },
                testTagPrefix = "icon_size",
                tagSuffixProvider = { it.id },
            )

            Text(strings.iconShapeTitle, style = MaterialTheme.typography.titleMedium, color = accent)
            GlassSegmentedSelector(
                options = AppIconShape.entries,
                selected = settings.iconShape,
                labelProvider = {
                    when (it) {
                        AppIconShape.CIRCLE -> strings.iconShapeCircle
                        AppIconShape.RECTANGLE -> strings.iconShapeRectangle
                        AppIconShape.SQUARE -> strings.iconShapeSquare
                    }
                },
                accentColor = accent,
                onSelect = { shape -> onUpdateSettings { it.copy(iconShape = shape) } },
                testTagPrefix = "icon_shape",
                tagSuffixProvider = { it.id },
            )

            // Card Transparency & Corner Radius
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GlassBlue.copy(alpha = 0.50f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val opacityPercent = (settings.glassOpacity * 100).roundToInt().coerceIn(0, 95)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "${strings.cardTransparencyTitle} ($opacityPercent%)",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                                modifier = Modifier.testTag("glass_opacity_value"),
                            )
                            Text(
                                text = strings.cardTransparencyHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TvFocusableSurface(
                                onClick = {
                                    onUpdateSettings { current ->
                                        val next = ((current.glassOpacity * 100f).roundToInt() - 10).coerceIn(0, 95) / 100f
                                        current.copy(glassOpacity = next)
                                    }
                                },
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.14f),
                                focusedColor = accent.copy(alpha = 0.55f),
                                modifier = Modifier.size(48.dp).testTag("glass_opacity_decrease"),
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
                                onClick = {
                                    onUpdateSettings { current ->
                                        val next = ((current.glassOpacity * 100f).roundToInt() + 10).coerceIn(0, 95) / 100f
                                        current.copy(glassOpacity = next)
                                    }
                                },
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.14f),
                                focusedColor = accent.copy(alpha = 0.55f),
                                modifier = Modifier.size(48.dp).testTag("glass_opacity_increase"),
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
                        value = settings.glassOpacity.coerceIn(0f, 0.95f),
                        onValueChange = { op -> onUpdateSettings { it.copy(glassOpacity = op.coerceIn(0f, 0.95f)) } },
                        valueRange = 0f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
                        modifier = Modifier.testTag("glass_opacity_slider"),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "${strings.cardCornerRadiusTitle} (${settings.cardCornerRadiusDp} dp)",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            modifier = Modifier.weight(1f),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TvFocusableSurface(
                                onClick = {
                                    onUpdateSettings { current ->
                                        current.copy(cardCornerRadiusDp = (current.cardCornerRadiusDp - 2).coerceIn(6, 30))
                                    }
                                },
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.14f),
                                focusedColor = accent.copy(alpha = 0.55f),
                                modifier = Modifier.size(48.dp).testTag("card_radius_decrease"),
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
                                onClick = {
                                    onUpdateSettings { current ->
                                        current.copy(cardCornerRadiusDp = (current.cardCornerRadiusDp + 2).coerceIn(6, 30))
                                    }
                                },
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.14f),
                                focusedColor = accent.copy(alpha = 0.55f),
                                modifier = Modifier.size(48.dp).testTag("card_radius_increase"),
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
                        value = settings.cardCornerRadiusDp.toFloat(),
                        onValueChange = { r -> onUpdateSettings { it.copy(cardCornerRadiusDp = r.roundToInt().coerceIn(6, 30)) } },
                        valueRange = 6f..30f,
                        colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
                    )
                }
            }

            // Navigation Focus Sound Toggle
            GlassToggleRow(
                title = strings.navigationSoundTitle,
                subtitle = strings.navigationSoundDesc,
                checked = settings.navigationSoundEnabled,
                accentColor = accent,
                onCheckedChange = { v -> onUpdateSettings { it.copy(navigationSoundEnabled = v) } },
                testTag = "toggle_navigation_sound",
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BuiltInWallpaperCard(
    wp: BuiltInWallpaper,
    selected: Boolean,
    surfaceHex: Long,
    accent: Color,
    onSelect: () -> Unit,
) {
    TvFocusableSurface(
        onClick = onSelect,
        accentColor = accent,
        shape = RoundedCornerShape(18.dp),
        baseColor = Color.Transparent,
        isSelected = selected,
        modifier = Modifier
            .width(240.dp)
            .aspectRatio(16f / 9f)
            .testTag("wallpaper_card_${wp.id}"),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (wp.drawableRes != null) {
                Image(
                    painter = painterResource(id = wp.drawableRes),
                    contentDescription = wp.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(Color(surfaceHex), DeepSpaceBlack)
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.45f),
                            )
                        )
                    )
                    .padding(12.dp),
            ) {
                if (wp.isVideo) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.62f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.72f)),
                        modifier = Modifier.align(Alignment.TopStart),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "VIDEO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite,
                            )
                        }
                    }
                }
                Text(
                    text = wp.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = accent,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
        }
    }
}

@Composable
fun GestureShortcutsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onUpdateGesture: (GestureType, GestureAction) -> Unit,
    onTestGesture: (GestureType) -> Unit,
) {
    BackHandler { onBack() }

    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor
    var editingGesture by remember { mutableStateOf<GestureType?>(null) }
    var lastDetectedLabel by remember { mutableStateOf("Glissez ou double-tapez ici pour tester") }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xEE081B44), Color(0xF6040D22))))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 880.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val gestureBackFocus = remember { FocusRequester() }
            LaunchedEffect(Unit) { runCatching { gestureBackFocus.requestFocus() } }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TvFocusableSurface(
                    onClick = onBack,
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    focusRequester = gestureBackFocus,
                    modifier = Modifier.size(48.dp).testTag("gestures_back_btn"),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backLabel, tint = PureWhite)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(strings.gestureShortcuts, style = MaterialTheme.typography.headlineLarge, color = PureWhite)
            }

            // Interactive Gesture Test Pad
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = accent.copy(alpha = 0.16f),
                border = BorderStroke(1.5.dp, accent.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                lastDetectedLabel = LauncherStrings.gestureLabel(GestureType.DOUBLE_TAP, settings.language)
                            },
                            onLongPress = {
                                lastDetectedLabel = LauncherStrings.gestureLabel(GestureType.LONG_PRESS, settings.language)
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragX = 0f; dragY = 0f },
                            onDrag = { change, amount ->
                                change.consume()
                                dragX += amount.x
                                dragY += amount.y
                            },
                            onDragEnd = {
                                val g = if (abs(dragX) > abs(dragY)) {
                                    if (dragX > 60f) GestureType.SWIPE_RIGHT else if (dragX < -60f) GestureType.SWIPE_LEFT else null
                                } else {
                                    if (dragY > 60f) GestureType.SWIPE_DOWN else if (dragY < -60f) GestureType.SWIPE_UP else null
                                }
                                if (g != null) {
                                    lastDetectedLabel = LauncherStrings.gestureLabel(g, settings.language)
                                }
                            },
                        )
                    }
                    .testTag("gesture_test_pad"),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                ) {
                    Icon(Icons.Rounded.Gesture, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = lastDetectedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                    )
                }
            }

            GestureType.entries.forEach { gesture ->
                val action = settings.gestureShortcuts[gesture] ?: GestureAction.NONE
                GlassSettingCardRow(
                    title = LauncherStrings.gestureLabel(gesture, settings.language),
                    subtitle = LauncherStrings.gestureActionLabel(action, settings.language),
                    trailingText = "Modifier",
                    leadingIcon = Icons.Rounded.Gesture,
                    accentColor = accent,
                    onClick = { editingGesture = gesture },
                    testTag = "gesture_row_${gesture.key}",
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    val targetGesture = editingGesture
    if (targetGesture != null) {
        AlertDialog(
            onDismissRequest = { editingGesture = null },
            containerColor = Color(0xFF0C1E44),
            titleContentColor = PureWhite,
            title = { Text(LauncherStrings.gestureLabel(targetGesture, settings.language)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    GestureAction.entries.forEach { action ->
                        val selected = settings.gestureShortcuts[targetGesture] == action
                        TvFocusableSurface(
                            onClick = {
                                onUpdateGesture(targetGesture, action)
                                editingGesture = null
                            },
                            accentColor = accent,
                            shape = RoundedCornerShape(10.dp),
                            baseColor = Color.White.copy(alpha = 0.08f),
                            isSelected = selected,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = LauncherStrings.gestureActionLabel(action, settings.language),
                                color = PureWhite,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        editingGesture = null
                        onTestGesture(targetGesture)
                    }) {
                        Text("Exécuter", color = accent)
                    }
                    TextButton(onClick = { editingGesture = null }) {
                        Text(strings.backLabel, color = MutedSlate)
                    }
                }
            },
        )
    }
}
