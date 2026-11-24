package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import com.example.ui.icons.Apps
import com.example.ui.icons.AutoAwesome
import com.example.ui.icons.DeleteOutline
import com.example.ui.icons.GraphicEq
import com.example.ui.icons.Input
import com.example.ui.icons.OpenInNew
import com.example.ui.icons.PowerSettingsNew
import com.example.ui.icons.SettingsEthernet
import com.example.ui.icons.SignalCellularAlt
import com.example.ui.icons.StarBorder
import com.example.ui.icons.VisibilityOff
import com.example.ui.icons.Wifi
import com.example.ui.icons.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.model.AiToolPreset
import com.example.model.AppIconSize
import com.example.model.AppItem
import com.example.model.DockStyle
import com.example.model.GestureType
import com.example.model.LauncherDestination
import com.example.model.LauncherLanguage
import com.example.model.LauncherStrings
import com.example.model.LocalizedStrings
import com.example.model.NetworkType
import com.example.ui.LauncherUiState
import com.example.ui.components.AppIconCard
import com.example.ui.components.ConcentricOrbitBackdrop
import com.example.ui.components.ContrastText
import com.example.ui.components.FocusSoundPlayer
import com.example.ui.components.FuturisticDockContainer
import com.example.ui.components.LauncherThemeFrameOverlay
import com.example.ui.components.LocalNavigationSoundEnabled
import com.example.ui.components.LocalTvFocusEnabled
import com.example.ui.components.OrbitAllAppsButton
import com.example.ui.components.OrbitContouredCardSurface
import com.example.ui.components.OrbitGreetingPill
import com.example.ui.components.OrbitLanguageCycleButton
import com.example.ui.components.ThemedClockDisplay
import com.example.ui.components.TvFocusableSurface
import com.example.ui.components.drawableToImageBitmap
import com.example.ui.components.weatherIconForCode
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: LauncherUiState,
    isHomeActive: Boolean,
    onNavigate: (LauncherDestination) -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onOpenContextApp: (AppItem?) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onHideApp: (String) -> Unit,
    onOpenAppInfo: (AppItem) -> Unit,
    onOpenNetworkSettings: () -> Unit,
    onExecuteGesture: (GestureType) -> Unit,
    onSelectHdmiSource: (String) -> Unit,
    onStopAdhan: () -> Unit,
    onUninstallApp: (AppItem) -> Unit = {},
    onToggleAiTool: (String) -> Unit = {},
    onLaunchAiTool: (String) -> Unit = {},
    onPowerOffTv: () -> Unit = {},
    onCycleLanguage: () -> Unit = {},
    renderContextDialog: Boolean = true,
) {
    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor
    val rimAccent = settings.rimAccentColor

    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var showAiToolsPicker by remember { mutableStateOf(false) }
    var aiToolToRemove by remember { mutableStateOf<String?>(null) }

    val centerFavFocusRequester = remember { FocusRequester() }
    val settingsPillFocusRequester = remember { FocusRequester() }

    BackHandler(enabled = isHomeActive && showAiToolsPicker) {
        showAiToolsPicker = false
    }
    BackHandler(enabled = isHomeActive && aiToolToRemove != null) {
        aiToolToRemove = null
    }

    CompositionLocalProvider(LocalTvFocusEnabled provides isHomeActive) {
        Box(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
            ) {
                LauncherThemeFrameOverlay(
                    skinStyle = settings.themePreset.skinStyle,
                    rimColor = rimAccent,
                    isCompact = maxWidth < 640.dp,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .pointerInput(settings.gestureShortcuts) {
                    detectTapGestures(
                        onDoubleTap = { onExecuteGesture(GestureType.DOUBLE_TAP) },
                        onLongPress = { onExecuteGesture(GestureType.LONG_PRESS) },
                    )
                }
                .pointerInput(settings.gestureShortcuts) {
                    detectDragGestures(
                        onDragStart = {
                            dragX = 0f
                            dragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragX += dragAmount.x
                            dragY += dragAmount.y
                        },
                        onDragEnd = {
                            val threshold = 110f
                            if (abs(dragX) > abs(dragY)) {
                                if (dragX > threshold) onExecuteGesture(GestureType.SWIPE_RIGHT)
                                else if (dragX < -threshold) onExecuteGesture(GestureType.SWIPE_LEFT)
                            } else {
                                if (dragY > threshold) onExecuteGesture(GestureType.SWIPE_DOWN)
                                else if (dragY < -threshold) onExecuteGesture(GestureType.SWIPE_UP)
                            }
                        }
                    )
                }
        ) {
            val availableWidth = maxWidth
            val isCompactWidth = availableWidth < 640.dp
            val isCompactHeight = maxHeight < 600.dp
            val horizontalPad = if (isCompactWidth) 14.dp else 26.dp
            val verticalPad = when {
                isCompactWidth -> 10.dp
                isCompactHeight -> 8.dp
                else -> 12.dp
            }
            val pillGlassAlpha = settings.glassOpacity.coerceIn(0f, 0.95f)
            val dockTitle = if (settings.customSectionTitle.isBlank() || settings.customSectionTitle == "HORIZON PRO") {
                strings.favoritesDockTitle
            } else {
                settings.customSectionTitle
            }
            val dockScale = settings.dockScalePercent.coerceIn(60, 130) / 100f
            val dockWidthFraction = if (dockScale < 1f) {
                (0.68f + (dockScale - 0.6f) * (0.32f / 0.4f)).coerceIn(0.68f, 1f)
            } else {
                1f
            }
            val crestHeightDp = (12f * dockScale).dp
            val dockSideInset = if (settings.dockStyle == DockStyle.MINIMAL_CARDS) 14.dp else 12.dp
            val headerRowSideInset = if (settings.showHdmiSourcesBar) {
                dockSideInset
            } else if (settings.dockStyle == DockStyle.MINIMAL_CARDS) {
                14.dp
            } else {
                0.dp
            }

            LaunchedEffect(isHomeActive, state.favoriteApps.isNotEmpty()) {
                if (isHomeActive) {
                    androidx.compose.runtime.withFrameNanos { }
                    runCatching {
                        if (state.favoriteApps.isNotEmpty()) {
                            centerFavFocusRequester.requestFocus()
                        } else {
                            settingsPillFocusRequester.requestFocus()
                        }
                    }
                }
            }

            val perRow = if (isCompactWidth) minOf(settings.appsPerRow, 4) else settings.appsPerRow
            val favoriteRows = state.favoriteApps.chunked(perRow)
            val centerFocusIdx = if (favoriteRows.isNotEmpty()) (favoriteRows[0].size - 1) / 2 else 0
            val dockGlassAlpha = settings.glassOpacity.coerceIn(0f, 0.95f)
            val tileSpacing = ((if (!isCompactWidth && (settings.iconSize == AppIconSize.LARGE || perRow >= 7)) 6f else 8f) * dockScale).dp
            val useScroll = isCompactWidth || settings.rowsBelowFavorites > 0 || favoriteRows.size > 1
            var showCalendarInLeftSlot by rememberSaveable(settings.showPrayerWidget, settings.showCalendarWidget) {
                mutableStateOf(settings.showCalendarWidget)
            }
            val hasLeftPrayer = settings.showPrayerWidget
            val hasLeftCalendar = settings.showCalendarWidget
            val canSwitchLeftCard = hasLeftPrayer && hasLeftCalendar
            val showCalendarActive = when {
                hasLeftCalendar && !hasLeftPrayer -> true
                hasLeftPrayer && !hasLeftCalendar -> false
                hasLeftCalendar && hasLeftPrayer -> showCalendarInLeftSlot
                else -> false
            }
            val showPrayerActive = (hasLeftPrayer || hasLeftCalendar) && !showCalendarActive

            val middleCardsRow: @Composable () -> Unit = {
                if (isCompactWidth) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_clock_header"),
                    ) {
                        ThemedClockDisplay(
                            formattedTime = state.formattedTime,
                            formattedDate = state.formattedDate,
                            settings = settings,
                            isCompact = true,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .weight(0.46f)
                                .align(Alignment.Top),
                        ) {
                            if (showPrayerActive) {
                                PrayerWidgetCard(
                                    state = state,
                                    strings = strings,
                                    accent = accent,
                                    isCompact = true,
                                    onClick = { onNavigate(LauncherDestination.PRAYER_AND_ADHAN) },
                                    onSwitchToCalendar = if (canSwitchLeftCard) {
                                        { showCalendarInLeftSlot = true }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .testTag("home_prayer_card"),
                                )
                            } else if (showCalendarActive) {
                                CalendarWidgetCard(
                                    state = state,
                                    strings = strings,
                                    accent = accent,
                                    isCompact = true,
                                    onClick = { onNavigate(LauncherDestination.CALENDAR_DETAIL) },
                                    onSwitchToPrayer = if (canSwitchLeftCard) {
                                        { showCalendarInLeftSlot = false }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .testTag("home_calendar_card"),
                                )
                            }
                            AnimatedVisibility(
                                visible = state.isAdhanPlaying,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    StopAdhanChip(
                                        label = strings.stopAdhan,
                                        isCompact = true,
                                        onClick = onStopAdhan,
                                    )
                                }
                            }
                        }

                        if (settings.showWeatherWidget) {
                            WeatherWidgetCard(
                                state = state,
                                strings = strings,
                                accent = accent,
                                isCompact = true,
                                onClick = { onNavigate(LauncherDestination.WEATHER_DETAIL) },
                                modifier = Modifier
                                    .weight(0.54f)
                                    .align(Alignment.Top)
                                    .wrapContentHeight()
                                    .testTag("home_weather_card"),
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(0.54f))
                        }
                    }
                } else {
                    val centerSlotWidth = if (availableWidth >= 1040.dp) 216.dp else 188.dp
                    val colGap = if (availableWidth >= 1040.dp) 18.dp else 12.dp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(colGap),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.Top),
                        ) {
                            if (showPrayerActive) {
                                PrayerWidgetCard(
                                    state = state,
                                    strings = strings,
                                    accent = accent,
                                    isCompact = false,
                                    onClick = { onNavigate(LauncherDestination.PRAYER_AND_ADHAN) },
                                    onSwitchToCalendar = if (canSwitchLeftCard) {
                                        { showCalendarInLeftSlot = true }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .testTag("home_prayer_card"),
                                )
                            } else if (showCalendarActive) {
                                CalendarWidgetCard(
                                    state = state,
                                    strings = strings,
                                    accent = accent,
                                    isCompact = false,
                                    onClick = { onNavigate(LauncherDestination.CALENDAR_DETAIL) },
                                    onSwitchToPrayer = if (canSwitchLeftCard) {
                                        { showCalendarInLeftSlot = false }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .testTag("home_calendar_card"),
                                )
                            }

                            AnimatedVisibility(
                                visible = state.isAdhanPlaying,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    StopAdhanChip(
                                        label = strings.stopAdhan,
                                        isCompact = false,
                                        onClick = onStopAdhan,
                                    )
                                }
                            }
                        }

                        Box(
                            contentAlignment = Alignment.TopCenter,
                            modifier = Modifier
                                .width(centerSlotWidth)
                                .testTag("home_clock_header"),
                        ) {
                            ThemedClockDisplay(
                                formattedTime = state.formattedTime,
                                formattedDate = state.formattedDate,
                                settings = settings,
                                isCompact = isCompactHeight,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.Top),
                        ) {
                            if (settings.showWeatherWidget) {
                                WeatherWidgetCard(
                                    state = state,
                                    strings = strings,
                                    accent = accent,
                                    isCompact = false,
                                    onClick = { onNavigate(LauncherDestination.WEATHER_DETAIL) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .testTag("home_weather_card"),
                                )
                            }
                        }
                    }
                }
            }

            val orbitBottomHeaderRow: @Composable () -> Unit = {
                if (settings.showHdmiSourcesBar) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(dockWidthFraction)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = dockSideInset),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp),
                                )
                                ContrastText(
                                    text = if (state.favoriteApps.isNotEmpty()) "$dockTitle (${state.favoriteApps.size})" else dockTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                                    enhancedContrast = settings.enhancedContrast,
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = (dockSideInset - 2.dp).coerceAtLeast(0.dp), end = dockSideInset)
                                    .testTag("home_hdmi_and_library_row"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                HdmiSourcesBar(
                                    activeSource = state.activeHdmiSource,
                                    surfaceHex = settings.themePreset.surfaceHex,
                                    glassOpacity = settings.glassOpacity,
                                    accent = accent,
                                    onSelectSource = onSelectHdmiSource,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OrbitAllAppsButton(
                                    label = strings.menuAppLibrary,
                                    ringLabel = strings.allAppsRingLabel,
                                    surfaceHex = settings.themePreset.surfaceHex,
                                    glassAlpha = pillGlassAlpha,
                                    accent = accent,
                                    buttonSize = 54.dp,
                                    onClick = { onNavigate(LauncherDestination.APP_LIBRARY) },
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(dockWidthFraction)
                                .padding(horizontal = headerRowSideInset),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(0.38f)
                                    .padding(start = 6.dp, bottom = 2.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp),
                                )
                                ContrastText(
                                    text = if (state.favoriteApps.isNotEmpty()) "$dockTitle (${state.favoriteApps.size})" else dockTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                                    enhancedContrast = settings.enhancedContrast,
                                )
                            }

                            OrbitAllAppsButton(
                                label = strings.menuAppLibrary,
                                ringLabel = strings.allAppsRingLabel,
                                surfaceHex = settings.themePreset.surfaceHex,
                                glassAlpha = pillGlassAlpha,
                                accent = accent,
                                buttonSize = if (isCompactHeight) 94.dp else 116.dp,
                                modifier = Modifier.align(Alignment.Center),
                                onClick = { onNavigate(LauncherDestination.APP_LIBRARY) },
                            )
                        }
                    }
                }
            }

            val rootColumnModifier = if (useScroll) {
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPad, vertical = verticalPad)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(maxHeight)
                    .padding(horizontal = horizontalPad, vertical = verticalPad)
            }

            Column(
                modifier = rootColumnModifier,
                verticalArrangement = Arrangement.Top,
            ) {
                // TOP ROW: Greeting Pill + Language Circle on LEFT, Unified Taskbar Pill on RIGHT
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(end = 8.dp)
                                .testTag("home_greeting_lang_header"),
                        ) {
                            if (settings.showGreeting) {
                                OrbitGreetingPill(
                                    text = state.greetingText,
                                    surfaceHex = settings.themePreset.surfaceHex,
                                    glassAlpha = pillGlassAlpha,
                                    accent = accent,
                                    enhancedContrast = settings.enhancedContrast,
                                    isCompact = isCompactWidth,
                                    rimColor = rimAccent,
                                )
                            }
                            OrbitLanguageCycleButton(
                                language = settings.language,
                                surfaceHex = settings.themePreset.surfaceHex,
                                glassAlpha = pillGlassAlpha,
                                accent = accent,
                                contentDescription = strings.languageSwitchLabel,
                                isCompact = isCompactWidth,
                                onClick = onCycleLanguage,
                                rimColor = rimAccent,
                            )
                        }

                        HorizontalAiToolsBar(
                            state = state,
                            strings = strings,
                            settingsFocusRequester = settingsPillFocusRequester,
                            isCompact = isCompactWidth,
                            onAddClick = { showAiToolsPicker = true },
                            onLaunchTool = onLaunchAiTool,
                            onLongPressTool = { key -> aiToolToRemove = key },
                            onOpenNetworkSettings = onOpenNetworkSettings,
                            onSearchClick = { onNavigate(LauncherDestination.SEARCH_APPS) },
                            onSettingsClick = { onNavigate(LauncherDestination.SETTINGS_MENU) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactWidth || isCompactHeight) 8.dp else 10.dp))

                // MIDDLE ORBIT DASHBOARD (Cards + Clock pinned at top red line, All Apps + Favoris anchored above dock)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = if (useScroll) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        },
                    ) {
                        ConcentricOrbitBackdrop(
                            accent = accent,
                            surfaceColor = Color(settings.themePreset.surfaceHex),
                            glassAlpha = pillGlassAlpha,
                            modifier = Modifier.matchParentSize(),
                            rimColor = rimAccent,
                        )

                        Column(
                            modifier = if (useScroll) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.fillMaxSize()
                            },
                            verticalArrangement = if (useScroll) Arrangement.Top else Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                middleCardsRow()
                            }
                            if (useScroll) {
                                Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 14.dp))
                            }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                orbitBottomHeaderRow()
                            }
                        }
                    }
                }

                // BOTTOM FAVORITES DOCK & EXTRA ROWS
                Column(modifier = Modifier.fillMaxWidth()) {

                    val dockContent: @Composable () -> Unit = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            val padH = (when (settings.dockStyle) {
                                DockStyle.MINIMAL_CARDS -> 10f
                                DockStyle.FLOATING_GLASS, DockStyle.FULL_WIDTH -> 8f
                            } * dockScale).dp
                            val padBottom = (when (settings.dockStyle) {
                                DockStyle.MINIMAL_CARDS -> 7f
                                DockStyle.FLOATING_GLASS, DockStyle.FULL_WIDTH -> 6f
                            } * dockScale).dp
                            val padTop = if (settings.dockStyle == DockStyle.FLOATING_GLASS) {
                                crestHeightDp + padBottom
                            } else {
                                padBottom
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(tileSpacing),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = padH, end = padH, top = padTop, bottom = padBottom),
                            ) {
                                if (favoriteRows.isEmpty()) {
                                    TvFocusableSurface(
                                        onClick = { onNavigate(LauncherDestination.APP_LIBRARY) },
                                        accentColor = accent,
                                        shape = RoundedCornerShape(16.dp),
                                        baseColor = Color(settings.themePreset.surfaceHex).copy(alpha = (dockGlassAlpha * 0.65f).coerceIn(0f, 0.75f)),
                                        baseBorderColor = accent.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("home_empty_favorites_card"),
                                    ) { isFocused ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Apps,
                                                contentDescription = strings.menuAppLibrary,
                                                tint = if (isFocused) PureWhite else accent,
                                                modifier = Modifier.size(26.dp),
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = strings.favoritesEmptyHint,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = PureWhite,
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy((8f * dockScale).dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        favoriteRows.forEachIndexed { rowIdx, rowApps ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(tileSpacing),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                rowApps.forEachIndexed { idx, app ->
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.weight(1f),
                                                    ) {
                                                        AppIconCard(
                                                            app = app,
                                                            settings = settings,
                                                            onClick = { onLaunchApp(app) },
                                                            onLongClick = { onOpenContextApp(app) },
                                                            showLabelBelow = isCompactWidth || favoriteRows.size > 1,
                                                            focusRequester = if (rowIdx == 0 && idx == centerFocusIdx) {
                                                                centerFavFocusRequester
                                                            } else {
                                                                null
                                                            },
                                                        )
                                                    }
                                                }
                                                repeat((perRow - rowApps.size).coerceAtLeast(0)) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (settings.dockStyle) {
                            DockStyle.FLOATING_GLASS -> FuturisticDockContainer(
                                surfaceColor = Color(settings.themePreset.surfaceHex),
                                glassAlpha = dockGlassAlpha,
                                accentColor = accent,
                                crestHeight = crestHeightDp,
                                cornerRadius = (settings.cardCornerRadiusDp + 4).dp,
                                modifier = Modifier.fillMaxWidth(dockWidthFraction).testTag("home_favorites_dock"),
                                rimColor = rimAccent,
                            ) {
                                dockContent()
                            }
                            DockStyle.FULL_WIDTH -> Surface(
                                shape = RoundedCornerShape(0.dp),
                                color = Color(settings.themePreset.surfaceHex).copy(alpha = dockGlassAlpha),
                                modifier = Modifier.fillMaxWidth(dockWidthFraction).testTag("home_favorites_dock"),
                            ) {
                                dockContent()
                            }
                            DockStyle.MINIMAL_CARDS -> Box(
                                modifier = Modifier.fillMaxWidth(dockWidthFraction).testTag("home_favorites_dock"),
                            ) {
                                dockContent()
                            }
                        }
                    }

                    // Extra rows below favorites ("Lignes sous les favoris": 0..3)
                    if (settings.rowsBelowFavorites > 0) {
                        val favPackages = state.favoriteApps.map { it.packageName }.toSet()
                        val remaining = state.visibleAppsOrdered.filterNot { favPackages.contains(it.packageName) }
                        val rows = remaining.chunked(perRow).take(settings.rowsBelowFavorites)

                        rows.forEach { rowApps ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(tileSpacing),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                            ) {
                                rowApps.forEach { app ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        AppIconCard(
                                            app = app,
                                            settings = settings,
                                            onClick = { onLaunchApp(app) },
                                            onLongClick = { onOpenContextApp(app) },
                                            showLabelBelow = true,
                                        )
                                    }
                                }
                                repeat((perRow - rowApps.size).coerceAtLeast(0)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // AI Tools Picker Dialog (opened from '+' button inside the Vertical AI Bar)
            if (showAiToolsPicker) {
                AiToolsPickerDialog(
                    state = state,
                    strings = strings,
                    accent = accent,
                    onToggleAiTool = onToggleAiTool,
                    onDismiss = { showAiToolsPicker = false },
                )
            }

            // Remove / Manage / Open AI Tool quick dialog (opened on long-press / Menu key of AI Bar item)
            val removeKey = aiToolToRemove
            if (removeKey != null) {
                val toolLabel = resolveAiToolDisplayName(removeKey, state.allApps)
                val aiRemoveFocus = remember(removeKey) { FocusRequester() }
                LaunchedEffect(removeKey) {
                    androidx.compose.runtime.withFrameNanos { }
                    runCatching { aiRemoveFocus.requestFocus() }
                }
                AlertDialog(
                    onDismissRequest = { aiToolToRemove = null },
                    containerColor = Color(0xFF0C1E44),
                    titleContentColor = PureWhite,
                    textContentColor = MutedSlate,
                    modifier = Modifier.testTag("ai_remove_dialog"),
                    title = { Text(toolLabel) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ContextMenuItem(
                                icon = Icons.Rounded.DeleteOutline,
                                label = strings.removeFromAiBar,
                                accent = Color(0xFFF43F5E),
                                iconTint = Color(0xFFFCA5A5),
                                focusRequester = aiRemoveFocus,
                                testTag = "ai_dialog_remove_btn",
                                onClick = {
                                    onToggleAiTool(removeKey)
                                    aiToolToRemove = null
                                },
                            )
                            ContextMenuItem(
                                icon = Icons.Rounded.AutoAwesome,
                                label = strings.manageAiToolsAction,
                                accent = accent,
                                testTag = "ai_dialog_manage_btn",
                                onClick = {
                                    aiToolToRemove = null
                                    showAiToolsPicker = true
                                },
                            )
                            ContextMenuItem(
                                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                                label = strings.openAiToolAction,
                                accent = accent,
                                testTag = "ai_dialog_open_btn",
                                onClick = {
                                    aiToolToRemove = null
                                    onLaunchAiTool(removeKey)
                                },
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { aiToolToRemove = null }) {
                            Text(strings.backLabel, color = accent)
                        }
                    },
                )
            }

            if (renderContextDialog && state.selectedAppForContext != null) {
                AppContextDialog(
                    app = state.selectedAppForContext,
                    state = state,
                    onDismiss = { onOpenContextApp(null) },
                    onToggleFavorite = onToggleFavorite,
                    onMoveFavorite = onMoveFavorite,
                    onHideApp = onHideApp,
                    onOpenAppInfo = onOpenAppInfo,
                    onUninstallApp = onUninstallApp,
                    onLaunchApp = onLaunchApp,
                )
            }
        }
        }
    }
}

@Composable
fun AppContextDialog(
    app: AppItem,
    state: LauncherUiState,
    onDismiss: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onHideApp: (String) -> Unit,
    onOpenAppInfo: (AppItem) -> Unit,
    onUninstallApp: (AppItem) -> Unit,
    onLaunchApp: (AppItem) -> Unit,
) {
    val strings = LauncherStrings.get(state.settings.language)
    val accent = state.settings.accentColor
    val isFav = state.favoriteApps.any { it.packageName == app.packageName }
    val dialogPrimaryFocus = remember(app.packageName) { FocusRequester() }

    LaunchedEffect(app.packageName) {
        androidx.compose.runtime.withFrameNanos { }
        runCatching { dialogPrimaryFocus.requestFocus() }
    }

    CompositionLocalProvider(LocalTvFocusEnabled provides true) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF0C1E44),
            titleContentColor = PureWhite,
            textContentColor = MutedSlate,
            modifier = Modifier.testTag("app_context_dialog"),
            title = { Text(app.label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContextMenuItem(
                        icon = if (isFav) Icons.Rounded.StarBorder else Icons.Rounded.Star,
                        label = if (isFav) strings.removeFromFavoritesOnly else strings.addToFavoritesAction,
                        accent = accent,
                        focusRequester = dialogPrimaryFocus,
                        testTag = if (isFav) "context_remove_favorite_btn" else "context_add_favorite_btn",
                        onClick = {
                            onToggleFavorite(app.packageName)
                            onDismiss()
                        },
                    )
                    ContextMenuItem(
                        icon = Icons.Rounded.DeleteOutline,
                        label = strings.uninstallAppAction,
                        accent = Color(0xFFF43F5E),
                        iconTint = Color(0xFFFCA5A5),
                        testTag = "context_uninstall_app_btn",
                        onClick = {
                            onUninstallApp(app)
                        },
                    )
                    ContextMenuItem(
                        icon = Icons.Rounded.Apps,
                        label = strings.openAppAction,
                        accent = accent,
                        testTag = "context_open_app_btn",
                        onClick = {
                            onDismiss()
                            onLaunchApp(app)
                        },
                    )
                    if (isFav) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ContextMenuItem(
                                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                label = strings.moveLeftAction,
                                accent = accent,
                                onClick = { onMoveFavorite(app.packageName, -1) },
                                modifier = Modifier.weight(1f),
                            )
                            ContextMenuItem(
                                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                                label = strings.moveRightAction,
                                accent = accent,
                                onClick = { onMoveFavorite(app.packageName, 1) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    ContextMenuItem(
                        icon = Icons.Rounded.VisibilityOff,
                        label = strings.hideAppAction,
                        accent = accent,
                        testTag = "context_hide_app_btn",
                        onClick = {
                            onHideApp(app.packageName)
                            onDismiss()
                        },
                    )
                    ContextMenuItem(
                        icon = Icons.Rounded.Info,
                        label = strings.appInfoAction,
                        accent = accent,
                        testTag = "context_app_info_btn",
                        onClick = { onOpenAppInfo(app) },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(strings.backLabel, color = accent)
                }
            },
        )
    }
}

@Composable
private fun StopAdhanChip(
    label: String,
    isCompact: Boolean,
    onClick: () -> Unit,
) {
    TvFocusableSurface(
        onClick = onClick,
        accentColor = EmeraldGlow,
        shape = RoundedCornerShape(18.dp),
        baseColor = EmeraldGlow.copy(alpha = 0.25f),
        baseBorderColor = EmeraldGlow,
        modifier = Modifier.testTag("home_stop_adhan_chip"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (isCompact) 12.dp else 16.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.GraphicEq,
                contentDescription = label,
                tint = EmeraldGlow,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = PureWhite,
            )
        }
    }
}

@Composable
private fun WeatherWidgetCard(
    state: LauncherUiState,
    strings: LocalizedStrings,
    accent: Color,
    isCompact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    val weather = state.weather
    val glassAlpha = settings.glassOpacity.coerceIn(0f, 0.95f)
    val unit = if (settings.useCelsius) "°" else "°F"
    val tempLocale = when (settings.language) {
        LauncherLanguage.FR -> java.util.Locale.FRENCH
        LauncherLanguage.AR -> java.util.Locale("ar")
        LauncherLanguage.EN -> java.util.Locale.ENGLISH
    }
    val daySlots = remember(weather?.daily, settings.language) {
        val loaded = weather?.daily.orEmpty().take(5)
        if (loaded.size >= 5) {
            loaded
        } else {
            val dayFmt = java.text.SimpleDateFormat("EEE", tempLocale)
            val cal = java.util.Calendar.getInstance()
            (0 until 5).map { idx ->
                loaded.getOrNull(idx) ?: run {
                    val slotCal = (cal.clone() as java.util.Calendar).apply {
                        add(java.util.Calendar.DAY_OF_YEAR, idx)
                    }
                    com.example.model.DailyWeatherItem(
                        dayLabel = dayFmt.format(slotCal.time),
                        weatherCode = weather?.weatherCode ?: 1,
                        tempMaxC = Double.NaN,
                        tempMinC = Double.NaN,
                    )
                }
            }
        }
    }

    OrbitContouredCardSurface(
        onClick = onClick,
        accentColor = accent,
        surfaceColor = Color(settings.themePreset.surfaceHex),
        glassAlpha = glassAlpha,
        cutoutDepth = if (isCompact) 12.dp else 26.dp,
        cornerRadius = 18.dp,
        isLeftPrayerShape = false,
        modifier = modifier,
        rimAccentColor = settings.rimAccentColor,
    ) { isFocused ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isCompact) 14.dp else 30.dp,
                    end = if (isCompact) 10.dp else 16.dp,
                    top = if (isCompact) 8.dp else 10.dp,
                    bottom = if (isCompact) 8.dp else 10.dp,
                ),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.weatherSectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isCompact) 14.sp else 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.18f))
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 7.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = weatherIconForCode(weather?.weatherCode ?: 1, weather?.isDay ?: true),
                    contentDescription = "Weather condition",
                    tint = if (isFocused) PureWhite else PureWhite.copy(alpha = 0.92f),
                    modifier = Modifier
                        .padding(start = if (isCompact) 2.dp else 6.dp)
                        .size(if (isCompact) 24.dp else 32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = if (settings.useGpsLocation) accent else MutedSlate,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = settings.shortCityName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isCompact) 10.sp else 12.sp),
                            color = if (isFocused) PureWhite else MutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val tempVal = weather?.let {
                        val c = it.temperatureC
                        val display = if (settings.useCelsius) c else (c * 9.0 / 5.0 + 32.0)
                        val unitLabel = if (settings.useCelsius) "°C" else "°F"
                        "${display.roundToInt()}$unitLabel"
                    } ?: "--°"
                    val condText = weather?.let {
                        LauncherStrings.weatherConditionLabel(it.weatherCode, settings.language)
                    } ?: "..."
                    Text(
                        text = "$tempVal · $condText",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isCompact) 13.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (weather != null) {
                        val hi = if (settings.useCelsius) weather.tempMaxC else (weather.tempMaxC * 9.0 / 5.0 + 32.0)
                        val lo = if (settings.useCelsius) weather.tempMinC else (weather.tempMinC * 9.0 / 5.0 + 32.0)
                        Text(
                            text = "H:${hi.roundToInt()}$unit L:${lo.roundToInt()}$unit · ${weather.windSpeedKmh.roundToInt()} km/h",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isCompact) 9.sp else 11.sp),
                            color = if (isFocused) PureWhite.copy(alpha = 0.9f) else MutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.14f))
            )
            Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_forecast_5day_row"),
            ) {
                daySlots.forEachIndexed { idx, dayItem ->
                    val shortDay = dayItem.dayLabel.trim().substringBefore(" ").take(if (isCompact) 3 else 5)
                    val tempSummary = if (!dayItem.tempMaxC.isNaN()) {
                        val hi = if (settings.useCelsius) dayItem.tempMaxC else (dayItem.tempMaxC * 9.0 / 5.0 + 32.0)
                        val lo = if (settings.useCelsius) dayItem.tempMinC else (dayItem.tempMinC * 9.0 / 5.0 + 32.0)
                        if (isCompact) "${hi.roundToInt()}°" else "${hi.roundToInt()}°/${lo.roundToInt()}°"
                    } else {
                        "--°"
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("weather_forecast_day_$idx"),
                    ) {
                        Text(
                            text = shortDay,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompact) 9.sp else 10.sp),
                            color = if (isFocused) PureWhite.copy(alpha = 0.92f) else MutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = weatherIconForCode(dayItem.weatherCode, true),
                            contentDescription = shortDay,
                            tint = if (isFocused) PureWhite else PureWhite.copy(alpha = 0.92f),
                            modifier = Modifier.size(if (isCompact) 14.dp else 16.dp),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tempSummary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = if (isCompact) 9.sp else 10.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerWidgetCard(
    state: LauncherUiState,
    strings: LocalizedStrings,
    accent: Color,
    isCompact: Boolean,
    onClick: () -> Unit,
    onSwitchToCalendar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    val next = state.nextPrayer ?: remember(
        settings.latitude,
        settings.longitude,
        settings.timezoneId,
        settings.prayerMethodId,
        settings.shortCityName,
        settings.is24HourFormat,
    ) {
        val calc = com.example.prayer.OfflinePrayerCalculator.calculate(
            latitude = settings.latitude,
            longitude = settings.longitude,
            timezoneId = settings.timezoneId,
            methodId = settings.prayerMethodId,
            cityDisplay = settings.shortCityName,
        )
        com.example.prayer.OfflinePrayerCalculator.computeNextPrayer(
            data = calc,
            is24Hour = settings.is24HourFormat,
        )
    }
    val prayerName = when (settings.language) {
        LauncherLanguage.FR -> next.key.labelFr
        LauncherLanguage.AR -> next.key.labelAr
        LauncherLanguage.EN -> next.key.labelEn
    }
    val remainingText = "${strings.inTimePrefix} ${next.remainingHours} h ${next.remainingMinutes} min"

    val hijriText = remember(
        state.prayerTimes?.hijriDate,
        state.prayerTimes?.hijriDateAr,
        settings.language,
        settings.timezoneId,
    ) {
        when (settings.language) {
            LauncherLanguage.AR -> state.prayerTimes?.hijriDateAr?.takeIf { it.isNotBlank() }
                ?: com.example.prayer.OfflinePrayerCalculator.formatHijriDate(LauncherLanguage.AR, settings.timezoneId)
            LauncherLanguage.FR -> com.example.prayer.OfflinePrayerCalculator.formatHijriDate(LauncherLanguage.FR, settings.timezoneId)
            LauncherLanguage.EN -> state.prayerTimes?.hijriDate?.takeIf { it.isNotBlank() }
                ?: com.example.prayer.OfflinePrayerCalculator.formatHijriDate(LauncherLanguage.EN, settings.timezoneId)
        }
    }

    val glassAlpha = settings.glassOpacity.coerceIn(0f, 0.95f)
    OrbitContouredCardSurface(
        onClick = onClick,
        accentColor = accent,
        surfaceColor = Color(settings.themePreset.surfaceHex),
        glassAlpha = glassAlpha,
        cutoutDepth = if (isCompact) 12.dp else 26.dp,
        cornerRadius = 18.dp,
        isLeftPrayerShape = true,
        modifier = modifier,
        rimAccentColor = settings.rimAccentColor,
    ) { isFocused ->
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isCompact) 10.dp else 16.dp,
                    end = if (isCompact) 14.dp else 30.dp,
                    top = if (isCompact) 8.dp else 10.dp,
                    bottom = if (isCompact) 8.dp else 10.dp,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = strings.prayerSectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isCompact) 14.sp else 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (onSwitchToCalendar != null) {
                    TvFocusableSurface(
                        onClick = onSwitchToCalendar,
                        accentColor = accent,
                        shape = RoundedCornerShape(8.dp),
                        baseColor = accent.copy(alpha = 0.18f),
                        modifier = Modifier.testTag("switch_left_widget_btn"),
                    ) {
                        Text(
                            text = "⇄ ${strings.leftWidgetModeCalendar}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = if (isCompact) 9.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = PureWhite,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.18f))
            )
            Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 7.dp))
            Text(
                text = "Aladhan · ${settings.shortCityName}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isCompact) 10.sp else 12.sp),
                color = if (isFocused) PureWhite else MutedSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (hijriText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = hijriText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isCompact) 9.5.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (isFocused) PureWhite.copy(alpha = 0.92f) else PureWhite.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("home_prayer_hijri_date"),
                )
            }
            if (settings.showDate && state.formattedDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = state.formattedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isCompact) 9.sp else 10.5.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = if (isFocused) PureWhite.copy(alpha = 0.86f) else MutedSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("home_prayer_gregorian_date"),
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "$prayerName · ${next.timeFormatted}",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = if (isCompact) 14.sp else 19.sp),
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = remainingText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isCompact) 10.sp else 12.sp),
                color = if (isFocused) PureWhite else accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CalendarWidgetCard(
    state: LauncherUiState,
    strings: LocalizedStrings,
    accent: Color,
    isCompact: Boolean,
    onClick: () -> Unit,
    onSwitchToPrayer: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    val dayBucket = state.formattedDate + "_" + settings.language.code + "_" + settings.timezoneId
    val snapshot = remember(dayBucket) {
        HorizonCalendarHelper.computeDailySnapshot(
            nowMillis = System.currentTimeMillis(),
            language = settings.language,
            timezoneId = settings.timezoneId,
        )
    }

    val glassAlpha = settings.glassOpacity.coerceIn(0f, 0.95f)
    OrbitContouredCardSurface(
        onClick = onClick,
        accentColor = accent,
        surfaceColor = Color(settings.themePreset.surfaceHex),
        glassAlpha = glassAlpha,
        cutoutDepth = if (isCompact) 12.dp else 26.dp,
        cornerRadius = 18.dp,
        isLeftPrayerShape = true,
        modifier = modifier,
        rimAccentColor = settings.rimAccentColor,
    ) { isFocused ->
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isCompact) 10.dp else 16.dp,
                    end = if (isCompact) 14.dp else 30.dp,
                    top = if (isCompact) 8.dp else 10.dp,
                    bottom = if (isCompact) 8.dp else 10.dp,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = strings.calendarSectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isCompact) 14.sp else 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (onSwitchToPrayer != null) {
                    TvFocusableSurface(
                        onClick = onSwitchToPrayer,
                        accentColor = accent,
                        shape = RoundedCornerShape(8.dp),
                        baseColor = accent.copy(alpha = 0.18f),
                        modifier = Modifier.testTag("switch_left_widget_btn"),
                    ) {
                        Text(
                            text = "⇄ ${strings.leftWidgetModeAdhan}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = if (isCompact) 9.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = PureWhite,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                    ) {
                        Text(
                            text = "${strings.calendarWeekPrefix} ${snapshot.weekOfYear}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = if (isCompact) 9.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = PureWhite,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.18f))
            )
            Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 7.dp))
            Text(
                text = "${snapshot.monthYearLabel} · ${snapshot.dayOfYear}/${snapshot.totalDaysInYear}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isCompact) 10.sp else 12.sp),
                color = if (isFocused) PureWhite else MutedSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_calendar_month_year"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${snapshot.dayOfMonth} · ${snapshot.weekdayName}",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = if (isCompact) 14.sp else 19.sp),
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_calendar_hero_day"),
            )
            Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_calendar_week_strip"),
            ) {
                snapshot.weekDays.forEach { dayItem ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (dayItem.isToday) accent.copy(alpha = 0.34f) else Color.Transparent,
                        border = if (dayItem.isToday) BorderStroke(1.dp, accent.copy(alpha = 0.85f)) else null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 2.dp),
                        ) {
                            Text(
                                text = dayItem.shortLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompact) 8.5.sp else 9.5.sp),
                                color = if (dayItem.isToday) PureWhite else MutedSlate,
                                maxLines = 1,
                            )
                            Text(
                                text = dayItem.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (isCompact) 9.5.sp else 10.5.sp,
                                    fontWeight = if (dayItem.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                ),
                                color = if (dayItem.isToday) PureWhite else PureWhite.copy(alpha = 0.88f),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalAiToolsBar(
    state: LauncherUiState,
    strings: LocalizedStrings,
    settingsFocusRequester: FocusRequester,
    isCompact: Boolean,
    onAddClick: () -> Unit,
    onLaunchTool: (String) -> Unit,
    onLongPressTool: (String) -> Unit,
    onOpenNetworkSettings: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    val accent = settings.accentColor
    val visibleShortcuts = settings.aiToolShortcuts.take(4)
    val barHeight = if (isCompact) 38.dp else 44.dp
    val itemSize = if (isCompact) 30.dp else 36.dp
    val iconGlyphSize = if (isCompact) 15.dp else 17.dp
    val glassAlpha = settings.glassOpacity.coerceIn(0f, 0.95f)
    val surfaceColor = Color(settings.themePreset.surfaceHex)

    val netIcon = when (state.networkStatus.type) {
        NetworkType.ETHERNET -> Icons.Rounded.SettingsEthernet
        NetworkType.WIFI -> Icons.Rounded.Wifi
        NetworkType.CELLULAR -> Icons.Rounded.SignalCellularAlt
        NetworkType.OFFLINE -> Icons.Rounded.WifiOff
    }

    val rimAccent = settings.rimAccentColor
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = surfaceColor.copy(alpha = glassAlpha),
        border = BorderStroke(1.1.dp, rimAccent.copy(alpha = 0.58f)),
        shadowElevation = if (glassAlpha < 0.08f) 0.dp else 4.dp,
        modifier = modifier
            .height(barHeight)
            .testTag("home_ai_tools_bar"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 5.dp else 6.dp),
            modifier = Modifier
                .height(barHeight)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = if (isCompact) 7.dp else 8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = strings.aiToolsTitle,
                tint = accent,
                modifier = Modifier.size(if (isCompact) 14.dp else 16.dp),
            )

            visibleShortcuts.forEach { storageKey ->
                AiBarShortcutButton(
                    storageKey = storageKey,
                    allApps = state.allApps,
                    accent = accent,
                    sizeDp = itemSize,
                    onClick = { onLaunchTool(storageKey) },
                    onLongClick = { onLongPressTool(storageKey) },
                )
            }

            TvFocusableSurface(
                onClick = onAddClick,
                accentColor = accent,
                shape = CircleShape,
                baseColor = accent.copy(alpha = 0.22f),
                focusedColor = accent.copy(alpha = 0.55f),
                baseBorderColor = accent.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(itemSize)
                    .testTag("ai_bar_add_btn"),
            ) { isFocused ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = strings.addAiToolAction,
                        tint = if (isFocused) PureWhite else PureWhite.copy(alpha = 0.94f),
                        modifier = Modifier.size(iconGlyphSize),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(if (isCompact) 16.dp else 18.dp)
                    .background(PureWhite.copy(alpha = 0.22f))
            )

            // Circular Wi-Fi / Ethernet icon inside unified top taskbar
            TvFocusableSurface(
                onClick = onOpenNetworkSettings,
                accentColor = accent,
                shape = CircleShape,
                baseColor = Color(0xFFEAF2FF).copy(alpha = (glassAlpha * 0.85f).coerceIn(0.16f, 0.85f)),
                focusedColor = accent,
                baseBorderColor = accent.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(itemSize)
                    .testTag("top_network_pill"),
            ) { isFocused ->
                val netTint = if (isFocused || glassAlpha < 0.42f) PureWhite else DeepSpaceBlack
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = netIcon,
                        contentDescription = state.networkStatus.label,
                        tint = netTint,
                        modifier = Modifier.size(iconGlyphSize),
                    )
                }
            }

            // Circular Search button inside unified top taskbar
            TvFocusableSurface(
                onClick = onSearchClick,
                accentColor = accent,
                shape = CircleShape,
                baseColor = surfaceColor.copy(alpha = (glassAlpha + 0.12f).coerceIn(0.22f, 0.92f)),
                focusedColor = accent.copy(alpha = 0.50f),
                baseBorderColor = accent.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(itemSize)
                    .testTag("top_search_button"),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = strings.menuSearchApps,
                        tint = PureWhite,
                        modifier = Modifier.size(iconGlyphSize),
                    )
                }
            }

            // Circular gear Settings icon inside unified top taskbar (no text label)
            TvFocusableSurface(
                onClick = onSettingsClick,
                accentColor = accent,
                shape = CircleShape,
                baseColor = surfaceColor.copy(alpha = (glassAlpha + 0.12f).coerceIn(0.22f, 0.92f)),
                focusedColor = accent.copy(alpha = 0.50f),
                baseBorderColor = accent.copy(alpha = 0.45f),
                focusRequester = settingsFocusRequester,
                modifier = Modifier
                    .size(itemSize)
                    .testTag("top_settings_button"),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = strings.settingsButton,
                        tint = PureWhite,
                        modifier = Modifier.size(iconGlyphSize),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AiBarShortcutButton(
    storageKey: String,
    allApps: List<AppItem>,
    accent: Color,
    sizeDp: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
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

    val preset = if (storageKey.startsWith("preset:")) {
        AiToolPreset.byId(storageKey.removePrefix("preset:"))
    } else null
    val matchingApp = when {
        storageKey.startsWith("app:") -> allApps.firstOrNull { it.packageName == storageKey.removePrefix("app:") }
        preset != null -> allApps.firstOrNull { it.packageName == preset.packageName }
        else -> null
    }
    val iconBitmap = remember(storageKey, matchingApp?.packageName) {
        drawableToImageBitmap(matchingApp?.icon ?: matchingApp?.banner)
    }
    val itemAccent = preset?.let { Color(it.accentHex) } ?: accent

    val scale by animateFloatAsState(
        targetValue = if (highlighted) 1.08f else 1f,
        animationSpec = spring(),
        label = "aiShortcutScale",
    )
    val bgColor by animateColorAsState(
        targetValue = if (highlighted) itemAccent.copy(alpha = 0.50f) else itemAccent.copy(alpha = 0.24f),
        label = "aiShortcutBg",
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(if (highlighted) 2.5.dp else 1.dp, if (highlighted) PureWhite else itemAccent.copy(alpha = 0.65f)),
        modifier = Modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
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
            .testTag("ai_tool_item_$storageKey"),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            when {
                iconBitmap != null -> Image(
                    bitmap = iconBitmap,
                    contentDescription = matchingApp?.label ?: preset?.label ?: storageKey,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(sizeDp - 12.dp).clip(CircleShape),
                )
                preset != null -> Text(
                    text = preset.shortBadge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                    ),
                    color = PureWhite,
                )
                else -> Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = storageKey,
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AiToolsPickerDialog(
    state: LauncherUiState,
    strings: LocalizedStrings,
    accent: Color,
    onToggleAiTool: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val activeShortcuts = state.settings.aiToolShortcuts.take(4)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0C1E44),
        titleContentColor = PureWhite,
        textContentColor = MutedSlate,
        modifier = Modifier.testTag("ai_tools_picker_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = accent)
                    Text(strings.aiToolsTitle)
                }
                Text(
                    text = "${activeShortcuts.size}/4",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = strings.aiPresetSectionTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
                AiToolPreset.ALL_PRESETS.forEach { preset ->
                    val added = activeShortcuts.contains(preset.storageKey)
                    TvFocusableSurface(
                        onClick = { onToggleAiTool(preset.storageKey) },
                        accentColor = Color(preset.accentHex),
                        shape = RoundedCornerShape(12.dp),
                        baseColor = Color.White.copy(alpha = 0.08f),
                        isSelected = added,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_picker_item_${preset.storageKey}"),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.accentHex).copy(alpha = 0.35f)),
                                ) {
                                    Text(
                                        text = preset.shortBadge,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PureWhite,
                                    )
                                }
                                Text(text = preset.label, style = MaterialTheme.typography.bodyLarge, color = PureWhite)
                            }
                            Icon(
                                imageVector = if (added) Icons.Rounded.CheckCircle else Icons.Rounded.Add,
                                contentDescription = null,
                                tint = if (added) EmeraldGlow else PureWhite.copy(alpha = 0.8f),
                            )
                        }
                    }
                }

                if (state.visibleAppsOrdered.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.aiInstalledAppsSectionTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                    )
                    state.visibleAppsOrdered.forEach { app ->
                        val key = "app:${app.packageName}"
                        val added = activeShortcuts.contains(key)
                        TvFocusableSurface(
                            onClick = { onToggleAiTool(key) },
                            accentColor = accent,
                            shape = RoundedCornerShape(12.dp),
                            baseColor = Color.White.copy(alpha = 0.07f),
                            isSelected = added,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_picker_item_app_${app.packageName}"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PureWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (added) Icons.Rounded.CheckCircle else Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = if (added) EmeraldGlow else PureWhite.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.saveAndClose, color = accent)
            }
        },
    )
}

private fun resolveAiToolDisplayName(storageKey: String, allApps: List<AppItem>): String {
    return when {
        storageKey.startsWith("preset:") -> {
            AiToolPreset.byId(storageKey.removePrefix("preset:"))?.label ?: storageKey
        }
        storageKey.startsWith("app:") -> {
            val pkg = storageKey.removePrefix("app:")
            allApps.firstOrNull { it.packageName == pkg }?.label ?: pkg
        }
        else -> storageKey
    }
}

@Composable
private fun HdmiSourcesBar(
    activeSource: String,
    surfaceHex: Long,
    glassOpacity: Float,
    accent: Color,
    onSelectSource: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sources = listOf("Android TV", "HDMI 1", "HDMI 2", "HDMI 3", "AV / Cast")
    val glassAlpha = glassOpacity.coerceIn(0f, 0.95f)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 2.dp, vertical = 3.dp),
    ) {
        sources.forEach { src ->
            val selected = src == activeSource
            TvFocusableSurface(
                onClick = { onSelectSource(src) },
                accentColor = accent,
                shape = RoundedCornerShape(12.dp),
                baseColor = Color(surfaceHex).copy(alpha = glassAlpha),
                isSelected = selected,
                modifier = Modifier
                    .height(38.dp)
                    .testTag("hdmi_source_$src"),
            ) { isFocused ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Input,
                        contentDescription = src,
                        tint = if (isFocused || selected) PureWhite else MutedSlate,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = src,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        color = PureWhite,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = PureWhite,
    focusRequester: FocusRequester? = null,
    testTag: String? = null,
) {
    val taggedModifier = if (testTag != null) modifier.fillMaxWidth().testTag(testTag) else modifier.fillMaxWidth()
    TvFocusableSurface(
        onClick = onClick,
        accentColor = accent,
        shape = RoundedCornerShape(12.dp),
        baseColor = Color.White.copy(alpha = 0.08f),
        focusRequester = focusRequester,
        modifier = taggedModifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = PureWhite)
        }
    }
}

@Composable
fun TvPowerSetupDialog(
    strings: LocalizedStrings,
    accent: Color,
    canEnableDeviceAdmin: Boolean,
    onEnableDeviceAdmin: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val primaryFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        runCatching { primaryFocus.requestFocus() }
    }
    CompositionLocalProvider(LocalTvFocusEnabled provides true) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF0C1E44),
            titleContentColor = PureWhite,
            textContentColor = MutedSlate,
            modifier = Modifier.testTag("tv_power_setup_dialog"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        tint = Color(0xFFFCA5A5),
                    )
                    Text(strings.tvPowerSetupTitle)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.tvPowerSetupDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite.copy(alpha = 0.88f),
                    )
                    if (canEnableDeviceAdmin) {
                        ContextMenuItem(
                            icon = Icons.Rounded.PowerSettingsNew,
                            label = strings.tvPowerEnableAdminBtn,
                            accent = Color(0xFFF43F5E),
                            iconTint = Color(0xFFFCA5A5),
                            focusRequester = primaryFocus,
                            testTag = "power_setup_admin_btn",
                            onClick = onEnableDeviceAdmin,
                        )
                    }
                    ContextMenuItem(
                        icon = Icons.Rounded.Settings,
                        label = strings.tvPowerEnableAccessibilityBtn,
                        accent = accent,
                        focusRequester = if (!canEnableDeviceAdmin) primaryFocus else null,
                        testTag = "power_setup_accessibility_btn",
                        onClick = onOpenAccessibilitySettings,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(strings.backLabel, color = accent)
                }
            },
        )
    }
}

