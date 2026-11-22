package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import com.example.ui.icons.AudioFile
import com.example.ui.icons.GpsFixed
import com.example.ui.icons.Mosque
import com.example.ui.icons.MyLocation
import com.example.ui.icons.NotificationsActive
import com.example.ui.icons.Stop
import com.example.ui.icons.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AdhanAlertMode
import com.example.model.CitySearchResult
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.LauncherStrings
import com.example.model.PrayerKey
import com.example.ui.LauncherUiState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import com.example.ui.components.GlassSegmentedSelector
import com.example.ui.components.GlassToggleRow
import com.example.ui.components.TvFocusableSurface
import com.example.ui.components.weatherIconForCode
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import kotlin.math.roundToInt

@Composable
fun WeatherDetailScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onSyncGps: () -> Unit,
    onRefreshWeather: () -> Unit,
    onSearchCity: (String) -> Unit,
    onSelectCity: (CitySearchResult) -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor
    var cityQuery by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { grants ->
            val granted = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) onSyncGps()
        }
    )

    fun triggerGpsSync() {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (coarse == PackageManager.PERMISSION_GRANTED || fine == PackageManager.PERMISSION_GRANTED) {
            onSyncGps()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        }
    }

    fun formatTemp(tempC: Double): String {
        val v = if (settings.useCelsius) tempC else (tempC * 9.0 / 5.0 + 32.0)
        return "${v.roundToInt()}°${if (settings.useCelsius) "C" else "F"}"
    }

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
            val weatherBackFocus = remember { FocusRequester() }
            LaunchedEffect(Unit) { runCatching { weatherBackFocus.requestFocus() } }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TvFocusableSurface(
                        onClick = onBack,
                        accentColor = accent,
                        shape = RoundedCornerShape(12.dp),
                        baseColor = Color.White.copy(alpha = 0.08f),
                        focusRequester = weatherBackFocus,
                        modifier = Modifier.size(48.dp).testTag("weather_back_btn"),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backLabel, tint = PureWhite)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(strings.weatherAndGpsTitle, style = MaterialTheme.typography.headlineLarge, color = PureWhite)
                }
                TvFocusableSurface(
                    onClick = onRefreshWeather,
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.size(48.dp).testTag("weather_refresh_btn"),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = PureWhite)
                    }
                }
            }

            // GPS Sync & Unit Row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TvFocusableSurface(
                    onClick = { triggerGpsSync() },
                    accentColor = accent,
                    shape = RoundedCornerShape(14.dp),
                    baseColor = accent,
                    focusedColor = PureWhite,
                    modifier = Modifier.weight(1f).height(50.dp).testTag("weather_gps_sync_btn"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (state.isRefreshingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DeepSpaceBlack, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = DeepSpaceBlack)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.gpsSyncButton, style = MaterialTheme.typography.labelLarge, color = DeepSpaceBlack)
                    }
                }

                TvFocusableSurface(
                    onClick = { onUpdateSettings { it.copy(useCelsius = !it.useCelsius) } },
                    accentColor = accent,
                    shape = RoundedCornerShape(14.dp),
                    baseColor = GlassBlue.copy(alpha = 0.55f),
                    baseBorderColor = accent,
                    modifier = Modifier.width(72.dp).height(50.dp).testTag("weather_unit_toggle"),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(if (settings.useCelsius) "°C" else "°F", color = PureWhite, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Manual City Search
            CitySearchSection(
                query = cityQuery,
                onQueryChange = {
                    cityQuery = it
                    onSearchCity(it)
                },
                hint = strings.searchCityHint,
                results = state.citySearchResults,
                accent = accent,
                onSelectCity = {
                    cityQuery = ""
                    onSelectCity(it)
                },
            )

            // Current Weather Hero Card
            val w = state.weather
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = GlassBlue.copy(alpha = 0.62f),
                border = BorderStroke(1.2.dp, accent.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (w == null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    ) {
                        if (state.isLoadingWeather) {
                            CircularProgressIndicator(color = accent)
                        } else {
                            Text("Météo indisponible hors ligne", color = MutedSlate)
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.GpsFixed,
                                        contentDescription = null,
                                        tint = if (w.isGpsSynced) EmeraldGlow else accent,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(settings.cityName, style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatTemp(w.temperatureC),
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                                    color = PureWhite,
                                )
                                Text(
                                    text = LauncherStrings.weatherConditionLabel(w.weatherCode, settings.language),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = accent,
                                )
                            }
                            Icon(
                                imageVector = weatherIconForCode(w.weatherCode, w.isDay),
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(72.dp),
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            WeatherStatPill("Ressenti", formatTemp(w.apparentTemperatureC), Modifier.weight(1f))
                            WeatherStatPill("Vent", "${w.windSpeedKmh.roundToInt()} km/h", Modifier.weight(1f))
                            WeatherStatPill("Humidité", "${w.humidityPercent}%", Modifier.weight(1f))
                            WeatherStatPill("Min / Max", "${w.tempMinC.roundToInt()}° / ${w.tempMaxC.roundToInt()}°", Modifier.weight(1f))
                        }
                    }
                }
            }

            if (w != null && w.hourly.isNotEmpty()) {
                Text("Prévisions horaires", style = MaterialTheme.typography.titleMedium, color = accent)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(w.hourly) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GlassBlue.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Text(item.hourLabel, style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
                                Spacer(modifier = Modifier.height(6.dp))
                                Icon(
                                    imageVector = weatherIconForCode(item.weatherCode, true),
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(formatTemp(item.temperatureC), style = MaterialTheme.typography.titleMedium, color = PureWhite)
                            }
                        }
                    }
                }
            }

            if (w != null && w.daily.isNotEmpty()) {
                Text("Prochains jours", style = MaterialTheme.typography.titleMedium, color = accent)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    w.daily.forEach { day ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GlassBlue.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            ) {
                                Text(day.dayLabel, style = MaterialTheme.typography.titleMedium, color = PureWhite, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = weatherIconForCode(day.weatherCode, true),
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(modifier = Modifier.width(18.dp))
                                Text(
                                    text = "${formatTemp(day.tempMinC)}  /  ${formatTemp(day.tempMaxC)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PrayerAndAdhanScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onSyncGps: () -> Unit,
    onSearchCity: (String) -> Unit,
    onSelectCity: (CitySearchResult) -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onTogglePrayer: (PrayerKey) -> Unit,
    onCustomAdhanPicked: (Uri) -> Unit,
    onClearCustomAdhan: () -> Unit,
    onTestOrStopAdhan: () -> Unit,
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor
    var cityQuery by remember { mutableStateOf("") }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) onCustomAdhanPicked(uri)
        }
    )

    val audioGetContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) onCustomAdhanPicked(uri)
        }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* notification permission handled */ }
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { grants ->
            val granted = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) onSyncGps()
        }
    )

    fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
            val prayerBackFocus = remember { FocusRequester() }
            LaunchedEffect(Unit) { runCatching { prayerBackFocus.requestFocus() } }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TvFocusableSurface(
                    onClick = onBack,
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    focusRequester = prayerBackFocus,
                    modifier = Modifier.size(48.dp).testTag("prayer_back_btn"),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backLabel, tint = PureWhite)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(strings.prayerAndAdhanTitle, style = MaterialTheme.typography.headlineLarge, color = PureWhite)
            }

            // Custom Device Audio Picker + Test Player (immediately visible at top for TV/mobile)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GlassBlue.copy(alpha = 0.62f),
                border = BorderStroke(1.2.dp, accent.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth().testTag("adhan_audio_upload_card"),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AudioFile, contentDescription = null, tint = accent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.adhanAudioFileTitle, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                            Text(
                                text = settings.customAdhanAudioName ?: strings.adhanDefaultChimeLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate,
                            )
                        }
                        if (settings.customAdhanAudioUri != null) {
                            IconButton(onClick = onClearCustomAdhan) {
                                Icon(Icons.Rounded.Close, contentDescription = "Reset audio", tint = MutedSlate)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        TvFocusableSurface(
                            onClick = {
                                val launchedAudio = runCatching {
                                    audioPickerLauncher.launch(arrayOf("audio/*"))
                                }.isSuccess
                                if (!launchedAudio) {
                                    val launchedAny = runCatching {
                                        audioPickerLauncher.launch(arrayOf("*/*"))
                                    }.isSuccess
                                    if (!launchedAny) {
                                        runCatching { audioGetContentLauncher.launch("audio/*") }
                                    }
                                }
                            },
                            accentColor = accent,
                            shape = RoundedCornerShape(14.dp),
                            baseColor = accent,
                            focusedColor = PureWhite,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("pick_adhan_audio_btn"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            ) {
                                Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = DeepSpaceBlack)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.selectCustomAdhan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = DeepSpaceBlack,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = onTestOrStopAdhan,
                            accentColor = if (state.isAdhanPlaying) EmeraldGlow else accent,
                            shape = RoundedCornerShape(14.dp),
                            baseColor = GlassBlue.copy(alpha = 0.6f),
                            baseBorderColor = if (state.isAdhanPlaying) EmeraldGlow else accent,
                            modifier = Modifier.height(48.dp).testTag("test_adhan_btn"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                            ) {
                                Icon(
                                    imageVector = if (state.isAdhanPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = if (state.isAdhanPlaying) EmeraldGlow else accent,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isAdhanPlaying) strings.stopAdhan else strings.testAdhan,
                                    maxLines = 1,
                                    color = PureWhite,
                                )
                            }
                        }
                    }
                }
            }

            // Adhan Audio & Notification Auto-Play Settings
            GlassToggleRow(
                title = strings.enableAdhanAlerts,
                subtitle = strings.enableAdhanAlertsDesc,
                checked = settings.adhanEnabled,
                accentColor = accent,
                onCheckedChange = { enabled ->
                    if (enabled) requestNotificationsIfNeeded()
                    onUpdateSettings { it.copy(adhanEnabled = enabled) }
                },
                testTag = "adhan_master_toggle",
            )

            Text(strings.adhanAlertModeTitle, style = MaterialTheme.typography.titleMedium, color = accent)
            GlassSegmentedSelector(
                options = AdhanAlertMode.entries,
                selected = settings.adhanMode,
                labelProvider = {
                    when (it) {
                        AdhanAlertMode.SILENT -> strings.adhanModeSilent
                        AdhanAlertMode.NOTIFICATION_ONLY -> strings.adhanModeNotification
                        AdhanAlertMode.AUDIO_AND_NOTIFICATION -> strings.adhanModeAudio
                    }
                },
                accentColor = accent,
                onSelect = { mode ->
                    if (mode != AdhanAlertMode.SILENT) requestNotificationsIfNeeded()
                    onUpdateSettings { it.copy(adhanMode = mode, adhanEnabled = mode != AdhanAlertMode.SILENT) }
                },
                testTagPrefix = "adhan_mode",
            )

            // GPS sync + City Search
            TvFocusableSurface(
                onClick = {
                    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    if (coarse == PackageManager.PERMISSION_GRANTED || fine == PackageManager.PERMISSION_GRANTED) {
                        onSyncGps()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            )
                        )
                    }
                },
                accentColor = accent,
                shape = RoundedCornerShape(14.dp),
                baseColor = accent,
                focusedColor = PureWhite,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("prayer_gps_sync_btn"),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = DeepSpaceBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${strings.gpsSyncButton} · ${settings.shortCityName}", style = MaterialTheme.typography.labelLarge, color = DeepSpaceBlack)
                }
            }

            CitySearchSection(
                query = cityQuery,
                onQueryChange = {
                    cityQuery = it
                    onSearchCity(it)
                },
                hint = strings.searchCityHint,
                results = state.citySearchResults,
                accent = accent,
                onSelectCity = {
                    cityQuery = ""
                    onSelectCity(it)
                },
            )

            // Daily Prayer Times Card
            val pt = state.prayerTimes
            if (pt != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GlassBlue.copy(alpha = 0.62f),
                    border = BorderStroke(1.2.dp, accent.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Mosque, contentDescription = null, tint = accent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(settings.cityName, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                            }
                            if (pt.hijriDate.isNotBlank()) {
                                Text(pt.hijriDate, style = MaterialTheme.typography.bodyMedium, color = accent)
                            }
                        }

                        Text(
                            text = "Lever du soleil (Chorouq) : ${pt.sunrise}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate,
                        )

                        pt.entries.forEach { entry ->
                            val isNext = state.nextPrayer?.key == entry.key
                            val alertEnabled = settings.enabledPrayers.contains(entry.key)
                            val label = when (settings.language) {
                                LauncherLanguage.FR -> "${entry.key.labelFr} (${entry.key.labelAr})"
                                LauncherLanguage.AR -> entry.key.labelAr
                                LauncherLanguage.EN -> "${entry.key.labelEn} (${entry.key.labelAr})"
                            }
                            TvFocusableSurface(
                                onClick = {
                                    requestNotificationsIfNeeded()
                                    onTogglePrayer(entry.key)
                                },
                                accentColor = accent,
                                shape = RoundedCornerShape(14.dp),
                                baseColor = Color.White.copy(alpha = 0.06f),
                                isSelected = isNext,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = PureWhite,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text = entry.time24,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (isNext) accent else PureWhite,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Switch(
                                        checked = alertEnabled,
                                        onCheckedChange = null,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PureWhite,
                                            checkedTrackColor = accent,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CitySearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    results: List<CitySearchResult>,
    accent: Color,
    onSelectCity: (CitySearchResult) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(hint, color = MutedSlate) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = accent) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedContainerColor = GlassBlue.copy(alpha = 0.45f),
                unfocusedContainerColor = GlassBlue.copy(alpha = 0.35f),
            ),
            modifier = Modifier.fillMaxWidth().testTag("city_search_input"),
        )

        if (results.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xEE0D2352),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    results.forEach { city ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectCity(city) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(city.fullDisplayName, style = MaterialTheme.typography.bodyLarge, color = PureWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherStatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedSlate)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = PureWhite)
        }
    }
}
