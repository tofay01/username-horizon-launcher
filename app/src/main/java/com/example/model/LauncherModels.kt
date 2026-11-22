package com.example.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color

enum class LauncherLanguage(val code: String, val displayName: String, val circleCode: String) {
    EN("en", "English", "EN"),
    FR("fr", "Français", "FR"),
    AR("ar", "العربية", "ع");

    fun next(): LauncherLanguage = when (this) {
        EN -> FR
        FR -> AR
        AR -> EN
    }
}

enum class ThemeCategory {
    CLASSIC,
    SPORTS_CLUBS,
    SPORTS_NATIONAL,
    SPORTS_ARAB,
}

enum class ThemePreset(
    val id: String,
    val labelFr: String,
    val labelAr: String,
    val labelEn: String,
    val accentHex: Long,
    val surfaceHex: Long,
    val defaultWallpaperId: String,
    val category: ThemeCategory = ThemeCategory.CLASSIC,
    val secondaryHex: Long = accentHex,
    val tertiaryHex: Long = 0xFFFFFFFF,
    val badgeCode: String = "HRZ",
) {
    COSMOS_BLUE(
        id = "cosmos_blue",
        labelFr = "Cosmos Bleu",
        labelAr = "الفضاء الأزرق",
        labelEn = "Cosmos Blue",
        accentHex = 0xFF38BDF8,
        surfaceHex = 0xFF0E2960,
        defaultWallpaperId = "earth",
        badgeCode = "CSM",
    ),
    AURORA_EMERALD(
        id = "aurora_emerald",
        labelFr = "Aurore Émeraude",
        labelAr = "الشفق الزمردي",
        labelEn = "Aurora Emerald",
        accentHex = 0xFF34D399,
        surfaceHex = 0xFF0A2E2B,
        defaultWallpaperId = "aurora",
        badgeCode = "AUR",
    ),
    AMBER_TWILIGHT(
        id = "amber_twilight",
        labelFr = "Crépuscule Ambre",
        labelAr = "غسق العنبر",
        labelEn = "Amber Twilight",
        accentHex = 0xFFFBBF24,
        surfaceHex = 0xFF281B18,
        defaultWallpaperId = "amber_night",
        badgeCode = "AMB",
    ),
    COBALT_CRYSTAL(
        id = "cobalt_crystal",
        labelFr = "Cristal Cobalt",
        labelAr = "كريستال الكوبالت",
        labelEn = "Cobalt Crystal",
        accentHex = 0xFF22D3EE,
        surfaceHex = 0xFF0A1D42,
        defaultWallpaperId = "cobalt_waves",
        badgeCode = "CBL",
    ),
    OLED_MINIMAL(
        id = "oled_minimal",
        labelFr = "OLED Minimaliste",
        labelAr = "أسود نقي",
        labelEn = "OLED Minimal",
        accentHex = 0xFFE2E8F0,
        surfaceHex = 0xFF0F172A,
        defaultWallpaperId = "gradient_dark",
        badgeCode = "OLD",
    ),

    // أشهر الأندية العالمية / Clubs célèbres
    REAL_MADRID(
        id = "real_madrid",
        labelFr = "Real Madrid",
        labelAr = "ريال مدريد",
        labelEn = "Real Madrid",
        accentHex = 0xFFFACC15,
        surfaceHex = 0xFF0F1B3D,
        defaultWallpaperId = "sport_real_madrid",
        category = ThemeCategory.SPORTS_CLUBS,
        secondaryHex = 0xFFF8FAFC,
        tertiaryHex = 0xFF2563EB,
        badgeCode = "RMA",
    ),
    BARCELONA(
        id = "barcelona",
        labelFr = "FC Barcelone",
        labelAr = "برشلونة",
        labelEn = "FC Barcelona",
        accentHex = 0xFFEF4444,
        surfaceHex = 0xFF0A1D56,
        defaultWallpaperId = "sport_barcelona",
        category = ThemeCategory.SPORTS_CLUBS,
        secondaryHex = 0xFFA50044,
        tertiaryHex = 0xFFFACC15,
        badgeCode = "FCB",
    ),
    MAN_CITY(
        id = "man_city",
        labelFr = "Manchester City",
        labelAr = "مانشستر سيتي",
        labelEn = "Manchester City",
        accentHex = 0xFF6CABDD,
        surfaceHex = 0xFF0A1E3D,
        defaultWallpaperId = "sport_man_city",
        category = ThemeCategory.SPORTS_CLUBS,
        secondaryHex = 0xFF38BDF8,
        tertiaryHex = 0xFFFACC15,
        badgeCode = "MCI",
    ),
    PSG(
        id = "psg",
        labelFr = "Paris Saint-Germain",
        labelAr = "باريس سان جيرمان",
        labelEn = "Paris Saint-Germain",
        accentHex = 0xFFEF4444,
        surfaceHex = 0xFF081636,
        defaultWallpaperId = "sport_psg",
        category = ThemeCategory.SPORTS_CLUBS,
        secondaryHex = 0xFFDA291C,
        tertiaryHex = 0xFFF8FAFC,
        badgeCode = "PSG",
    ),

    // منتخبات عالمية / Équipes nationales
    BRAZIL(
        id = "brazil",
        labelFr = "Brésil",
        labelAr = "البرازيل",
        labelEn = "Brazil",
        accentHex = 0xFFFACC15,
        surfaceHex = 0xFF062B17,
        defaultWallpaperId = "sport_brazil",
        category = ThemeCategory.SPORTS_NATIONAL,
        secondaryHex = 0xFF009C3B,
        tertiaryHex = 0xFF1D4ED8,
        badgeCode = "BRA",
    ),
    ARGENTINA(
        id = "argentina",
        labelFr = "Argentine",
        labelAr = "الأرجنتين",
        labelEn = "Argentina",
        accentHex = 0xFF5BC0F8,
        surfaceHex = 0xFF091F3E,
        defaultWallpaperId = "sport_argentina",
        category = ThemeCategory.SPORTS_NATIONAL,
        secondaryHex = 0xFFF8FAFC,
        tertiaryHex = 0xFFFACC15,
        badgeCode = "ARG",
    ),
    FRANCE(
        id = "france",
        labelFr = "France",
        labelAr = "فرنسا",
        labelEn = "France",
        accentHex = 0xFF3B82F6,
        surfaceHex = 0xFF091945,
        defaultWallpaperId = "sport_france",
        category = ThemeCategory.SPORTS_NATIONAL,
        secondaryHex = 0xFFF8FAFC,
        tertiaryHex = 0xFFEF4135,
        badgeCode = "FRA",
    ),
    SPAIN(
        id = "spain",
        labelFr = "Espagne",
        labelAr = "إسبانيا",
        labelEn = "Spain",
        accentHex = 0xFFFBBF24,
        surfaceHex = 0xFF3B0912,
        defaultWallpaperId = "sport_spain",
        category = ThemeCategory.SPORTS_NATIONAL,
        secondaryHex = 0xFFAA151B,
        tertiaryHex = 0xFFFACC15,
        badgeCode = "ESP",
    ),

    // المنتخبات العربية / Équipes arabes
    EGYPT(
        id = "egypt",
        labelFr = "Égypte",
        labelAr = "مصر",
        labelEn = "Egypt",
        accentHex = 0xFFFBBF24,
        surfaceHex = 0xFF32080E,
        defaultWallpaperId = "sport_egypt",
        category = ThemeCategory.SPORTS_ARAB,
        secondaryHex = 0xFFCE1126,
        tertiaryHex = 0xFFF8FAFC,
        badgeCode = "EGY",
    ),
    ALGERIA(
        id = "algeria",
        labelFr = "Algérie",
        labelAr = "الجزائر",
        labelEn = "Algeria",
        accentHex = 0xFF10B981,
        surfaceHex = 0xFF04291A,
        defaultWallpaperId = "sport_algeria",
        category = ThemeCategory.SPORTS_ARAB,
        secondaryHex = 0xFFF8FAFC,
        tertiaryHex = 0xFFD21034,
        badgeCode = "DZA",
    ),
    SAUDI_ARABIA(
        id = "saudi_arabia",
        labelFr = "Arabie Saoudite",
        labelAr = "المملكة العربية السعودية",
        labelEn = "Saudi Arabia",
        accentHex = 0xFF22C55E,
        surfaceHex = 0xFF042818,
        defaultWallpaperId = "sport_saudi_arabia",
        category = ThemeCategory.SPORTS_ARAB,
        secondaryHex = 0xFF006C35,
        tertiaryHex = 0xFFF8FAFC,
        badgeCode = "KSA",
    ),
    TUNISIA(
        id = "tunisia",
        labelFr = "Tunisie",
        labelAr = "تونس",
        labelEn = "Tunisia",
        accentHex = 0xFFEF4444,
        surfaceHex = 0xFF38070E,
        defaultWallpaperId = "sport_tunisia",
        category = ThemeCategory.SPORTS_ARAB,
        secondaryHex = 0xFFE70013,
        tertiaryHex = 0xFFF8FAFC,
        badgeCode = "TUN",
    ),
    ;

    val isSportsTheme: Boolean get() = category != ThemeCategory.CLASSIC

    companion object {
        fun byWallpaperId(id: String): ThemePreset? =
            entries.firstOrNull { it.defaultWallpaperId == id }
    }
}

enum class DockStyle(val id: String) {
    FLOATING_GLASS("floating_glass"),
    FULL_WIDTH("full_width"),
    MINIMAL_CARDS("minimal_cards"),
}

enum class AppIconSize(val id: String) {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
}

enum class AppIconShape(val id: String) {
    CIRCLE("circle"),
    RECTANGLE("rectangle"),
    SQUARE("square"),
}

enum class GestureType(val key: String) {
    SWIPE_UP("swipe_up"),
    SWIPE_DOWN("swipe_down"),
    SWIPE_LEFT("swipe_left"),
    SWIPE_RIGHT("swipe_right"),
    DOUBLE_TAP("double_tap"),
    LONG_PRESS("long_press"),
}

enum class GestureAction(val key: String) {
    OPEN_APP_LIBRARY("open_app_library"),
    OPEN_SEARCH("open_search"),
    OPEN_SETTINGS("open_settings"),
    OPEN_WEATHER("open_weather"),
    OPEN_PRAYER("open_prayer"),
    OPEN_WALLPAPER_STUDIO("open_wallpaper_studio"),
    OPEN_UI_CUSTOMIZER("open_ui_customizer"),
    TOGGLE_ADHAN_AUDIO("toggle_adhan_audio"),
    REFRESH_GPS_DATA("refresh_gps_data"),
    NONE("none"),
}

enum class PrayerKey(val id: String, val labelFr: String, val labelAr: String, val labelEn: String) {
    FAJR("fajr", "Fajr", "الفجر", "Fajr"),
    DHUHR("dhuhr", "Dhuhr", "الظهر", "Dhuhr"),
    ASR("asr", "Asr", "العصر", "Asr"),
    MAGHRIB("maghrib", "Maghrib", "المغرب", "Maghrib"),
    ISHA("isha", "Isha", "العشاء", "Isha"),
}

enum class AdhanAlertMode(val id: String) {
    SILENT("silent"),
    NOTIFICATION_ONLY("notification"),
    AUDIO_AND_NOTIFICATION("audio"),
}

enum class ClockTheme(
    val id: String,
    val labelFr: String,
    val labelAr: String,
    val labelEn: String,
    val isAnalog: Boolean = false,
) {
    CLASSIC_HORIZON("classic_horizon", "Horizon Classique", "كلاسيكي هوريزون", "Horizon Classic"),
    BLACK_ORANGE("black_orange", "Black Orange", "أسود وبرتقالي", "Black Orange"),
    BIG_SIMPLE_FLIP("big_simple_flip", "Big Simple Flip", "ساعة قلاب كبيرة", "Big Simple Flip"),
    PURE_GOLD_LCD("pure_gold_lcd", "Pure Gold", "الذهبي الفاخر", "Pure Gold"),
    EXQUISITE_NEON("exquisite_neon", "Exquisite", "متوهج عصري", "Exquisite"),
    STARRY_SKY("starry_sky", "Starry Sky", "سماء النجوم", "Starry Sky"),
    THREE_D_DIGITS("three_d_digits", "3D Digits", "أرقام ثلاثية الأبعاد", "3D Digits"),
    INK_ECHOES("ink_echoes", "Ink Echoes", "صدى الحبر الميكانيكي", "Ink Echoes"),
    LCD_OFFROAD("lcd_offroad", "LCD Off-Road", "رقمي رياضي", "LCD Off-Road"),
    FUNCTION_BLUE("function_blue", "Function Desk", "أزرق وظيفي", "Function Desk"),
    SIMPLE_RED("simple_red", "Simple Red", "الأحمر الملكي (عقارب)", "Simple Red", isAnalog = true),
    RETRO_VINTAGE("retro_vintage", "Retro Vintage", "تراثي عتيق (عقارب)", "Retro Vintage", isAnalog = true),
    PLAIN_WHITE("plain_white", "Plain White", "أبيض كلاسيكي (عقارب)", "Plain White", isAnalog = true),
    MECHANICAL_BLUE("mechanical_blue", "Mechanical Blue", "ميكانيكي أزرق (عقارب)", "Mechanical Blue", isAnalog = true),
    LACK_CLASSIC("lack_classic", "Lack Roman", "روماني هادئ (عقارب)", "Lack Roman", isAnalog = true);

    fun displayLabel(language: LauncherLanguage): String = when (language) {
        LauncherLanguage.FR -> labelFr
        LauncherLanguage.AR -> labelAr
        LauncherLanguage.EN -> labelEn
    }
}

data class PrayerCalculationMethod(
    val id: Int,
    val name: String,
) {
    companion object {
        val ALL_METHODS = listOf(
            PrayerCalculationMethod(12, "12 · UOIF, France"),
            PrayerCalculationMethod(2, "2 · ISNA (North America)"),
            PrayerCalculationMethod(3, "3 · Muslim World League"),
            PrayerCalculationMethod(4, "4 · Umm Al-Qura, Makkah"),
            PrayerCalculationMethod(5, "5 · Egyptian General Authority"),
            PrayerCalculationMethod(19, "19 · Algeria (Ministère des Affaires Religieuses)"),
            PrayerCalculationMethod(21, "21 · Morocco (Habous)"),
            PrayerCalculationMethod(13, "13 · Diyanet, Turkey"),
            PrayerCalculationMethod(1, "1 · University of Islamic Sciences, Karachi"),
        )

        fun byId(id: Int): PrayerCalculationMethod =
            ALL_METHODS.firstOrNull { it.id == id } ?: ALL_METHODS.first()
    }
}

data class AppItem(
    val packageName: String,
    val label: String,
    val activityName: String,
    val icon: Drawable?,
    val banner: Drawable? = null,
    val isSystemApp: Boolean = false,
)

data class AiToolPreset(
    val id: String,
    val label: String,
    val packageName: String,
    val webUrl: String,
    val shortBadge: String,
    val accentHex: Long,
) {
    val storageKey: String get() = "preset:$id"

    companion object {
        val ALL_PRESETS = listOf(
            AiToolPreset("chatgpt", "ChatGPT", "com.openai.chatgpt", "https://chatgpt.com", "GPT", 0xFF10A37F),
            AiToolPreset("gemini", "Gemini", "com.google.android.apps.bard", "https://gemini.google.com", "GEM", 0xFF38BDF8),
            AiToolPreset("claude", "Claude", "com.anthropic.claude", "https://claude.ai", "CLD", 0xFFD97706),
            AiToolPreset("perplexity", "Perplexity", "ai.perplexity.app.android", "https://www.perplexity.ai", "PPX", 0xFF22D3EE),
            AiToolPreset("copilot", "Copilot", "com.microsoft.copilot", "https://copilot.microsoft.com", "CPL", 0xFF6366F1),
            AiToolPreset("deepseek", "DeepSeek", "com.deepseek.chat", "https://chat.deepseek.com", "DSK", 0xFF3B82F6),
        )

        fun byId(id: String): AiToolPreset? = ALL_PRESETS.firstOrNull { it.id == id }
    }
}

data class NetworkStatus(
    val type: NetworkType = NetworkType.WIFI,
    val label: String = "Wi-Fi",
)

enum class NetworkType {
    ETHERNET,
    WIFI,
    CELLULAR,
    OFFLINE,
}

data class PrayerTimeEntry(
    val key: PrayerKey,
    val time24: String,
    val minutesFromMidnight: Int,
)

data class PrayerTimesData(
    val cityDisplay: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val methodId: Int,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDate: String = "",
    val hijriDateAr: String = "",
    val sourceLabel: String = "Aladhan",
) {
    val entries: List<PrayerTimeEntry>
        get() = listOf(
            PrayerTimeEntry(PrayerKey.FAJR, fajr, parseMinutes(fajr)),
            PrayerTimeEntry(PrayerKey.DHUHR, dhuhr, parseMinutes(dhuhr)),
            PrayerTimeEntry(PrayerKey.ASR, asr, parseMinutes(asr)),
            PrayerTimeEntry(PrayerKey.MAGHRIB, maghrib, parseMinutes(maghrib)),
            PrayerTimeEntry(PrayerKey.ISHA, isha, parseMinutes(isha)),
        )

    private fun parseMinutes(hhmm: String): Int {
        val clean = hhmm.take(5)
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }
}

data class NextPrayerInfo(
    val key: PrayerKey,
    val timeFormatted: String,
    val remainingHours: Int,
    val remainingMinutes: Int,
)

data class HourlyWeatherItem(
    val hourLabel: String,
    val temperatureC: Double,
    val weatherCode: Int,
)

data class DailyWeatherItem(
    val dayLabel: String,
    val weatherCode: Int,
    val tempMaxC: Double,
    val tempMinC: Double,
)

data class WeatherData(
    val cityDisplay: String,
    val temperatureC: Double,
    val apparentTemperatureC: Double,
    val weatherCode: Int,
    val windSpeedKmh: Double,
    val humidityPercent: Int,
    val tempMaxC: Double,
    val tempMinC: Double,
    val isDay: Boolean,
    val isGpsSynced: Boolean,
    val hourly: List<HourlyWeatherItem> = emptyList(),
    val daily: List<DailyWeatherItem> = emptyList(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

data class CitySearchResult(
    val name: String,
    val admin1: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
) {
    val fullDisplayName: String
        get() = listOfNotNull(name, admin1?.takeIf { it.isNotBlank() }, country?.takeIf { it.isNotBlank() })
            .distinct()
            .joinToString(", ")
}

data class LauncherSettings(
    val language: LauncherLanguage = LauncherLanguage.FR,
    val appsPerRow: Int = 7,
    val rowsBelowFavorites: Int = 0,
    val enhancedContrast: Boolean = true,
    val is24HourFormat: Boolean = true,
    val showDate: Boolean = true,
    val showGreeting: Boolean = true,
    val clockTheme: ClockTheme = ClockTheme.CLASSIC_HORIZON,
    val customSectionTitle: String = "HORIZON PRO",
    val themePreset: ThemePreset = ThemePreset.COSMOS_BLUE,
    val accentColorHex: Long = 0xFF38BDF8,
    val wallpaperId: String = "earth",
    val customWallpaperUri: String? = null,
    val customWallpaperIsVideo: Boolean = false,
    val wallpaperDimAlpha: Float = 0.25f,
    val glassOpacity: Float = 0.56f,
    val cardCornerRadiusDp: Int = 18,
    val dockStyle: DockStyle = DockStyle.FLOATING_GLASS,
    val dockScalePercent: Int = 100,
    val iconSize: AppIconSize = AppIconSize.MEDIUM,
    val iconShape: AppIconShape = AppIconShape.RECTANGLE,
    val navigationSoundEnabled: Boolean = true,
    val showWeatherWidget: Boolean = true,
    val showPrayerWidget: Boolean = true,
    val useCelsius: Boolean = true,
    val useGpsLocation: Boolean = true,
    val cityName: String = "Mions Centre, Auvergne-Rhône-Alpes, France",
    val shortCityName: String = "Mions Centre",
    val latitude: Double = 45.6626,
    val longitude: Double = 4.9536,
    val timezoneId: String = "Europe/Paris",
    val prayerMethodId: Int = 12,
    val adhanEnabled: Boolean = true,
    val adhanMode: AdhanAlertMode = AdhanAlertMode.AUDIO_AND_NOTIFICATION,
    val customAdhanAudioUri: String? = null,
    val customAdhanAudioName: String? = null,
    val enabledPrayers: Set<PrayerKey> = PrayerKey.entries.toSet(),
    val animationsEnabled: Boolean = true,
    val advancedEffectsEnabled: Boolean = true,
    val showHdmiSourcesBar: Boolean = false,
    val developerName: String = "Horizon TV Developer",
    val developerEmail: String = "support@horizontv.app",
    val developerWhatsApp: String = "",
    val githubUpdateRepo: String = "",
    val autoCheckUpdatesOnStartup: Boolean = true,
    val favoritePackages: List<String> = emptyList(),
    val aiToolShortcuts: List<String> = emptyList(),
    val customAppOrder: List<String> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
    val gestureShortcuts: Map<GestureType, GestureAction> = mapOf(
        GestureType.SWIPE_UP to GestureAction.OPEN_APP_LIBRARY,
        GestureType.SWIPE_DOWN to GestureAction.OPEN_SEARCH,
        GestureType.SWIPE_LEFT to GestureAction.OPEN_PRAYER,
        GestureType.SWIPE_RIGHT to GestureAction.OPEN_WEATHER,
        GestureType.DOUBLE_TAP to GestureAction.OPEN_SETTINGS,
        GestureType.LONG_PRESS to GestureAction.OPEN_WALLPAPER_STUDIO,
    ),
) {
    val accentColor: Color get() = Color(accentColorHex)
}

enum class LauncherDestination {
    HOME,
    SETTINGS_MENU,
    LAUNCHER_SETTINGS,
    APP_LIBRARY,
    EDIT_FAVORITES,
    REORDER_APPS,
    SEARCH_APPS,
    HIDDEN_APPS,
    ABOUT_SUPPORT,
    WALLPAPER_STUDIO,
    UI_CUSTOMIZER,
    GESTURE_SHORTCUTS,
    PRAYER_AND_ADHAN,
    WEATHER_DETAIL,
}
