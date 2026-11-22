package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InstalledAppsRepository
import com.example.data.LauncherPreferencesRepository
import com.example.data.LocationWeatherRepository
import com.example.data.PrayerTimesRepository
import com.example.model.AdhanAlertMode
import com.example.model.AiToolPreset
import com.example.model.AppIconShape
import com.example.model.AppIconSize
import com.example.model.AppItem
import com.example.model.CitySearchResult
import com.example.model.ClockTheme
import com.example.model.DockStyle
import com.example.model.GestureAction
import com.example.model.GestureType
import com.example.model.LauncherDestination
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.LauncherStrings
import com.example.model.NetworkStatus
import com.example.model.NextPrayerInfo
import com.example.model.PrayerKey
import com.example.model.PrayerTimesData
import com.example.model.ThemePreset
import com.example.model.WeatherData
import com.example.prayer.AdhanPlaybackService
import com.example.prayer.AdhanScheduler
import com.example.prayer.OfflinePrayerCalculator
import com.example.update.AppUpdateUiState
import com.example.update.GitHubUpdateRepository
import com.example.update.UpdateCheckResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class LauncherUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val destination: LauncherDestination = LauncherDestination.HOME,
    val allApps: List<AppItem> = emptyList(),
    val favoriteApps: List<AppItem> = emptyList(),
    val visibleAppsOrdered: List<AppItem> = emptyList(),
    val hiddenApps: List<AppItem> = emptyList(),
    val networkStatus: NetworkStatus = NetworkStatus(),
    val formattedTime: String = "17:36",
    val formattedDate: String = "LUNDI 16 NOVEMBRE",
    val greetingText: String = "BONSOIR",
    val prayerTimes: PrayerTimesData? = null,
    val nextPrayer: NextPrayerInfo? = null,
    val weather: WeatherData? = null,
    val isRefreshingGps: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val citySearchResults: List<CitySearchResult> = emptyList(),
    val selectedAppForContext: AppItem? = null,
    val activeHdmiSource: String = "Android TV",
    val statusMessage: String? = null,
    val isAdhanPlaying: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val updateState: AppUpdateUiState = AppUpdateUiState.Idle,
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = LauncherPreferencesRepository(application)
    private val appsRepo = InstalledAppsRepository(application)
    private val locationWeatherRepo = LocationWeatherRepository(application)
    private val prayerRepo = PrayerTimesRepository()
    private val adhanScheduler = AdhanScheduler(application)
    private val updateRepo = GitHubUpdateRepository(application)

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        isRefreshingGpsFlow.value = false
        isLoadingWeatherFlow.value = false
    }

    private fun safeLaunch(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(exceptionHandler, block = block)

    private val destinationFlow = MutableStateFlow(LauncherDestination.HOME)
    private val allAppsFlow = MutableStateFlow<List<AppItem>>(emptyList())
    private val networkStatusFlow = MutableStateFlow(runCatching { appsRepo.getCurrentNetworkStatus() }.getOrDefault(NetworkStatus()))
    private val nowMillisFlow = MutableStateFlow(System.currentTimeMillis())
    private val prayerTimesFlow = MutableStateFlow<PrayerTimesData?>(null)
    private val weatherFlow = MutableStateFlow<WeatherData?>(null)
    private val isRefreshingGpsFlow = MutableStateFlow(false)
    private val isLoadingWeatherFlow = MutableStateFlow(false)
    private val citySearchResultsFlow = MutableStateFlow<List<CitySearchResult>>(emptyList())
    private val contextAppFlow = MutableStateFlow<AppItem?>(null)
    private val activeHdmiSourceFlow = MutableStateFlow("Android TV")
    private val statusMessageFlow = MutableStateFlow<String?>(null)
    private val locationPermFlow = MutableStateFlow(runCatching { locationWeatherRepo.hasLocationPermission() }.getOrDefault(false))
    private val updateStateFlow = MutableStateFlow<AppUpdateUiState>(AppUpdateUiState.Idle)

    private var lastLoadedLocationKey: String? = null
    private var hasAutoCheckedUpdates = false

    private val initialUiState: LauncherUiState = buildInitialUiState()

    val uiState: StateFlow<LauncherUiState> = combine(
        prefsRepo.settingsFlow,
        destinationFlow,
        allAppsFlow,
        networkStatusFlow,
        nowMillisFlow,
        prayerTimesFlow,
        weatherFlow,
        isRefreshingGpsFlow,
        isLoadingWeatherFlow,
        citySearchResultsFlow,
        contextAppFlow,
        activeHdmiSourceFlow,
        statusMessageFlow,
        AdhanPlaybackService.isPlaying,
        locationPermFlow,
        updateStateFlow,
    ) { arr ->
        val settings = arr[0] as LauncherSettings
        val dest = arr[1] as LauncherDestination
        @Suppress("UNCHECKED_CAST")
        val allApps = arr[2] as List<AppItem>
        val network = arr[3] as NetworkStatus
        val nowMillis = arr[4] as Long
        val prayerTimes = arr[5] as PrayerTimesData?
        val weather = arr[6] as WeatherData?
        val refreshingGps = arr[7] as Boolean
        val loadingWeather = arr[8] as Boolean
        @Suppress("UNCHECKED_CAST")
        val cityResults = arr[9] as List<CitySearchResult>
        val contextApp = arr[10] as AppItem?
        val hdmiSource = arr[11] as String
        val statusMsg = arr[12] as String?
        val adhanPlaying = arr[13] as Boolean
        val hasLocPerm = arr[14] as Boolean
        val updateState = arr[15] as AppUpdateUiState

        val orderedAll = applyCustomOrder(allApps, settings.customAppOrder)
        val visibleOrdered = orderedAll.filterNot { settings.hiddenPackages.contains(it.packageName) }
        val hiddenList = orderedAll.filter { settings.hiddenPackages.contains(it.packageName) }

        val mapByPkg = visibleOrdered.associateBy { it.packageName }
        val favs = settings.favoritePackages.mapNotNull { mapByPkg[it] }

        val effectivePrayer = runCatching {
            prayerTimes ?: OfflinePrayerCalculator.calculate(
                latitude = settings.latitude,
                longitude = settings.longitude,
                timezoneId = settings.timezoneId,
                methodId = settings.prayerMethodId,
                cityDisplay = settings.shortCityName,
            )
        }.getOrNull()
        val nextPrayer = effectivePrayer?.let {
            runCatching {
                OfflinePrayerCalculator.computeNextPrayer(
                    data = it,
                    nowMillis = nowMillis,
                    is24Hour = settings.is24HourFormat,
                )
            }.getOrNull()
        }

        val (timeFmt, dateFmt, greeting) = formatClockAndGreeting(nowMillis, settings)

        LauncherUiState(
            settings = settings,
            destination = dest,
            allApps = orderedAll,
            favoriteApps = favs,
            visibleAppsOrdered = visibleOrdered,
            hiddenApps = hiddenList,
            networkStatus = network,
            formattedTime = timeFmt,
            formattedDate = dateFmt,
            greetingText = greeting,
            prayerTimes = effectivePrayer,
            nextPrayer = nextPrayer,
            weather = weather,
            isRefreshingGps = refreshingGps,
            isLoadingWeather = loadingWeather,
            citySearchResults = cityResults,
            selectedAppForContext = contextApp,
            activeHdmiSource = hdmiSource,
            statusMessage = statusMsg,
            isAdhanPlaying = adhanPlaying,
            hasLocationPermission = hasLocPerm,
            updateState = updateState,
        )
    }.distinctUntilChanged().catch {
        emit(initialUiState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = initialUiState,
    )

    private fun buildInitialUiState(): LauncherUiState {
        val settings = LauncherSettings()
        val nowMillis = nowMillisFlow.value
        val initialPrayer = runCatching {
            OfflinePrayerCalculator.calculate(
                latitude = settings.latitude,
                longitude = settings.longitude,
                timezoneId = settings.timezoneId,
                methodId = settings.prayerMethodId,
                cityDisplay = settings.shortCityName,
            )
        }.getOrNull()
        val initialNextPrayer = initialPrayer?.let {
            runCatching {
                OfflinePrayerCalculator.computeNextPrayer(
                    data = it,
                    nowMillis = nowMillis,
                    is24Hour = settings.is24HourFormat,
                )
            }.getOrNull()
        }
        val (timeFmt, dateFmt, greeting) = formatClockAndGreeting(nowMillis, settings)
        return LauncherUiState(
            settings = settings,
            networkStatus = networkStatusFlow.value,
            formattedTime = timeFmt,
            formattedDate = dateFmt,
            greetingText = greeting,
            prayerTimes = initialPrayer,
            nextPrayer = initialNextPrayer,
            hasLocationPermission = locationPermFlow.value,
        )
    }

    private fun formatClockAndGreeting(
        nowMillis: Long,
        settings: LauncherSettings,
    ): Triple<String, String, String> {
        val strings = LauncherStrings.get(settings.language)
        val locale = when (settings.language) {
            LauncherLanguage.FR -> Locale.FRENCH
            LauncherLanguage.AR -> Locale("ar")
            LauncherLanguage.EN -> Locale.ENGLISH
        }
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> strings.greetingMorning
            hour < 18 -> strings.greetingAfternoon
            else -> strings.greetingEvening
        }
        val timePattern = if (settings.is24HourFormat) "HH:mm" else "h:mm a"
        val timeFmt = runCatching {
            SimpleDateFormat(timePattern, locale).format(Date(nowMillis))
        }.getOrDefault("--:--")
        val datePattern = if (settings.language == LauncherLanguage.FR) "EEEE d MMMM" else "EEEE, d MMMM"
        val dateFmt = runCatching {
            SimpleDateFormat(datePattern, locale).format(Date(nowMillis)).uppercase(locale)
        }.getOrDefault("")
        return Triple(timeFmt, dateFmt, greeting)
    }

    init {
        refreshInstalledApps()
        startClockAndNetworkTicker()
        observeSettingsForData()
    }

    private fun startClockAndNetworkTicker() {
        safeLaunch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val minuteBucket = (now / 60000L) * 60000L
                if (nowMillisFlow.value / 60000L != minuteBucket / 60000L) {
                    nowMillisFlow.value = now
                }
                val (netStatus, hasPerm) = withContext(Dispatchers.IO) {
                    val net = runCatching { appsRepo.getCurrentNetworkStatus() }.getOrDefault(NetworkStatus())
                    val perm = runCatching { locationWeatherRepo.hasLocationPermission() }.getOrDefault(false)
                    net to perm
                }
                if (networkStatusFlow.value != netStatus) {
                    networkStatusFlow.value = netStatus
                }
                if (locationPermFlow.value != hasPerm) {
                    locationPermFlow.value = hasPerm
                }
                checkForegroundPrayerAutoTrigger(now)
                delay(15000L)
            }
        }
    }

    private fun checkForegroundPrayerAutoTrigger(nowMillis: Long) {
        val state = uiState.value
        val settings = state.settings
        if (!settings.adhanEnabled || settings.adhanMode == AdhanAlertMode.SILENT) return
        val prayerTimes = state.prayerTimes ?: return

        val tz = runCatching { TimeZone.getTimeZone(settings.timezoneId) }.getOrElse { TimeZone.getDefault() }
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }.format(Date(nowMillis))
        val currentHHmm = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = tz }.format(Date(nowMillis))

        val matchingPrayer = prayerTimes.entries.firstOrNull { entry ->
            settings.enabledPrayers.contains(entry.key) && entry.time24.take(5) == currentHHmm
        } ?: return

        val context = getApplication<Application>()
        if (!AdhanPlaybackService.markPrayerTriggeredIfNew(context, matchingPrayer.key.id, dateKey)) return

        val prayerLabel = when (settings.language) {
            LauncherLanguage.FR -> matchingPrayer.key.labelFr
            LauncherLanguage.AR -> matchingPrayer.key.labelAr
            LauncherLanguage.EN -> matchingPrayer.key.labelEn
        }

        when (settings.adhanMode) {
            AdhanAlertMode.AUDIO_AND_NOTIFICATION -> {
                AdhanPlaybackService.startAdhan(
                    context = context,
                    prayerName = prayerLabel,
                    customUri = settings.customAdhanAudioUri,
                    cityName = settings.shortCityName,
                )
            }
            AdhanAlertMode.NOTIFICATION_ONLY -> {
                AdhanPlaybackService.showPrayerNotificationOnly(
                    context = context,
                    prayerName = prayerLabel,
                    cityName = settings.shortCityName,
                )
            }
            AdhanAlertMode.SILENT -> Unit
        }
    }

    private fun observeSettingsForData() {
        safeLaunch {
            delay(300L)
            prefsRepo.settingsFlow.collect { settings ->
                val key = "${settings.latitude},${settings.longitude},${settings.timezoneId},${settings.prayerMethodId},${settings.shortCityName}"
                if (key != lastLoadedLocationKey) {
                    lastLoadedLocationKey = key
                    refreshPrayerAndWeather(settings)
                }
                if (!hasAutoCheckedUpdates &&
                    settings.autoCheckUpdatesOnStartup &&
                    settings.githubUpdateRepo.isNotBlank()
                ) {
                    hasAutoCheckedUpdates = true
                    safeLaunch {
                        delay(1500L)
                        checkForUpdates(silent = true, overrideSettings = settings)
                    }
                }
            }
        }
    }

    fun refreshInstalledApps() {
        safeLaunch {
            val apps = runCatching { appsRepo.loadInstalledApps() }.getOrDefault(emptyList())
            val current = allAppsFlow.value
            val changed = current.size != apps.size ||
                current.zip(apps).any { (oldApp, newApp) ->
                    oldApp.packageName != newApp.packageName || oldApp.label != newApp.label
                }
            if (current.isEmpty() || changed) {
                allAppsFlow.value = apps
            }
        }
    }

    fun refreshPrayerAndWeather(overrideSettings: LauncherSettings? = null) {
        val s = overrideSettings ?: uiState.value.settings
        safeLaunch {
            isLoadingWeatherFlow.value = true
            val prayerJob = launch(exceptionHandler) {
                val data = runCatching {
                    prayerRepo.getPrayerTimes(
                        latitude = s.latitude,
                        longitude = s.longitude,
                        timezoneId = s.timezoneId,
                        methodId = s.prayerMethodId,
                        shortCityDisplay = s.shortCityName,
                    )
                }.getOrNull()
                if (data != null) {
                    prayerTimesFlow.value = data
                    runCatching { adhanScheduler.scheduleDailyPrayers(data, s) }
                }
            }
            val weatherJob = launch(exceptionHandler) {
                val w = runCatching {
                    locationWeatherRepo.fetchWeather(
                        latitude = s.latitude,
                        longitude = s.longitude,
                        cityDisplay = s.shortCityName,
                        isGpsSynced = s.useGpsLocation,
                    )
                }.getOrNull()
                if (w != null) weatherFlow.value = w
            }
            prayerJob.join()
            weatherJob.join()
            isLoadingWeatherFlow.value = false
        }
    }

    fun syncWithGpsLocation() {
        locationPermFlow.value = runCatching { locationWeatherRepo.hasLocationPermission() }.getOrDefault(false)
        safeLaunch {
            isRefreshingGpsFlow.value = true
            val gps = runCatching { locationWeatherRepo.getCurrentGpsLocation() }.getOrNull()
            if (gps != null) {
                runCatching {
                    prefsRepo.updateSettings { s ->
                        s.copy(
                            useGpsLocation = true,
                            latitude = gps.latitude,
                            longitude = gps.longitude,
                            shortCityName = gps.shortCityName,
                            cityName = gps.fullCityName,
                            timezoneId = gps.timezoneId,
                        )
                    }
                }
                showStatus("GPS : ${gps.shortCityName}")
            } else {
                showStatus(
                    when (uiState.value.settings.language) {
                        LauncherLanguage.FR -> "Position GPS indisponible pour le moment"
                        LauncherLanguage.AR -> "تعذر تحديد موقع GPS حالياً"
                        LauncherLanguage.EN -> "GPS location currently unavailable"
                    }
                )
            }
            isRefreshingGpsFlow.value = false
        }
    }

    fun searchCities(query: String) {
        safeLaunch {
            val lang = uiState.value.settings.language.code
            citySearchResultsFlow.value = runCatching { locationWeatherRepo.searchCities(query, lang) }.getOrDefault(emptyList())
        }
    }

    fun selectCity(city: CitySearchResult) {
        safeLaunch {
            runCatching {
                prefsRepo.updateSettings { s ->
                    s.copy(
                        useGpsLocation = false,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        shortCityName = city.name,
                        cityName = city.fullDisplayName,
                        timezoneId = city.timezone ?: s.timezoneId,
                    )
                }
            }
            citySearchResultsFlow.value = emptyList()
            showStatus(city.name)
        }
    }

    fun navigateTo(destination: LauncherDestination) {
        destinationFlow.value = destination
    }

    fun navigateBack(): Boolean {
        return if (destinationFlow.value != LauncherDestination.HOME) {
            destinationFlow.value = when (destinationFlow.value) {
                LauncherDestination.WALLPAPER_STUDIO,
                LauncherDestination.UI_CUSTOMIZER,
                LauncherDestination.GESTURE_SHORTCUTS -> LauncherDestination.LAUNCHER_SETTINGS
                else -> LauncherDestination.HOME
            }
            true
        } else {
            false
        }
    }

    fun launchApp(app: AppItem) {
        val launched = appsRepo.launchApp(app.packageName)
        if (!launched) {
            showStatus("Impossible d'ouvrir ${app.label}")
        }
    }

    fun openAppInfo(app: AppItem) {
        contextAppFlow.value = null
        appsRepo.openAppInfo(app.packageName)
    }

    fun uninstallApp(app: AppItem, launcherContext: android.content.Context? = null) {
        contextAppFlow.value = null
        if (app.isSystemApp) {
            showStatus(LauncherStrings.get(uiState.value.settings.language).systemAppCannotUninstall)
            appsRepo.openAppInfo(app.packageName)
            return
        }
        val launched = appsRepo.uninstallApp(app.packageName, launcherContext)
        if (!launched) {
            showStatus(LauncherStrings.get(uiState.value.settings.language).systemAppCannotUninstall)
        }
    }

    fun openSystemSettings() {
        val ok = appsRepo.openSystemSettings()
        if (!ok) {
            showStatus(
                when (uiState.value.settings.language) {
                    LauncherLanguage.FR -> "Paramètres système inaccessibles sur cet appareil"
                    LauncherLanguage.AR -> "تعذر فتح إعدادات النظام على هذا الجهاز"
                    LauncherLanguage.EN -> "Unable to open system settings on this device"
                }
            )
        }
    }
    fun openNetworkSettings() = appsRepo.openNetworkSettings()

    fun isDefaultHomeLauncher(): Boolean = appsRepo.isDefaultHomeLauncher()

    fun getDefaultHomeIntent(): Intent? = appsRepo.getDefaultHomeRoleIntent()

    fun openDefaultHomeFallbackSettings(): Boolean = appsRepo.openDefaultHomeFallbackSettings()

    fun setContextApp(app: AppItem?) {
        contextAppFlow.value = app
    }

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        safeLaunch {
            runCatching {
                var latest: LauncherSettings? = null
                prefsRepo.updateSettings { current ->
                    transform(current).also { latest = it }
                }
                val updated = latest ?: uiState.value.settings
                prayerTimesFlow.value?.let { pt ->
                    adhanScheduler.scheduleDailyPrayers(pt, updated)
                }
            }
        }
    }

    fun toggleAiToolShortcut(storageKey: String) {
        val current = uiState.value.settings.aiToolShortcuts.take(4)
        if (!current.contains(storageKey) && current.size >= 4) {
            showStatus(LauncherStrings.get(uiState.value.settings.language).aiBarMaxReached)
            return
        }
        updateSettings { s ->
            val list = s.aiToolShortcuts.take(4).toMutableList()
            if (list.contains(storageKey)) {
                list.remove(storageKey)
            } else if (list.size < 4) {
                list.add(storageKey)
            }
            s.copy(aiToolShortcuts = list)
        }
    }

    fun launchAiTool(storageKey: String) {
        when {
            storageKey.startsWith("preset:") -> {
                val id = storageKey.removePrefix("preset:")
                val preset = AiToolPreset.byId(id) ?: return
                val launchedNative = appsRepo.launchApp(preset.packageName)
                if (!launchedNative) {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(preset.webUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val openedWeb = runCatching {
                        getApplication<Application>().startActivity(webIntent)
                        true
                    }.getOrDefault(false)
                    if (!openedWeb) {
                        showStatus(preset.label)
                    }
                }
            }
            storageKey.startsWith("app:") -> {
                val pkg = storageKey.removePrefix("app:")
                if (!appsRepo.launchApp(pkg)) {
                    showStatus(pkg)
                }
            }
        }
    }

    fun toggleFavorite(packageName: String) {
        updateSettings { s ->
            val current = s.favoritePackages.toMutableList()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            s.copy(favoritePackages = current)
        }
    }

    fun moveFavorite(packageName: String, delta: Int) {
        updateSettings { s ->
            val favs = (if (s.favoritePackages.isNotEmpty()) s.favoritePackages else uiState.value.favoriteApps.map { it.packageName }).toMutableList()
            val idx = favs.indexOf(packageName)
            if (idx == -1) return@updateSettings s
            val newIdx = (idx + delta).coerceIn(0, favs.lastIndex)
            if (idx != newIdx) {
                val item = favs.removeAt(idx)
                favs.add(newIdx, item)
            }
            s.copy(favoritePackages = favs)
        }
    }

    fun moveAppInOrder(packageName: String, delta: Int) {
        updateSettings { s ->
            val currentOrder = uiState.value.allApps.map { it.packageName }.toMutableList()
            val idx = currentOrder.indexOf(packageName)
            if (idx == -1) return@updateSettings s
            val newIdx = (idx + delta).coerceIn(0, currentOrder.lastIndex)
            if (idx != newIdx) {
                val item = currentOrder.removeAt(idx)
                currentOrder.add(newIdx, item)
            }
            s.copy(customAppOrder = currentOrder)
        }
    }

    fun toggleHiddenApp(packageName: String) {
        updateSettings { s ->
            val hidden = s.hiddenPackages.toMutableSet()
            if (hidden.contains(packageName)) {
                hidden.remove(packageName)
            } else {
                hidden.add(packageName)
            }
            val favs = s.favoritePackages.filterNot { hidden.contains(it) }
            s.copy(hiddenPackages = hidden, favoritePackages = favs)
        }
    }

    fun applyThemePreset(preset: ThemePreset) {
        updateSettings { s ->
            s.copy(
                themePreset = preset,
                accentColorHex = preset.accentHex,
                wallpaperId = preset.defaultWallpaperId,
                customWallpaperUri = null,
                customWallpaperIsVideo = false,
            )
        }
    }

    fun cycleLanguage() {
        updateSettings { s ->
            s.copy(language = s.language.next())
        }
    }

    fun followThemeReset() {
        updateSettings { s ->
            val preset = s.themePreset
            s.copy(
                appsPerRow = 7,
                rowsBelowFavorites = 0,
                enhancedContrast = true,
                animationsEnabled = true,
                advancedEffectsEnabled = true,
                accentColorHex = preset.accentHex,
                wallpaperId = preset.defaultWallpaperId,
                customWallpaperUri = null,
                customWallpaperIsVideo = false,
                wallpaperDimAlpha = 0.25f,
                glassOpacity = 0.56f,
                cardCornerRadiusDp = 18,
                dockStyle = DockStyle.FLOATING_GLASS,
                iconSize = AppIconSize.MEDIUM,
                iconShape = AppIconShape.RECTANGLE,
                navigationSoundEnabled = true,
                clockTheme = ClockTheme.CLASSIC_HORIZON,
            )
        }
        showStatus("Thème réinitialisé")
    }

    fun setCustomWallpaper(uri: Uri, forcedVideoHint: Boolean? = null) {
        val context = getApplication<Application>()
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        safeLaunch {
            val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty().lowercase()
            val fileName = resolveFileName(uri).orEmpty().lowercase()
            val isVideo = forcedVideoHint ?: (
                mimeType.startsWith("video/") ||
                    fileName.endsWith(".mp4") ||
                    fileName.endsWith(".mkv") ||
                    fileName.endsWith(".webm") ||
                    fileName.endsWith(".mov") ||
                    fileName.endsWith(".m4v") ||
                    fileName.endsWith(".avi") ||
                    fileName.endsWith(".3gp")
                )

            val savedUri = withContext(Dispatchers.IO) {
                runCatching {
                    val dir = File(context.filesDir, "wallpapers").apply { mkdirs() }
                    dir.listFiles()?.forEach { old -> runCatching { old.delete() } }
                    val ext = when {
                        fileName.contains(".") -> fileName.substringAfterLast(".").take(6).ifBlank { if (isVideo) "mp4" else "jpg" }
                        isVideo -> "mp4"
                        else -> "jpg"
                    }
                    val dest = File(dir, "custom_wallpaper_${System.currentTimeMillis()}.$ext")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { output ->
                            input.copyTo(output, bufferSize = 65536)
                        }
                    }
                    if (dest.exists() && dest.length() > 0L) Uri.fromFile(dest).toString() else uri.toString()
                }.getOrDefault(uri.toString())
            }

            prefsRepo.updateSettings { s ->
                s.copy(
                    wallpaperId = "custom",
                    customWallpaperUri = savedUri,
                    customWallpaperIsVideo = isVideo,
                )
            }

            val lang = uiState.value.settings.language
            showStatus(
                when {
                    isVideo && lang == LauncherLanguage.AR -> "تم تفعيل خلفية الفيديو"
                    isVideo && lang == LauncherLanguage.FR -> "Fond d'écran vidéo appliqué"
                    isVideo -> "Video wallpaper applied"
                    lang == LauncherLanguage.AR -> "تم تفعيل صورة الخلفية"
                    lang == LauncherLanguage.FR -> "Image de fond appliquée"
                    else -> "Image wallpaper applied"
                }
            )
        }
    }

    fun updateGestureShortcut(gesture: GestureType, action: GestureAction) {
        updateSettings { s ->
            val map = s.gestureShortcuts.toMutableMap()
            map[gesture] = action
            s.copy(gestureShortcuts = map)
        }
    }

    fun setCustomAdhanUri(uri: Uri) {
        val context = getApplication<Application>()
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        safeLaunch {
            val displayName = resolveFileName(uri) ?: "Adhan_Audio.mp3"
            val ext = displayName.substringAfterLast('.', "mp3")
                .lowercase()
                .take(6)
                .ifBlank { "mp3" }

            val savedUri = withContext(Dispatchers.IO) {
                runCatching {
                    val dir = File(context.filesDir, "adhan").apply { mkdirs() }
                    dir.listFiles()?.forEach { old -> runCatching { old.delete() } }
                    val dest = File(dir, "custom_adhan_${System.currentTimeMillis()}.$ext")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { output ->
                            input.copyTo(output, bufferSize = 32768)
                        }
                    }
                    if (dest.exists() && dest.length() > 0L) Uri.fromFile(dest).toString() else uri.toString()
                }.getOrDefault(uri.toString())
            }

            var latest: LauncherSettings? = null
            prefsRepo.updateSettings { s ->
                s.copy(
                    customAdhanAudioUri = savedUri,
                    customAdhanAudioName = displayName,
                    adhanEnabled = true,
                    adhanMode = AdhanAlertMode.AUDIO_AND_NOTIFICATION,
                ).also { latest = it }
            }
            val updated = latest ?: uiState.value.settings
            prayerTimesFlow.value?.let { pt ->
                adhanScheduler.scheduleDailyPrayers(pt, updated)
            }
            showStatus(displayName)
        }
    }

    fun clearCustomAdhanUri() {
        updateSettings { s ->
            s.copy(customAdhanAudioUri = null, customAdhanAudioName = null)
        }
    }

    fun togglePrayerEnabled(prayerKey: PrayerKey) {
        updateSettings { s ->
            val set = s.enabledPrayers.toMutableSet()
            if (set.contains(prayerKey)) set.remove(prayerKey) else set.add(prayerKey)
            s.copy(enabledPrayers = set)
        }
    }

    fun testOrStopAdhan() {
        val context = getApplication<Application>()
        if (AdhanPlaybackService.isPlaying.value) {
            AdhanPlaybackService.stopAdhan(context)
        } else {
            val s = uiState.value.settings
            val next = uiState.value.nextPrayer?.key?.labelFr ?: "Adhan"
            AdhanPlaybackService.startAdhan(
                context = context,
                prayerName = next,
                customUri = s.customAdhanAudioUri,
                cityName = s.shortCityName,
            )
        }
    }

    fun selectHdmiSource(source: String) {
        activeHdmiSourceFlow.value = source
        showStatus("Source : $source")
    }

    fun executeGesture(type: GestureType) {
        val action = uiState.value.settings.gestureShortcuts[type] ?: return
        when (action) {
            GestureAction.OPEN_APP_LIBRARY -> navigateTo(LauncherDestination.APP_LIBRARY)
            GestureAction.OPEN_SEARCH -> navigateTo(LauncherDestination.SEARCH_APPS)
            GestureAction.OPEN_SETTINGS -> navigateTo(LauncherDestination.SETTINGS_MENU)
            GestureAction.OPEN_WEATHER -> navigateTo(LauncherDestination.WEATHER_DETAIL)
            GestureAction.OPEN_PRAYER -> navigateTo(LauncherDestination.PRAYER_AND_ADHAN)
            GestureAction.OPEN_WALLPAPER_STUDIO -> navigateTo(LauncherDestination.WALLPAPER_STUDIO)
            GestureAction.OPEN_UI_CUSTOMIZER -> navigateTo(LauncherDestination.UI_CUSTOMIZER)
            GestureAction.TOGGLE_ADHAN_AUDIO -> testOrStopAdhan()
            GestureAction.REFRESH_GPS_DATA -> {
                if (locationWeatherRepo.hasLocationPermission()) {
                    syncWithGpsLocation()
                } else {
                    refreshPrayerAndWeather()
                }
            }
            GestureAction.NONE -> Unit
        }
    }

    fun openGithubRepoSetup() {
        updateStateFlow.value = AppUpdateUiState.RepoSetupRequired
    }

    fun saveGithubRepo(rawInput: String, checkImmediately: Boolean = true): Boolean {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            updateSettings { it.copy(githubUpdateRepo = "") }
            updateStateFlow.value = AppUpdateUiState.Idle
            return true
        }
        val normalized = GitHubUpdateRepository.normalizeRepoSlug(trimmed) ?: return false
        updateSettings { it.copy(githubUpdateRepo = normalized) }
        updateStateFlow.value = AppUpdateUiState.Idle
        if (checkImmediately) {
            val updated = uiState.value.settings.copy(githubUpdateRepo = normalized)
            checkForUpdates(silent = false, overrideSettings = updated)
        }
        return true
    }

    fun checkForUpdates(silent: Boolean = false, overrideSettings: LauncherSettings? = null) {
        val s = overrideSettings ?: uiState.value.settings
        val repoSlug = s.githubUpdateRepo.trim()
        if (repoSlug.isBlank()) {
            if (!silent) {
                updateStateFlow.value = AppUpdateUiState.RepoSetupRequired
            }
            return
        }
        if (updateStateFlow.value is AppUpdateUiState.Checking ||
            updateStateFlow.value is AppUpdateUiState.Downloading
        ) {
            return
        }

        val strings = LauncherStrings.get(s.language)
        if (!silent) {
            updateStateFlow.value = AppUpdateUiState.Checking
        }

        safeLaunch {
            when (val res = updateRepo.checkLatestRelease(repoSlug, com.example.BuildConfig.VERSION_NAME)) {
                is UpdateCheckResult.UpdateAvailable -> {
                    updateStateFlow.value = AppUpdateUiState.Available(res.info)
                }
                is UpdateCheckResult.UpToDate -> {
                    if (!silent) {
                        updateStateFlow.value = AppUpdateUiState.Idle
                        showStatus("Horizon v${com.example.BuildConfig.VERSION_NAME} · ${strings.updateUpToDateStatus}")
                    }
                }
                UpdateCheckResult.RepoNotConfigured -> {
                    if (!silent) {
                        updateStateFlow.value = AppUpdateUiState.RepoSetupRequired
                    }
                }
                is UpdateCheckResult.NoApkAssetFound -> {
                    if (!silent) {
                        updateStateFlow.value = AppUpdateUiState.Idle
                        showStatus("${res.latestTag} · ${strings.updateNoApkStatus}")
                    }
                }
                is UpdateCheckResult.Error -> {
                    if (!silent) {
                        updateStateFlow.value = AppUpdateUiState.Idle
                        showStatus("${strings.updateErrorStatus} (${res.message})")
                    }
                }
            }
        }
    }

    fun downloadAndInstallUpdate() {
        val current = updateStateFlow.value
        val info = when (current) {
            is AppUpdateUiState.Available -> current.info
            is AppUpdateUiState.ReadyToInstall -> current.info
            else -> return
        }
        val strings = LauncherStrings.get(uiState.value.settings.language)
        updateStateFlow.value = AppUpdateUiState.Downloading(
            info = info,
            progressPercent = 0,
            downloadedBytes = 0L,
            totalBytes = info.apkSizeBytes,
        )
        safeLaunch {
            val result = updateRepo.downloadApk(info) { percent, downloaded, total ->
                updateStateFlow.value = AppUpdateUiState.Downloading(
                    info = info,
                    progressPercent = percent,
                    downloadedBytes = downloaded,
                    totalBytes = total,
                )
            }
            result.onSuccess { file ->
                val canInstall = updateRepo.canInstallPackages()
                updateStateFlow.value = AppUpdateUiState.ReadyToInstall(
                    info = info,
                    apkFilePath = file.absolutePath,
                    needsInstallPermission = !canInstall,
                )
                if (canInstall) {
                    updateRepo.launchApkInstaller(file.absolutePath)
                }
            }.onFailure { err ->
                updateStateFlow.value = AppUpdateUiState.Available(info)
                showStatus("${strings.updateErrorStatus} (${err.localizedMessage ?: "Download"})")
            }
        }
    }

    fun retryInstallDownloadedApk() {
        val current = updateStateFlow.value as? AppUpdateUiState.ReadyToInstall ?: return
        val canInstall = updateRepo.canInstallPackages()
        updateStateFlow.value = current.copy(needsInstallPermission = !canInstall)
        if (canInstall) {
            updateRepo.launchApkInstaller(current.apkFilePath)
        }
    }

    fun getUnknownSourcesSettingsIntent(): Intent = updateRepo.createUnknownSourcesSettingsIntent()

    fun dismissUpdateDialog() {
        updateStateFlow.value = AppUpdateUiState.Idle
    }

    fun showStatus(msg: String) {
        statusMessageFlow.value = msg
    }

    fun clearStatus() {
        statusMessageFlow.value = null
    }

    private fun resolveFileName(uri: Uri): String? {
        val cr = getApplication<Application>().contentResolver
        return runCatching {
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        }.getOrNull() ?: uri.lastPathSegment
    }

    private fun applyCustomOrder(apps: List<AppItem>, order: List<String>): List<AppItem> {
        if (order.isEmpty()) return apps
        val rank = order.withIndex().associate { it.value to it.index }
        return apps.sortedWith(compareBy<AppItem> { rank[it.packageName] ?: Int.MAX_VALUE }.thenBy { it.label.lowercase() })
    }
}
