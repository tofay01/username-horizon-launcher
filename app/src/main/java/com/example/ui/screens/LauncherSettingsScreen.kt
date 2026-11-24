package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import com.example.ui.icons.Gesture
import com.example.ui.icons.LocationCity
import com.example.ui.icons.Mosque
import com.example.ui.icons.Palette
import com.example.ui.icons.Public
import com.example.ui.icons.RestartAlt
import com.example.ui.icons.Schedule
import com.example.ui.icons.SystemUpdate
import com.example.ui.icons.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.InstalledAppsRepository
import com.example.model.ClockTheme
import com.example.model.LauncherDestination
import com.example.model.LauncherSettings
import com.example.model.LauncherStrings
import com.example.model.PrayerCalculationMethod
import com.example.ui.LauncherUiState
import com.example.ui.components.ClockModeFilter
import com.example.ui.components.ClockThemeGalleryDialog
import com.example.ui.components.ClockThemePreviewThumbnail
import com.example.ui.components.DockSizeStepperCard
import com.example.ui.components.GlassSegmentedSelector
import com.example.ui.components.GlassSettingCardRow
import com.example.ui.components.GlassToggleRow
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite

@Composable
fun LauncherSettingsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onNavigate: (LauncherDestination) -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onFollowThemeReset: () -> Unit,
    onRequestDefaultHome: () -> Unit,
    onCheckUpdates: () -> Unit,
    onOpenRepoSetup: () -> Unit = {},
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor

    var isDefaultHome by remember {
        mutableStateOf(InstalledAppsRepository.isDefaultHomeLauncher(context))
    }
    var showTimezoneDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showClockThemeDialog by remember { mutableStateOf(false) }
    var clockModeFilter by remember { mutableStateOf(ClockModeFilter.ALL) }
    val firstControlFocusRequester = remember { FocusRequester() }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultHome = InstalledAppsRepository.isDefaultHomeLauncher(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        runCatching { firstControlFocusRequester.requestFocus() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xEE081B44), Color(0xF5040D22))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TvFocusableSurface(
                    onClick = onBack,
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.size(48.dp).testTag("launcher_settings_back"),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.backLabel,
                            tint = PureWhite,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = PureWhite,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Promoted Default Home Launcher control at top of App Settings with live ON_RESUME badge
            GlassSettingCardRow(
                title = strings.aboutSetDefaultHomeTitle,
                subtitle = strings.aboutSetDefaultHomeDesc,
                trailingText = if (isDefaultHome) strings.aboutDefaultHomeActive else strings.aboutSetDefaultHomeAction,
                leadingIcon = Icons.Rounded.Home,
                accentColor = if (isDefaultHome) EmeraldGlow else accent,
                isHighlighted = !isDefaultHome,
                onClick = {
                    onRequestDefaultHome()
                    isDefaultHome = InstalledAppsRepository.isDefaultHomeLauncher(context)
                },
                testTag = "action_default_home",
                focusRequester = firstControlFocusRequester,
            )

            // Applications par ligne: 4, 5, 6, 7
            Text(
                text = strings.appsPerRow,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
                modifier = Modifier.padding(top = 4.dp),
            )
            GlassSegmentedSelector(
                options = listOf(4, 5, 6, 7),
                selected = settings.appsPerRow,
                labelProvider = { it.toString() },
                accentColor = accent,
                onSelect = { count -> onUpdateSettings { it.copy(appsPerRow = count) } },
                testTagPrefix = "apps_per_row",
            )

            // Lignes sous les favoris: 0, 1, 2, 3
            Text(
                text = strings.rowsBelowFavorites,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
                modifier = Modifier.padding(top = 6.dp),
            )
            GlassSegmentedSelector(
                options = listOf(0, 1, 2, 3),
                selected = settings.rowsBelowFavorites,
                labelProvider = { it.toString() },
                accentColor = accent,
                onSelect = { rows -> onUpdateSettings { it.copy(rowsBelowFavorites = rows) } },
                testTagPrefix = "rows_below",
            )

            // Taille / zoom du شريط المفضلة (60%..130%)
            DockSizeStepperCard(
                dockScalePercent = settings.dockScalePercent,
                title = strings.dockSizeTitle,
                hint = strings.dockSizeHint,
                accentColor = accent,
                onChangeScale = { scale ->
                    onUpdateSettings { it.copy(dockScalePercent = scale.coerceIn(60, 130)) }
                },
            )

            // Contraste renforcé
            GlassToggleRow(
                title = strings.enhancedContrast,
                subtitle = strings.enhancedContrastDesc,
                checked = settings.enhancedContrast,
                accentColor = accent,
                onCheckedChange = { checked -> onUpdateSettings { it.copy(enhancedContrast = checked) } },
                testTag = "toggle_enhanced_contrast",
            )

            // Horloge et date
            Text(
                text = strings.clockAndDateSection,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                modifier = Modifier.padding(top = 8.dp),
            )

            TvFocusableSurface(
                onClick = { showClockThemeDialog = true },
                accentColor = accent,
                shape = RoundedCornerShape(16.dp),
                baseColor = Color(settings.themePreset.surfaceHex).copy(alpha = 0.52f),
                baseBorderColor = accent.copy(alpha = 0.45f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_clock_theme_picker"),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    ClockThemePreviewThumbnail(
                        theme = settings.clockTheme,
                        accent = accent,
                        formattedTime = state.formattedTime,
                        modifier = Modifier.size(width = 84.dp, height = 52.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.clockThemeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = strings.clockThemeSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = settings.clockTheme.displayLabel(settings.language),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = accent,
                        maxLines = 1,
                    )
                }
            }

            GlassSegmentedSelector(
                options = ClockModeFilter.entries,
                selected = clockModeFilter,
                labelProvider = { filter ->
                    when (filter) {
                        ClockModeFilter.ALL -> strings.clockFilterAll
                        ClockModeFilter.DIGITAL -> strings.clockFilterDigital
                        ClockModeFilter.ANALOG -> strings.clockFilterAnalog
                    }
                },
                accentColor = accent,
                onSelect = { clockModeFilter = it },
                testTagPrefix = "clock_mode_filter",
            )

            val visibleQuickThemes = remember(clockModeFilter) {
                when (clockModeFilter) {
                    ClockModeFilter.ALL -> ClockTheme.entries
                    ClockModeFilter.DIGITAL -> ClockTheme.entries.filter { !it.isAnalog }
                    ClockModeFilter.ANALOG -> ClockTheme.entries.filter { it.isAnalog }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clock_theme_quick_strip"),
            ) {
                items(visibleQuickThemes, key = { it.id }) { clockTheme ->
                    val selected = clockTheme == settings.clockTheme
                    TvFocusableSurface(
                        onClick = { onUpdateSettings { it.copy(clockTheme = clockTheme) } },
                        accentColor = accent,
                        shape = RoundedCornerShape(14.dp),
                        baseColor = Color(settings.themePreset.surfaceHex).copy(alpha = 0.45f),
                        isSelected = selected,
                        modifier = Modifier
                            .width(102.dp)
                            .testTag("clock_theme_quick_${clockTheme.id}"),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                        ) {
                            ClockThemePreviewThumbnail(
                                theme = clockTheme,
                                accent = accent,
                                formattedTime = state.formattedTime,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = clockTheme.displayLabel(settings.language),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) accent else PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            GlassSettingCardRow(
                title = "${strings.timeFormat}: ${if (settings.is24HourFormat) strings.timeFormat24 else strings.timeFormat12}",
                leadingIcon = Icons.Rounded.Schedule,
                accentColor = accent,
                onClick = { onUpdateSettings { it.copy(is24HourFormat = !it.is24HourFormat) } },
                testTag = "toggle_time_format",
            )

            GlassToggleRow(
                title = strings.showDate,
                checked = settings.showDate,
                accentColor = accent,
                onCheckedChange = { checked -> onUpdateSettings { it.copy(showDate = checked) } },
                testTag = "toggle_show_date",
            )

            GlassToggleRow(
                title = strings.showGreeting,
                checked = settings.showGreeting,
                accentColor = accent,
                onCheckedChange = { checked -> onUpdateSettings { it.copy(showGreeting = checked) } },
                testTag = "toggle_show_greeting",
            )

            // Personnalisation Thème & Fond d'écran + Gestes
            val activeThemeLabel = when (settings.language) {
                com.example.model.LauncherLanguage.AR -> settings.themePreset.labelAr
                com.example.model.LauncherLanguage.EN -> settings.themePreset.labelEn
                com.example.model.LauncherLanguage.FR -> settings.themePreset.labelFr
            }
            val activeWallpaperTag = when {
                settings.customWallpaperUri != null && settings.customWallpaperIsVideo -> when (settings.language) {
                    com.example.model.LauncherLanguage.AR -> "فيديو"
                    com.example.model.LauncherLanguage.FR -> "Vidéo"
                    com.example.model.LauncherLanguage.EN -> "Video"
                }
                settings.customWallpaperUri != null -> when (settings.language) {
                    com.example.model.LauncherLanguage.AR -> "صورة"
                    com.example.model.LauncherLanguage.FR -> "Photo"
                    com.example.model.LauncherLanguage.EN -> "Photo"
                }
                com.example.ui.components.isBuiltInVideoWallpaper(settings.wallpaperId) -> when (settings.language) {
                    com.example.model.LauncherLanguage.AR -> "فيديو"
                    com.example.model.LauncherLanguage.FR -> "Vidéo"
                    com.example.model.LauncherLanguage.EN -> "Video"
                }
                else -> null
            }
            GlassSettingCardRow(
                title = strings.themeAndWallpaperTitle,
                subtitle = if (activeWallpaperTag != null) "$activeThemeLabel · $activeWallpaperTag" else activeThemeLabel,
                leadingIcon = Icons.Rounded.Palette,
                accentColor = accent,
                onClick = { onNavigate(LauncherDestination.THEME_AND_WALLPAPER) },
                testTag = "open_theme_and_wallpaper",
            )

            GlassSettingCardRow(
                title = strings.gestureShortcuts,
                leadingIcon = Icons.Rounded.Gesture,
                accentColor = accent,
                onClick = { onNavigate(LauncherDestination.GESTURE_SHORTCUTS) },
                testTag = "open_gesture_shortcuts",
            )

            // Prayer & Weather GPS section
            GlassSettingCardRow(
                title = "${strings.prayerCity} : ${settings.cityName}",
                leadingIcon = Icons.Rounded.LocationCity,
                accentColor = accent,
                onClick = { onNavigate(LauncherDestination.PRAYER_AND_ADHAN) },
                testTag = "open_prayer_city",
            )

            GlassSettingCardRow(
                title = "${strings.prayerTimezone} : ${settings.timezoneId}",
                leadingIcon = Icons.Rounded.Public,
                accentColor = accent,
                onClick = { showTimezoneDialog = true },
                testTag = "open_timezone_picker",
            )

            GlassSettingCardRow(
                title = "${strings.calculationMethod} : ${PrayerCalculationMethod.byId(settings.prayerMethodId).name}",
                leadingIcon = Icons.Rounded.Mosque,
                accentColor = accent,
                onClick = { showMethodDialog = true },
                testTag = "open_method_picker",
            )

            GlassSettingCardRow(
                title = strings.adhan,
                subtitle = settings.customAdhanAudioName ?: strings.selectCustomAdhan,
                leadingIcon = Icons.Rounded.Mosque,
                trailingText = if (settings.adhanEnabled) strings.enabledLabel else strings.disabledLabel,
                accentColor = accent,
                onClick = { onNavigate(LauncherDestination.PRAYER_AND_ADHAN) },
                testTag = "open_adhan_settings",
            )

            GlassSettingCardRow(
                title = strings.weatherAndGpsTitle,
                leadingIcon = Icons.Rounded.WbSunny,
                trailingText = settings.shortCityName,
                accentColor = accent,
                onClick = { onNavigate(LauncherDestination.WEATHER_DETAIL) },
                testTag = "open_weather_settings",
            )

            // Home Widgets: Adhan vs Daily Calendar + Weather
            val currentLeftMode = when {
                settings.showPrayerWidget && settings.showCalendarWidget -> "both"
                !settings.showPrayerWidget && settings.showCalendarWidget -> "calendar"
                settings.showPrayerWidget && !settings.showCalendarWidget -> "prayer"
                else -> "hidden"
            }
            Text(
                text = strings.leftWidgetModeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
                modifier = Modifier.padding(top = 6.dp),
            )
            GlassSegmentedSelector(
                options = listOf("prayer", "calendar", "both", "hidden"),
                selected = currentLeftMode,
                labelProvider = { mode ->
                    when (mode) {
                        "prayer" -> strings.leftWidgetModeAdhan
                        "calendar" -> strings.leftWidgetModeCalendar
                        "both" -> strings.leftWidgetModeBoth
                        else -> strings.leftWidgetModeHidden
                    }
                },
                accentColor = accent,
                onSelect = { mode ->
                    onUpdateSettings { s ->
                        when (mode) {
                            "prayer" -> s.copy(showPrayerWidget = true, showCalendarWidget = false)
                            "calendar" -> s.copy(showPrayerWidget = false, showCalendarWidget = true)
                            "both" -> s.copy(showPrayerWidget = true, showCalendarWidget = true)
                            else -> s.copy(showPrayerWidget = false, showCalendarWidget = false)
                        }
                    }
                },
                testTagPrefix = "settings_left_widget_mode",
            )
            GlassToggleRow(
                title = strings.showWeatherCardToggle,
                checked = settings.showWeatherWidget,
                accentColor = accent,
                onCheckedChange = { checked -> onUpdateSettings { it.copy(showWeatherWidget = checked) } },
                testTag = "settings_toggle_weather_widget",
            )

            // Animations & Advanced effects
            GlassSettingCardRow(
                title = "${strings.animations} : ${if (settings.animationsEnabled) strings.enabledLabel else strings.disabledLabel}",
                accentColor = accent,
                isHighlighted = settings.animationsEnabled,
                onClick = { onUpdateSettings { it.copy(animationsEnabled = !it.animationsEnabled) } },
                testTag = "toggle_animations",
            )

            GlassSettingCardRow(
                title = "${strings.advancedEffects} : ${if (settings.advancedEffectsEnabled) strings.enabledLabel else strings.disabledLabel}",
                subtitle = strings.advancedEffectsDesc,
                accentColor = accent,
                isHighlighted = settings.advancedEffectsEnabled,
                onClick = { onUpdateSettings { it.copy(advancedEffectsEnabled = !it.advancedEffectsEnabled) } },
                testTag = "toggle_advanced_effects",
            )

            GlassSettingCardRow(
                title = strings.followThemeReset,
                subtitle = strings.followThemeResetDesc,
                leadingIcon = Icons.Rounded.RestartAlt,
                accentColor = accent,
                onClick = onFollowThemeReset,
                testTag = "action_follow_theme_reset",
            )


            GlassToggleRow(
                title = strings.showHdmiBar,
                checked = settings.showHdmiSourcesBar,
                accentColor = accent,
                onCheckedChange = { checked -> onUpdateSettings { it.copy(showHdmiSourcesBar = checked) } },
                testTag = "toggle_hdmi_bar",
            )

            val isCheckingUpdate = state.updateState is com.example.update.AppUpdateUiState.Checking
            GlassSettingCardRow(
                title = strings.githubRepoTitle,
                subtitle = settings.githubUpdateRepo.ifBlank { strings.githubRepoNotConfigured },
                trailingText = if (settings.githubUpdateRepo.isNotBlank()) settings.githubUpdateRepo else null,
                leadingIcon = Icons.Rounded.Public,
                accentColor = accent,
                isHighlighted = settings.githubUpdateRepo.isBlank(),
                onClick = onOpenRepoSetup,
                testTag = "github_repo_card",
            )

            GlassToggleRow(
                title = strings.autoCheckUpdatesTitle,
                subtitle = strings.autoCheckUpdatesDesc,
                checked = settings.autoCheckUpdatesOnStartup,
                accentColor = accent,
                onCheckedChange = { enabled -> onUpdateSettings { it.copy(autoCheckUpdatesOnStartup = enabled) } },
                testTag = "toggle_auto_check_updates",
            )

            GlassSettingCardRow(
                title = strings.checkForUpdates,
                subtitle = if (isCheckingUpdate) strings.updateCheckingLabel else settings.githubUpdateRepo.takeIf { it.isNotBlank() },
                leadingIcon = Icons.Rounded.SystemUpdate,
                trailingText = if (isCheckingUpdate) "..." else "v${com.example.BuildConfig.VERSION_NAME}",
                accentColor = accent,
                onClick = onCheckUpdates,
                testTag = "action_check_updates",
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showTimezoneDialog) {
        val timezones = listOf(
            "Europe/Paris",
            "Europe/Brussels",
            "Europe/London",
            "Africa/Algiers",
            "Africa/Casablanca",
            "Africa/Tunis",
            "Africa/Cairo",
            "Asia/Riyadh",
            "Asia/Dubai",
            "America/New_York",
            "America/Montreal",
        )
        AlertDialog(
            onDismissRequest = { showTimezoneDialog = false },
            containerColor = Color(0xFF0C1E44),
            titleContentColor = PureWhite,
            title = { Text(strings.prayerTimezone) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    timezones.forEach { tz ->
                        val selected = tz == settings.timezoneId
                        TvFocusableSurface(
                            onClick = {
                                onUpdateSettings { it.copy(timezoneId = tz) }
                                showTimezoneDialog = false
                            },
                            accentColor = accent,
                            shape = RoundedCornerShape(10.dp),
                            baseColor = Color.White.copy(alpha = 0.08f),
                            isSelected = selected,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = tz,
                                color = PureWhite,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimezoneDialog = false }) {
                    Text(strings.backLabel, color = accent)
                }
            },
        )
    }

    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            containerColor = Color(0xFF0C1E44),
            titleContentColor = PureWhite,
            title = { Text(strings.calculationMethod) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    PrayerCalculationMethod.ALL_METHODS.forEach { method ->
                        val selected = method.id == settings.prayerMethodId
                        TvFocusableSurface(
                            onClick = {
                                onUpdateSettings { it.copy(prayerMethodId = method.id) }
                                showMethodDialog = false
                            },
                            accentColor = accent,
                            shape = RoundedCornerShape(10.dp),
                            baseColor = Color.White.copy(alpha = 0.08f),
                            isSelected = selected,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = method.name,
                                color = if (selected) PureWhite else MutedSlate,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text(strings.backLabel, color = accent)
                }
            },
        )
    }

    if (showClockThemeDialog) {
        ClockThemeGalleryDialog(
            selectedTheme = settings.clockTheme,
            language = settings.language,
            strings = strings,
            accent = accent,
            sampleTime = state.formattedTime,
            onSelectTheme = { picked -> onUpdateSettings { it.copy(clockTheme = picked) } },
            onDismiss = { showClockThemeDialog = false },
        )
    }
}
