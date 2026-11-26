package com.example.data

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.BuildConfig
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.RadioStation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

@JsonClass(generateAdapter = true)
data class RadioBrowserStationDto(
    @Json(name = "stationuuid") val stationUuid: String? = null,
    val name: String? = null,
    val url: String? = null,
    @Json(name = "url_resolved") val urlResolved: String? = null,
    val country: String? = null,
    @Json(name = "countrycode") val countryCode: String? = null,
    val state: String? = null,
    val language: String? = null,
    val tags: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val favicon: String? = null,
    val votes: Int? = null,
    @Json(name = "lastcheckok") val lastCheckOk: Int? = null,
)

interface RadioBrowserApi {
    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("countrycode") countryCode: String? = null,
        @Query("name") name: String? = null,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("is_https") isHttps: Boolean = true,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("limit") limit: Int = 60,
    ): List<RadioBrowserStationDto>
}

data class RadioCountryOption(
    val code: String,
    val labelFr: String,
    val labelAr: String,
    val labelEn: String,
) {
    fun displayName(language: LauncherLanguage): String = when (language) {
        LauncherLanguage.FR -> labelFr
        LauncherLanguage.AR -> labelAr
        LauncherLanguage.EN -> labelEn
    }
}

sealed interface RadioFetchResult {
    data class Success(val stations: List<RadioStation>, val countryCode: String?) : RadioFetchResult
    data class Failure(val errorMessage: String) : RadioFetchResult
}

class RadioRepository(private val context: Context) {

    private val moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "HorizonTVLauncher/${BuildConfig.VERSION_NAME}")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(req)
            }
            .build()
    }

    private val apis: List<RadioBrowserApi> by lazy {
        MIRROR_BASE_URLS.map { baseUrl ->
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RadioBrowserApi::class.java)
        }
    }

    suspend fun resolveCountryCode(settings: LauncherSettings): String = withContext(Dispatchers.IO) {
        inferCountryFromCityName(settings.cityName)?.let { return@withContext it }
        inferCountryFromTimezone(settings.timezoneId)?.let { return@withContext it }

        val geocoded = reverseGeocodeCountryCode(settings.latitude, settings.longitude)
        if (!geocoded.isNullOrBlank() && geocoded.length == 2) {
            return@withContext geocoded.uppercase(Locale.US)
        }

        val localeCountry = Locale.getDefault().country.trim().uppercase(Locale.US)
        if (localeCountry.length == 2 && localeCountry.all { it in 'A'..'Z' }) {
            return@withContext localeCountry
        }

        "FR"
    }

    suspend fun fetchStations(
        countryCode: String? = null,
        query: String? = null,
        limit: Int = 60,
    ): RadioFetchResult = withContext(Dispatchers.IO) {
        val cleanCountry = countryCode?.trim()?.uppercase(Locale.US)?.takeIf { it.length == 2 }
        val cleanQuery = query?.trim()?.takeIf { it.isNotEmpty() }

        var lastError: Throwable? = null
        for (api in apis) {
            val attempt = runCatching {
                val raw = api.searchStations(
                    countryCode = cleanCountry,
                    name = cleanQuery,
                    limit = limit,
                )
                filterAndMapStations(raw)
            }
            if (attempt.isSuccess) {
                return@withContext RadioFetchResult.Success(
                    stations = attempt.getOrDefault(emptyList()),
                    countryCode = cleanCountry,
                )
            }
            lastError = attempt.exceptionOrNull()
        }
        RadioFetchResult.Failure(lastError?.localizedMessage ?: "Network error")
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocodeCountryCode(lat: Double, lon: Double): String? {
        if (!Geocoder.isPresent()) return null
        return runCatching {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { list -> cont.resume(list) }
                }
            } else {
                geocoder.getFromLocation(lat, lon, 1).orEmpty()
            }
            addresses.firstOrNull()?.countryCode
                ?.trim()
                ?.uppercase(Locale.US)
                ?.takeIf { it.length == 2 }
        }.getOrNull()
    }

    companion object {
        private val MIRROR_BASE_URLS = listOf(
            "https://de1.api.radio-browser.info/",
            "https://fi1.api.radio-browser.info/",
            "https://at1.api.radio-browser.info/",
        )

        private val UNSUPPORTED_PLAYLIST_EXTENSIONS = listOf(".pls", ".asx", ".ram", ".xspf")

        val QUICK_COUNTRIES = listOf(
            RadioCountryOption("FR", "France", "فرنسا", "France"),
            RadioCountryOption("DZ", "Algérie", "الجزائر", "Algeria"),
            RadioCountryOption("MA", "Maroc", "المغرب", "Morocco"),
            RadioCountryOption("TN", "Tunisie", "تونس", "Tunisia"),
            RadioCountryOption("EG", "Égypte", "مصر", "Egypt"),
            RadioCountryOption("SA", "Arabie Saoudite", "السعودية", "Saudi Arabia"),
            RadioCountryOption("AE", "Émirats Arabes", "الإمارات", "UAE"),
            RadioCountryOption("GB", "Royaume-Uni", "بريطانيا", "UK"),
            RadioCountryOption("US", "États-Unis", "أمريكا", "USA"),
            RadioCountryOption("DE", "Allemagne", "ألمانيا", "Germany"),
            RadioCountryOption("ES", "Espagne", "إسبانيا", "Spain"),
            RadioCountryOption("TR", "Turquie", "تركيا", "Turkey"),
            RadioCountryOption("CA", "Canada", "كندا", "Canada"),
        )

        fun countryOptionForCode(code: String): RadioCountryOption? =
            QUICK_COUNTRIES.firstOrNull { it.code.equals(code, ignoreCase = true) }

        fun filterAndMapStations(raw: List<RadioBrowserStationDto>): List<RadioStation> {
            val seenUuids = HashSet<String>()
            val seenUrls = HashSet<String>()
            val result = ArrayList<RadioStation>(raw.size)
            for (dto in raw) {
                val uuid = dto.stationUuid?.trim().orEmpty()
                val name = dto.name?.trim().orEmpty()
                if (uuid.isEmpty() || name.isEmpty()) continue
                if (dto.lastCheckOk == 0) continue

                val resolved = dto.urlResolved?.trim().orEmpty().ifEmpty { dto.url?.trim().orEmpty() }
                if (!resolved.startsWith("https://", ignoreCase = true)) continue

                val pathLower = resolved.substringBefore('?').lowercase(Locale.US)
                if (UNSUPPORTED_PLAYLIST_EXTENSIONS.any { pathLower.endsWith(it) }) continue
                if (pathLower.endsWith(".m3u")) continue

                val normalizedUrl = resolved.lowercase(Locale.US)
                if (!seenUuids.add(uuid) || !seenUrls.add(normalizedUrl)) continue

                result.add(
                    RadioStation(
                        stationUuid = uuid,
                        name = name,
                        streamUrl = resolved,
                        country = dto.country?.trim().orEmpty(),
                        countryCode = dto.countryCode?.trim()?.uppercase(Locale.US).orEmpty(),
                        state = dto.state?.trim().orEmpty(),
                        language = dto.language?.trim().orEmpty(),
                        tags = dto.tags?.trim().orEmpty(),
                        codec = dto.codec?.trim()?.uppercase(Locale.US).orEmpty(),
                        bitrate = (dto.bitrate ?: 0).coerceAtLeast(0),
                        favicon = dto.favicon?.trim().orEmpty(),
                        votes = (dto.votes ?: 0).coerceAtLeast(0),
                    )
                )
            }
            return result
        }

        fun inferCountryFromCityName(cityName: String): String? {
            if (cityName.isBlank()) return null
            val lower = cityName.lowercase(Locale.US)
            return CITY_COUNTRY_KEYWORDS.firstOrNull { (keywords, _) ->
                keywords.any { lower.contains(it) }
            }?.second
        }

        fun inferCountryFromTimezone(timezoneId: String): String? {
            if (timezoneId.isBlank()) return null
            TIMEZONE_TO_COUNTRY[timezoneId]?.let { return it }
            val trimmed = timezoneId.trim()
            return when {
                trimmed.startsWith("America/Argentina/") -> "AR"
                trimmed.startsWith("America/Indiana/") || trimmed.startsWith("America/Kentucky/") -> "US"
                trimmed.startsWith("Australia/") -> "AU"
                else -> null
            }
        }

        private val CITY_COUNTRY_KEYWORDS: List<Pair<List<String>, String>> = listOf(
            listOf("france", "فرنسا") to "FR",
            listOf("algeria", "algérie", "algerie", "الجزائر") to "DZ",
            listOf("morocco", "maroc", "المغرب") to "MA",
            listOf("tunisia", "tunisie", "تونس") to "TN",
            listOf("egypt", "égypte", "egypte", "مصر") to "EG",
            listOf("saudi arabia", "arabie saoudite", "السعودية") to "SA",
            listOf("united arab emirates", "émirats", "emirates", "dubai", "الإمارات") to "AE",
            listOf("qatar", "قطر") to "QA",
            listOf("kuwait", "koweït", "الكويت") to "KW",
            listOf("jordan", "jordanie", "الأردن") to "JO",
            listOf("lebanon", "liban", "لبنان") to "LB",
            listOf("iraq", "irak", "العراق") to "IQ",
            listOf("united kingdom", "royaume-uni", "england", "london", "بريطانيا") to "GB",
            listOf("united states", "états-unis", "etats-unis", "usa", "أمريكا") to "US",
            listOf("germany", "allemagne", "deutschland", "ألمانيا") to "DE",
            listOf("spain", "espagne", "españa", "إسبانيا") to "ES",
            listOf("italy", "italie", "italia", "إيطاليا") to "IT",
            listOf("belgium", "belgique", "بلجيكا") to "BE",
            listOf("switzerland", "suisse", "سويسرا") to "CH",
            listOf("netherlands", "pays-bas", "هولندا") to "NL",
            listOf("turkey", "turquie", "türkiye", "تركيا") to "TR",
            listOf("canada", "كندا") to "CA",
            listOf("portugal", "البرتغال") to "PT",
            listOf("brazil", "brésil", "البرازيل") to "BR",
        )

        private val TIMEZONE_TO_COUNTRY = mapOf(
            "Europe/Paris" to "FR",
            "Africa/Algiers" to "DZ",
            "Africa/Casablanca" to "MA",
            "Africa/Tunis" to "TN",
            "Africa/Cairo" to "EG",
            "Asia/Riyadh" to "SA",
            "Asia/Dubai" to "AE",
            "Asia/Qatar" to "QA",
            "Asia/Kuwait" to "KW",
            "Asia/Amman" to "JO",
            "Asia/Beirut" to "LB",
            "Asia/Baghdad" to "IQ",
            "Europe/London" to "GB",
            "Europe/Berlin" to "DE",
            "Europe/Madrid" to "ES",
            "Europe/Rome" to "IT",
            "Europe/Brussels" to "BE",
            "Europe/Zurich" to "CH",
            "Europe/Amsterdam" to "NL",
            "Europe/Lisbon" to "PT",
            "Europe/Istanbul" to "TR",
            "America/New_York" to "US",
            "America/Chicago" to "US",
            "America/Denver" to "US",
            "America/Los_Angeles" to "US",
            "America/Phoenix" to "US",
            "America/Toronto" to "CA",
            "America/Montreal" to "CA",
            "America/Vancouver" to "CA",
            "America/Sao_Paulo" to "BR",
        )
    }
}
