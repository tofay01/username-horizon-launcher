package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AdhanAlertMode
import com.example.model.AppIconShape
import com.example.model.AppIconSize
import com.example.model.ClockTheme
import com.example.model.DockStyle
import com.example.model.GestureAction
import com.example.model.GestureType
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.PrayerKey
import com.example.model.ThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "horizon_launcher_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val APPS_PER_ROW = intPreferencesKey("apps_per_row")
        val ROWS_BELOW_FAVORITES = intPreferencesKey("rows_below_favorites")
        val ENHANCED_CONTRAST = booleanPreferencesKey("enhanced_contrast")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_GREETING = booleanPreferencesKey("show_greeting")
        val CLOCK_THEME = stringPreferencesKey("clock_theme")
        val CUSTOM_SECTION_TITLE = stringPreferencesKey("custom_section_title")
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val ACCENT_COLOR_HEX = longPreferencesKey("accent_color_hex")
        val WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val CUSTOM_WALLPAPER_IS_VIDEO = booleanPreferencesKey("custom_wallpaper_is_video")
        val WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
        val GLASS_OPACITY = floatPreferencesKey("glass_opacity")
        val CARD_CORNER_RADIUS = intPreferencesKey("card_corner_radius")
        val DOCK_STYLE = stringPreferencesKey("dock_style")
        val DOCK_SCALE_PERCENT = intPreferencesKey("dock_scale_percent")
        val ICON_SIZE = stringPreferencesKey("icon_size")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val NAVIGATION_SOUND_ENABLED = booleanPreferencesKey("navigation_sound_enabled")
        val SHOW_WEATHER_WIDGET = booleanPreferencesKey("show_weather_widget")
        val SHOW_PRAYER_WIDGET = booleanPreferencesKey("show_prayer_widget")
        val SHOW_CALENDAR_WIDGET = booleanPreferencesKey("show_calendar_widget")
        val USE_CELSIUS = booleanPreferencesKey("use_celsius")
        val USE_GPS_LOCATION = booleanPreferencesKey("use_gps_location")
        val CITY_NAME = stringPreferencesKey("city_name")
        val SHORT_CITY_NAME = stringPreferencesKey("short_city_name")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val TIMEZONE_ID = stringPreferencesKey("timezone_id")
        val PRAYER_METHOD_ID = intPreferencesKey("prayer_method_id")
        val ADHAN_ENABLED = booleanPreferencesKey("adhan_enabled")
        val ADHAN_MODE = stringPreferencesKey("adhan_mode")
        val CUSTOM_ADHAN_URI = stringPreferencesKey("custom_adhan_uri")
        val CUSTOM_ADHAN_NAME = stringPreferencesKey("custom_adhan_name")
        val ENABLED_PRAYERS = stringPreferencesKey("enabled_prayers")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val ADVANCED_EFFECTS_ENABLED = booleanPreferencesKey("advanced_effects_enabled")
        val SHOW_HDMI_BAR = booleanPreferencesKey("show_hdmi_bar")
        val DEVELOPER_NAME = stringPreferencesKey("developer_name")
        val DEVELOPER_EMAIL = stringPreferencesKey("developer_email")
        val DEVELOPER_WHATSAPP = stringPreferencesKey("developer_whatsapp")
        val GITHUB_UPDATE_REPO = stringPreferencesKey("github_update_repo")
        val AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val FAVORITE_PACKAGES = stringPreferencesKey("favorite_packages")
        val AI_TOOL_SHORTCUTS = stringPreferencesKey("ai_tool_shortcuts")
        val CUSTOM_APP_ORDER = stringPreferencesKey("custom_app_order")
        val HIDDEN_PACKAGES = stringPreferencesKey("hidden_packages")
        val GESTURE_MAP = stringPreferencesKey("gesture_map")
        val MIGRATED_SEVEN_PER_ROW = booleanPreferencesKey("migrated_7_per_row")
    }

    val settingsFlow: Flow<LauncherSettings> = context.launcherDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> mapFromPrefs(prefs) }

    suspend fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        context.launcherDataStore.edit { prefs ->
            val current = mapFromPrefs(prefs)
            val updated = transform(current)
            prefs[Keys.LANGUAGE] = updated.language.code
            prefs[Keys.APPS_PER_ROW] = updated.appsPerRow
            prefs[Keys.MIGRATED_SEVEN_PER_ROW] = true
            prefs[Keys.ROWS_BELOW_FAVORITES] = updated.rowsBelowFavorites
            prefs[Keys.ENHANCED_CONTRAST] = updated.enhancedContrast
            prefs[Keys.IS_24_HOUR] = updated.is24HourFormat
            prefs[Keys.SHOW_DATE] = updated.showDate
            prefs[Keys.SHOW_GREETING] = updated.showGreeting
            prefs[Keys.CLOCK_THEME] = updated.clockTheme.id
            prefs[Keys.CUSTOM_SECTION_TITLE] = updated.customSectionTitle
            prefs[Keys.THEME_PRESET] = updated.themePreset.id
            prefs[Keys.ACCENT_COLOR_HEX] = updated.accentColorHex
            prefs[Keys.WALLPAPER_ID] = updated.wallpaperId
            prefs[Keys.CUSTOM_WALLPAPER_URI] = updated.customWallpaperUri ?: ""
            prefs[Keys.CUSTOM_WALLPAPER_IS_VIDEO] = updated.customWallpaperIsVideo
            prefs[Keys.WALLPAPER_DIM] = updated.wallpaperDimAlpha
            prefs[Keys.GLASS_OPACITY] = updated.glassOpacity
            prefs[Keys.CARD_CORNER_RADIUS] = updated.cardCornerRadiusDp
            prefs[Keys.DOCK_STYLE] = updated.dockStyle.id
            prefs[Keys.DOCK_SCALE_PERCENT] = updated.dockScalePercent.coerceIn(60, 130)
            prefs[Keys.ICON_SIZE] = updated.iconSize.id
            prefs[Keys.ICON_SHAPE] = updated.iconShape.id
            prefs[Keys.NAVIGATION_SOUND_ENABLED] = updated.navigationSoundEnabled
            prefs[Keys.SHOW_WEATHER_WIDGET] = updated.showWeatherWidget
            prefs[Keys.SHOW_PRAYER_WIDGET] = updated.showPrayerWidget
            prefs[Keys.SHOW_CALENDAR_WIDGET] = updated.showCalendarWidget
            prefs[Keys.USE_CELSIUS] = updated.useCelsius
            prefs[Keys.USE_GPS_LOCATION] = updated.useGpsLocation
            prefs[Keys.CITY_NAME] = updated.cityName
            prefs[Keys.SHORT_CITY_NAME] = updated.shortCityName
            prefs[Keys.LATITUDE] = updated.latitude
            prefs[Keys.LONGITUDE] = updated.longitude
            prefs[Keys.TIMEZONE_ID] = updated.timezoneId
            prefs[Keys.PRAYER_METHOD_ID] = updated.prayerMethodId
            prefs[Keys.ADHAN_ENABLED] = updated.adhanEnabled
            prefs[Keys.ADHAN_MODE] = updated.adhanMode.id
            prefs[Keys.CUSTOM_ADHAN_URI] = updated.customAdhanAudioUri ?: ""
            prefs[Keys.CUSTOM_ADHAN_NAME] = updated.customAdhanAudioName ?: ""
            prefs[Keys.ENABLED_PRAYERS] = updated.enabledPrayers.joinToString(",") { it.id }
            prefs[Keys.ANIMATIONS_ENABLED] = updated.animationsEnabled
            prefs[Keys.ADVANCED_EFFECTS_ENABLED] = updated.advancedEffectsEnabled
            prefs[Keys.SHOW_HDMI_BAR] = updated.showHdmiSourcesBar
            prefs[Keys.DEVELOPER_NAME] = updated.developerName
            prefs[Keys.DEVELOPER_EMAIL] = updated.developerEmail
            prefs[Keys.DEVELOPER_WHATSAPP] = updated.developerWhatsApp
            prefs[Keys.GITHUB_UPDATE_REPO] = updated.githubUpdateRepo
            prefs[Keys.AUTO_CHECK_UPDATES] = updated.autoCheckUpdatesOnStartup
            prefs[Keys.FAVORITE_PACKAGES] = updated.favoritePackages.joinToString("|")
            prefs[Keys.AI_TOOL_SHORTCUTS] = updated.aiToolShortcuts.joinToString("|")
            prefs[Keys.CUSTOM_APP_ORDER] = updated.customAppOrder.joinToString("|")
            prefs[Keys.HIDDEN_PACKAGES] = updated.hiddenPackages.joinToString("|")
            prefs[Keys.GESTURE_MAP] = updated.gestureShortcuts.entries.joinToString(";") { "${it.key.key}=${it.value.key}" }
        }
    }

    private fun mapFromPrefs(prefs: Preferences): LauncherSettings {
        val defaultSettings = LauncherSettings()
        val language = prefs[Keys.LANGUAGE]?.let { code ->
            LauncherLanguage.entries.firstOrNull { it.code == code }
        } ?: defaultSettings.language
        val themePreset = prefs[Keys.THEME_PRESET]?.let { id ->
            ThemePreset.entries.firstOrNull { it.id == id }
        } ?: defaultSettings.themePreset
        val dockStyle = prefs[Keys.DOCK_STYLE]?.let { id ->
            DockStyle.entries.firstOrNull { it.id == id }
        } ?: defaultSettings.dockStyle
        val iconSize = prefs[Keys.ICON_SIZE]?.let { id ->
            AppIconSize.entries.firstOrNull { it.id == id }
        } ?: defaultSettings.iconSize
        val iconShape = prefs[Keys.ICON_SHAPE]?.let { id ->
            AppIconShape.entries.firstOrNull { it.id == id }
        } ?: defaultSettings.iconShape
        val adhanMode = prefs[Keys.ADHAN_MODE]?.let { id ->
            AdhanAlertMode.entries.firstOrNull { it.id == id }
        } ?: defaultSettings.adhanMode
        val clockTheme = prefs[Keys.CLOCK_THEME]?.let { id ->
            ClockTheme.entries.firstOrNull { it.id == id }
        } ?: defaultSettings.clockTheme
        val enabledPrayers = prefs[Keys.ENABLED_PRAYERS]?.let { raw ->
            if (raw.isBlank()) emptySet()
            else raw.split(",").mapNotNull { id -> PrayerKey.entries.firstOrNull { it.id == id } }.toSet()
        } ?: defaultSettings.enabledPrayers
        val favorites = prefs[Keys.FAVORITE_PACKAGES]?.let { raw ->
            if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
        } ?: emptyList()
        val aiTools = prefs[Keys.AI_TOOL_SHORTCUTS]?.let { raw ->
            if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }.take(4)
        } ?: emptyList()
        val appOrder = prefs[Keys.CUSTOM_APP_ORDER]?.let { raw ->
            if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
        } ?: emptyList()
        val hidden = prefs[Keys.HIDDEN_PACKAGES]?.let { raw ->
            if (raw.isBlank()) emptySet() else raw.split("|").filter { it.isNotBlank() }.toSet()
        } ?: emptySet()
        val gestures = prefs[Keys.GESTURE_MAP]?.let { raw ->
            val parsed = mutableMapOf<GestureType, GestureAction>()
            parsed.putAll(defaultSettings.gestureShortcuts)
            raw.split(";").forEach { pair ->
                val parts = pair.split("=")
                if (parts.size == 2) {
                    val g = GestureType.entries.firstOrNull { it.key == parts[0] }
                    val a = GestureAction.entries.firstOrNull { it.key == parts[1] }
                    if (g != null && a != null) parsed[g] = a
                }
            }
            parsed
        } ?: defaultSettings.gestureShortcuts

        val rawAppsPerRow = prefs[Keys.APPS_PER_ROW]
        val resolvedAppsPerRow = when {
            rawAppsPerRow == null -> defaultSettings.appsPerRow
            rawAppsPerRow == 6 && prefs[Keys.MIGRATED_SEVEN_PER_ROW] != true -> 7
            else -> rawAppsPerRow.coerceIn(4, 7)
        }

        return LauncherSettings(
            language = language,
            appsPerRow = resolvedAppsPerRow,
            rowsBelowFavorites = (prefs[Keys.ROWS_BELOW_FAVORITES] ?: defaultSettings.rowsBelowFavorites).coerceIn(0, 3),
            enhancedContrast = prefs[Keys.ENHANCED_CONTRAST] ?: defaultSettings.enhancedContrast,
            is24HourFormat = prefs[Keys.IS_24_HOUR] ?: defaultSettings.is24HourFormat,
            showDate = prefs[Keys.SHOW_DATE] ?: defaultSettings.showDate,
            showGreeting = prefs[Keys.SHOW_GREETING] ?: defaultSettings.showGreeting,
            clockTheme = clockTheme,
            customSectionTitle = prefs[Keys.CUSTOM_SECTION_TITLE] ?: defaultSettings.customSectionTitle,
            themePreset = themePreset,
            accentColorHex = prefs[Keys.ACCENT_COLOR_HEX] ?: defaultSettings.accentColorHex,
            wallpaperId = prefs[Keys.WALLPAPER_ID] ?: defaultSettings.wallpaperId,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI]?.takeIf { it.isNotBlank() },
            customWallpaperIsVideo = prefs[Keys.CUSTOM_WALLPAPER_IS_VIDEO] ?: defaultSettings.customWallpaperIsVideo,
            wallpaperDimAlpha = prefs[Keys.WALLPAPER_DIM] ?: defaultSettings.wallpaperDimAlpha,
            glassOpacity = prefs[Keys.GLASS_OPACITY] ?: defaultSettings.glassOpacity,
            cardCornerRadiusDp = prefs[Keys.CARD_CORNER_RADIUS] ?: defaultSettings.cardCornerRadiusDp,
            dockStyle = dockStyle,
            dockScalePercent = (prefs[Keys.DOCK_SCALE_PERCENT] ?: defaultSettings.dockScalePercent).coerceIn(60, 130),
            iconSize = iconSize,
            iconShape = iconShape,
            navigationSoundEnabled = prefs[Keys.NAVIGATION_SOUND_ENABLED] ?: defaultSettings.navigationSoundEnabled,
            showWeatherWidget = prefs[Keys.SHOW_WEATHER_WIDGET] ?: defaultSettings.showWeatherWidget,
            showPrayerWidget = prefs[Keys.SHOW_PRAYER_WIDGET] ?: defaultSettings.showPrayerWidget,
            showCalendarWidget = prefs[Keys.SHOW_CALENDAR_WIDGET] ?: defaultSettings.showCalendarWidget,
            useCelsius = prefs[Keys.USE_CELSIUS] ?: defaultSettings.useCelsius,
            useGpsLocation = prefs[Keys.USE_GPS_LOCATION] ?: defaultSettings.useGpsLocation,
            cityName = prefs[Keys.CITY_NAME] ?: defaultSettings.cityName,
            shortCityName = prefs[Keys.SHORT_CITY_NAME] ?: defaultSettings.shortCityName,
            latitude = prefs[Keys.LATITUDE] ?: defaultSettings.latitude,
            longitude = prefs[Keys.LONGITUDE] ?: defaultSettings.longitude,
            timezoneId = prefs[Keys.TIMEZONE_ID] ?: defaultSettings.timezoneId,
            prayerMethodId = prefs[Keys.PRAYER_METHOD_ID] ?: defaultSettings.prayerMethodId,
            adhanEnabled = prefs[Keys.ADHAN_ENABLED] ?: defaultSettings.adhanEnabled,
            adhanMode = adhanMode,
            customAdhanAudioUri = prefs[Keys.CUSTOM_ADHAN_URI]?.takeIf { it.isNotBlank() },
            customAdhanAudioName = prefs[Keys.CUSTOM_ADHAN_NAME]?.takeIf { it.isNotBlank() },
            enabledPrayers = enabledPrayers,
            animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: defaultSettings.animationsEnabled,
            advancedEffectsEnabled = prefs[Keys.ADVANCED_EFFECTS_ENABLED] ?: defaultSettings.advancedEffectsEnabled,
            showHdmiSourcesBar = prefs[Keys.SHOW_HDMI_BAR] ?: defaultSettings.showHdmiSourcesBar,
            developerName = prefs[Keys.DEVELOPER_NAME]?.takeIf { it.isNotBlank() } ?: defaultSettings.developerName,
            developerEmail = prefs[Keys.DEVELOPER_EMAIL]?.takeIf { it.isNotBlank() } ?: defaultSettings.developerEmail,
            developerWhatsApp = prefs[Keys.DEVELOPER_WHATSAPP] ?: defaultSettings.developerWhatsApp,
            githubUpdateRepo = prefs[Keys.GITHUB_UPDATE_REPO]?.takeIf { it.isNotBlank() } ?: defaultSettings.githubUpdateRepo,
            autoCheckUpdatesOnStartup = prefs[Keys.AUTO_CHECK_UPDATES] ?: defaultSettings.autoCheckUpdatesOnStartup,
            favoritePackages = favorites,
            aiToolShortcuts = aiTools,
            customAppOrder = appOrder,
            hiddenPackages = hidden,
            gestureShortcuts = gestures,
        )
    }
}
