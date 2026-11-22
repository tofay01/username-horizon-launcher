package com.example.data

import com.example.model.PrayerTimesData
import com.example.prayer.OfflinePrayerCalculator
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    val code: Int?,
    val data: AladhanData?,
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    val timings: AladhanTimings?,
    val date: AladhanDate?,
    val meta: AladhanMeta?,
)

@JsonClass(generateAdapter = true)
data class AladhanTimings(
    @Json(name = "Fajr") val fajr: String?,
    @Json(name = "Sunrise") val sunrise: String?,
    @Json(name = "Dhuhr") val dhuhr: String?,
    @Json(name = "Asr") val asr: String?,
    @Json(name = "Maghrib") val maghrib: String?,
    @Json(name = "Isha") val isha: String?,
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    val hijri: AladhanHijri?,
)

@JsonClass(generateAdapter = true)
data class AladhanHijri(
    val day: String?,
    val month: AladhanHijriMonth?,
    val year: String?,
)

@JsonClass(generateAdapter = true)
data class AladhanHijriMonth(
    val en: String?,
    val ar: String?,
)

@JsonClass(generateAdapter = true)
data class AladhanMeta(
    val timezone: String?,
)

interface AladhanApiService {
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") dateDdMmYyyy: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("timezonestring") timezone: String,
    ): AladhanResponse
}

class PrayerTimesRepository {
    private val moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val api: AladhanApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AladhanApiService::class.java)
    }

    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        timezoneId: String,
        methodId: Int,
        shortCityDisplay: String,
    ): PrayerTimesData = withContext(Dispatchers.IO) {
        val offlineFallback = OfflinePrayerCalculator.calculate(
            latitude = latitude,
            longitude = longitude,
            timezoneId = timezoneId,
            methodId = methodId,
            cityDisplay = shortCityDisplay,
        )

        val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        runCatching {
            val response = api.getTimings(
                dateDdMmYyyy = dateStr,
                latitude = latitude,
                longitude = longitude,
                method = methodId,
                timezone = timezoneId,
            )
            val timings = response.data?.timings ?: return@runCatching offlineFallback
            val hijri = response.data.date?.hijri
            val hijriStr = if (!hijri?.day.isNullOrBlank() && !hijri?.month?.en.isNullOrBlank() && !hijri?.year.isNullOrBlank()) {
                "${hijri?.day} ${hijri?.month?.en} ${hijri?.year}"
            } else {
                offlineFallback.hijriDate
            }
            val hijriArStr = if (!hijri?.day.isNullOrBlank() && !hijri?.month?.ar.isNullOrBlank() && !hijri?.year.isNullOrBlank()) {
                "${hijri?.day} ${hijri?.month?.ar} ${hijri?.year} هـ"
            } else {
                offlineFallback.hijriDateAr
            }
            PrayerTimesData(
                cityDisplay = shortCityDisplay,
                latitude = latitude,
                longitude = longitude,
                timezone = response.data.meta?.timezone ?: timezoneId,
                methodId = methodId,
                fajr = cleanTime(timings.fajr, offlineFallback.fajr),
                sunrise = cleanTime(timings.sunrise, offlineFallback.sunrise),
                dhuhr = cleanTime(timings.dhuhr, offlineFallback.dhuhr),
                asr = cleanTime(timings.asr, offlineFallback.asr),
                maghrib = cleanTime(timings.maghrib, offlineFallback.maghrib),
                isha = cleanTime(timings.isha, offlineFallback.isha),
                hijriDate = hijriStr,
                hijriDateAr = hijriArStr,
                sourceLabel = "Aladhan",
            )
        }.getOrDefault(offlineFallback)
    }

    private fun cleanTime(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        val match = Regex("""\d{1,2}:\d{2}""").find(raw)
        return match?.value?.let {
            val parts = it.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: return@let fallback
            val m = parts.getOrNull(1)?.toIntOrNull() ?: return@let fallback
            "%02d:%02d".format(Locale.US, h, m)
        } ?: fallback
    }
}
