package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.LauncherDestination
import com.example.model.LauncherLanguage
import com.example.model.LauncherStrings
import com.example.power.TvPowerController
import com.example.power.TvPowerOffResult
import com.example.prayer.AdhanPlaybackService
import com.example.ui.LauncherViewModel
import com.example.ui.components.FocusSoundPlayer
import com.example.ui.components.LauncherWallpaperLayer
import com.example.ui.components.LocalNavigationSoundEnabled
import com.example.ui.screens.AboutSupportScreen
import com.example.ui.screens.AppContextDialog
import com.example.ui.screens.AppLibraryScreen
import com.example.ui.screens.CalendarDetailScreen
import com.example.ui.screens.EditFavoritesScreen
import com.example.ui.screens.GestureShortcutsScreen
import com.example.ui.screens.HiddenAppsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LauncherSettingsScreen
import com.example.ui.screens.PrayerAndAdhanScreen
import com.example.ui.screens.ReorderAppsScreen
import com.example.ui.screens.SearchAppsScreen
import com.example.ui.screens.SettingsMenuOverlay
import com.example.ui.screens.ThemeAndWallpaperScreen
import com.example.ui.screens.TvPowerSetupDialog
import com.example.ui.screens.WeatherDetailScreen
import com.example.ui.screens.GitHubUpdateDialogHost
import com.example.ui.theme.HorizonLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            )
        }
        setContent {
            HorizonLauncherRoot()
        }
    }
}

@Composable
fun HorizonLauncherRoot(
    viewModel: LauncherViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showPowerSetupDialog by remember { mutableStateOf(false) }

    var roleLaunchElapsedStart by remember { mutableStateOf(0L) }
    val homeRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val currentStrings = LauncherStrings.get(state.settings.language)
            val nowDefault = viewModel.isDefaultHomeLauncher()
            if (nowDefault) {
                viewModel.showStatus(currentStrings.defaultHomeSuccessStatus)
            } else {
                val quickAutoCancel = result.resultCode == android.app.Activity.RESULT_CANCELED &&
                    (System.currentTimeMillis() - roleLaunchElapsedStart) < 500L
                if (quickAutoCancel || result.resultCode != android.app.Activity.RESULT_CANCELED) {
                    if (!viewModel.openDefaultHomeFallbackSettings()) {
                        viewModel.showStatus(currentStrings.defaultHomeUnavailableStatus)
                    }
                } else {
                    viewModel.showStatus(currentStrings.defaultHomeUnavailableStatus)
                }
            }
        }
    )

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (TvPowerController.isDeviceAdminActive(context)) {
                TvPowerController.powerOffOrSleepTv(context)
            }
        }
    )

    val unknownSourcesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            viewModel.retryInstallDownloadedApk(onlyIfAwaitingPermission = true)
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshInstalledApps()
                viewModel.retryInstallDownloadedApk(onlyIfAwaitingPermission = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatus()
        }
    }

    LaunchedEffect(Unit) {
        FocusSoundPlayer.warmUp(context)
    }

    LaunchedEffect(state.destination) {
        FocusSoundPlayer.markTransition()
    }

    val layoutDirection = if (state.settings.language == LauncherLanguage.AR) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalNavigationSoundEnabled provides state.settings.navigationSoundEnabled,
    ) {
        HorizonLauncherTheme(
            accentColor = state.settings.accentColor,
            surfaceTint = Color(state.settings.themePreset.surfaceHex),
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            ) { innerPadding ->
                // Root fills full edge-to-edge screen; inner screens handle safeDrawing insets explicitly
                val _unusedPadding = innerPadding
                Box(modifier = Modifier.fillMaxSize()) {
                    BackHandler(enabled = state.destination == LauncherDestination.HOME && state.selectedAppForContext == null && !showPowerSetupDialog) {
                        // Home launcher stays rock-solid on Back press instead of finishing and restarting
                    }
                    BackHandler(enabled = state.selectedAppForContext != null) {
                        viewModel.setContextApp(null)
                    }
                    BackHandler(enabled = showPowerSetupDialog) {
                        showPowerSetupDialog = false
                    }

                    LauncherWallpaperLayer(settings = state.settings)

                    HomeScreen(
                        state = state,
                        isHomeActive = state.destination == LauncherDestination.HOME && !showPowerSetupDialog,
                        onNavigate = viewModel::navigateTo,
                        onLaunchApp = viewModel::launchApp,
                        onOpenContextApp = viewModel::setContextApp,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onMoveFavorite = viewModel::moveFavorite,
                        onHideApp = viewModel::toggleHiddenApp,
                        onOpenAppInfo = viewModel::openAppInfo,
                        onUninstallApp = { app -> viewModel.uninstallApp(app, context) },
                        onOpenNetworkSettings = viewModel::openNetworkSettings,
                        onExecuteGesture = viewModel::executeGesture,
                        onSelectHdmiSource = viewModel::selectHdmiSource,
                        onStopAdhan = { AdhanPlaybackService.stopAdhan(context) },
                        onToggleAiTool = viewModel::toggleAiToolShortcut,
                        onLaunchAiTool = viewModel::launchAiTool,
                        onPowerOffTv = {
                            if (TvPowerController.powerOffOrSleepTv(context) == TvPowerOffResult.NEEDS_SETUP) {
                                showPowerSetupDialog = true
                            }
                        },
                        onCycleLanguage = viewModel::cycleLanguage,
                        onOpenRadio = viewModel::openRadioPicker,
                        onPlayRadioStation = viewModel::playRadioStation,
                        onToggleRadioPlayPause = viewModel::toggleRadioPlayPause,
                        onRefreshRadio = { viewModel.loadLocalRadioStations() },
                        onSelectRadioCountry = viewModel::setRadioCountryFilter,
                        onSearchRadio = viewModel::searchRadioStations,
                        onToggleFavoriteRadioStation = viewModel::toggleFavoriteRadioStation,
                        onRefreshNews = { viewModel.refreshNewsTicker() },
                        onOpenNewsArticle = viewModel::openNewsArticleUrl,
                        renderContextDialog = false,
                    )

                    val requestDefaultHome: () -> Unit = {
                        val currentStrings = LauncherStrings.get(state.settings.language)
                        if (viewModel.isDefaultHomeLauncher()) {
                            viewModel.showStatus(currentStrings.defaultHomeAlreadyActiveStatus)
                        } else {
                            val roleIntent = viewModel.getDefaultHomeIntent()
                            if (roleIntent != null) {
                                roleLaunchElapsedStart = System.currentTimeMillis()
                                val launched = runCatching {
                                    homeRoleLauncher.launch(roleIntent)
                                    true
                                }.getOrDefault(false)
                                if (!launched && !viewModel.openDefaultHomeFallbackSettings()) {
                                    viewModel.showStatus(currentStrings.defaultHomeUnavailableStatus)
                                }
                            } else if (!viewModel.openDefaultHomeFallbackSettings()) {
                                viewModel.showStatus(currentStrings.defaultHomeUnavailableStatus)
                            }
                        }
                    }

                    Crossfade(
                        targetState = state.destination,
                        animationSpec = tween(durationMillis = if (state.settings.animationsEnabled) 220 else 0),
                        label = "destinationTransition",
                    ) { dest ->
                        when (dest) {
                            LauncherDestination.HOME -> Unit
                            LauncherDestination.SETTINGS_MENU -> SettingsMenuOverlay(
                                state = state,
                                onDismiss = { viewModel.navigateTo(LauncherDestination.HOME) },
                                onNavigate = viewModel::navigateTo,
                                onOpenSystemSettings = viewModel::openSystemSettings,
                                onChangeLanguage = { lang ->
                                    viewModel.updateSettings { it.copy(language = lang) }
                                },
                            )
                            LauncherDestination.LAUNCHER_SETTINGS -> LauncherSettingsScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onNavigate = viewModel::navigateTo,
                                onUpdateSettings = viewModel::updateSettings,
                                onFollowThemeReset = viewModel::followThemeReset,
                                onRequestDefaultHome = requestDefaultHome,
                                onCheckUpdates = { viewModel.checkForUpdates(silent = false) },
                                onOpenRepoSetup = viewModel::openGithubRepoSetup,
                            )
                            LauncherDestination.APP_LIBRARY -> AppLibraryScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onLaunchApp = viewModel::launchApp,
                                onOpenContextApp = viewModel::setContextApp,
                            )
                            LauncherDestination.EDIT_FAVORITES -> EditFavoritesScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onToggleFavorite = viewModel::toggleFavorite,
                            )
                            LauncherDestination.REORDER_APPS -> ReorderAppsScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onMoveApp = viewModel::moveAppInOrder,
                                onMoveFavorite = viewModel::moveFavorite,
                            )
                            LauncherDestination.SEARCH_APPS -> SearchAppsScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onLaunchApp = viewModel::launchApp,
                            )
                            LauncherDestination.HIDDEN_APPS -> HiddenAppsScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onToggleHidden = viewModel::toggleHiddenApp,
                            )
                            LauncherDestination.ABOUT_SUPPORT -> AboutSupportScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onUpdateSettings = viewModel::updateSettings,
                                onRequestDefaultHome = requestDefaultHome,
                            )
                            LauncherDestination.THEME_AND_WALLPAPER -> ThemeAndWallpaperScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onUpdateSettings = viewModel::updateSettings,
                                onApplyThemePreset = viewModel::applyThemePreset,
                                onSelectCustomWallpaper = viewModel::setCustomWallpaper,
                            )
                            LauncherDestination.GESTURE_SHORTCUTS -> GestureShortcutsScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onUpdateGesture = viewModel::updateGestureShortcut,
                                onTestGesture = viewModel::executeGesture,
                            )
                            LauncherDestination.WEATHER_DETAIL -> WeatherDetailScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onSyncGps = viewModel::syncWithGpsLocation,
                                onRefreshWeather = { viewModel.refreshPrayerAndWeather() },
                                onSearchCity = viewModel::searchCities,
                                onSelectCity = viewModel::selectCity,
                                onUpdateSettings = viewModel::updateSettings,
                            )
                            LauncherDestination.PRAYER_AND_ADHAN -> PrayerAndAdhanScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onSyncGps = viewModel::syncWithGpsLocation,
                                onSearchCity = viewModel::searchCities,
                                onSelectCity = viewModel::selectCity,
                                onUpdateSettings = viewModel::updateSettings,
                                onTogglePrayer = viewModel::togglePrayerEnabled,
                                onCustomAdhanPicked = viewModel::setCustomAdhanUri,
                                onClearCustomAdhan = viewModel::clearCustomAdhanUri,
                                onTestOrStopAdhan = viewModel::testOrStopAdhan,
                            )
                            LauncherDestination.CALENDAR_DETAIL -> CalendarDetailScreen(
                                state = state,
                                onBack = { viewModel.navigateBack() },
                                onUpdateSettings = viewModel::updateSettings,
                            )
                        }
                    }

                    val contextApp = state.selectedAppForContext
                    if (contextApp != null) {
                        AppContextDialog(
                            app = contextApp,
                            state = state,
                            onDismiss = { viewModel.setContextApp(null) },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onMoveFavorite = viewModel::moveFavorite,
                            onHideApp = viewModel::toggleHiddenApp,
                            onOpenAppInfo = viewModel::openAppInfo,
                            onUninstallApp = { app -> viewModel.uninstallApp(app, context) },
                            onLaunchApp = viewModel::launchApp,
                        )
                    }

                    if (showPowerSetupDialog) {
                        val strings = LauncherStrings.get(state.settings.language)
                        TvPowerSetupDialog(
                            strings = strings,
                            accent = state.settings.accentColor,
                            canEnableDeviceAdmin = TvPowerController.canRequestDeviceAdmin(context),
                            onEnableDeviceAdmin = {
                                showPowerSetupDialog = false
                                val adminIntent = TvPowerController.createEnableDeviceAdminIntent(
                                    context = context,
                                    explanation = strings.tvPowerAdminExplanation,
                                )
                                val launched = runCatching {
                                    deviceAdminLauncher.launch(adminIntent)
                                    true
                                }.getOrDefault(false)
                                if (!launched) {
                                    val accOk = runCatching {
                                        context.startActivity(TvPowerController.createAccessibilitySettingsIntent())
                                        true
                                    }.getOrDefault(false)
                                    if (!accOk) viewModel.openSystemSettings()
                                }
                            },
                            onOpenAccessibilitySettings = {
                                showPowerSetupDialog = false
                                val accOk = runCatching {
                                    context.startActivity(TvPowerController.createAccessibilitySettingsIntent())
                                    true
                                }.getOrDefault(false)
                                if (!accOk) viewModel.openSystemSettings()
                            },
                            onDismiss = { showPowerSetupDialog = false },
                        )
                    }

                    val updateUiStrings = LauncherStrings.get(state.settings.language)
                    GitHubUpdateDialogHost(
                        updateState = state.updateState,
                        currentRepo = state.settings.githubUpdateRepo,
                        strings = updateUiStrings,
                        accent = state.settings.accentColor,
                        onSaveRepo = viewModel::saveGithubRepo,
                        onDownloadAndInstall = viewModel::downloadAndInstallUpdate,
                        onRetryInstall = viewModel::retryInstallDownloadedApk,
                        onRequestInstallPermission = {
                            runCatching {
                                unknownSourcesLauncher.launch(viewModel.getUnknownSourcesSettingsIntent())
                            }.onFailure {
                                viewModel.openSystemSettings()
                            }
                        },
                        onDismiss = viewModel::dismissUpdateDialog,
                    )
                }
            }
        }
    }
}
