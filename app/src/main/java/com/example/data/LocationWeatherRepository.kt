package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.model.CitySearchResult
import com.example.model.DailyWeatherItem
import com.example.model.HourlyWeatherItem
import com.example.model.WeatherData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    val timezone: String?,
    val current: OpenMeteoCurrent?,
    val hourly: OpenMeteoHourly?,
    val daily: OpenMeteoDaily?,
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int?,
    @Json(name = "apparent_temperature") val apparentTemperature: Double?,
    @Json(name = "is_day") val isDay: Int?,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?,
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    val time: List<String>?,
    @Json(name = "temperature_2m") val temperature2m: List<Double>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    val time: List<String>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>?,
)

@JsonClass(generateAdapter = true)
data class OpenMeteoGeoResponse(
    val results: List<OpenMeteoGeoItem>?,
)

@JsonClass(generateAdapter = true)
data class OpenMeteoGeoItem(
    val name: String?,
    val admin1: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
)

interface OpenMeteoWeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 5,
    ): OpenMeteoForecastResponse
}

interface OpenMeteoGeoApi {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") query: String,
        @Query("count") count: Int = 8,
        @Query("language") language: String = "fr",
        @Query("format") format: String = "json",
    ): OpenMeteoGeoResponse
}

data class ResolvedGpsLocation(
    val latitude: Double,
    val longitude: Double,
    val shortCityName: String,
    val fullCityName: String,
    val timezoneId: String,
)

class LocationWeatherRepository(private val context: Context) {

    private val moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val weatherApi: OpenMeteoWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoWeatherApi::class.java)
    }

    private val geoApi: OpenMeteoGeoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoGeoApi::class.java)
    }

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return coarse == PackageManager.PERMISSION_GRANTED || fine == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentGpsLocation(): ResolvedGpsLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val fusedLoc: Location? = suspendCancellableCoroutine { cont ->
            val tokenSource = CancellationTokenSource()
            cont.invokeOnCancellation { tokenSource.cancel() }
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        fusedClient.lastLocation
                            .addOnSuccessListener { last -> cont.resume(last) }
                            .addOnFailureListener { cont.resume(null) }
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }

        val location = fusedLoc ?: fallbackLocationManager() ?: return@withContext null
        val (shortName, fullName) = reverseGeocode(location.latitude, location.longitude)
        ResolvedGpsLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            shortCityName = shortName,
            fullCityName = fullName,
            timezoneId = TimeZone.getDefault().id,
        )
    }

    @SuppressLint("MissingPermission")
    private fun fallbackLocationManager(): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        return providers.firstNotNullOfOrNull { provider ->
            runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(lat: Double, lon: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { list -> cont.resume(list) }
                }
            } else {
                geocoder.getFromLocation(lat, lon, 1).orEmpty()
            }
            val addr = addresses.firstOrNull()
            if (addr != null) {
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "%.2f, %.2f".format(lat, lon)
                val region = addr.adminArea
                val country = addr.countryName
                val full = listOfNotNull(city, region, country).distinct().joinToString(", ")
                city to full
            } else {
                "GPS (%.2f, %.2f)".format(lat, lon) to "GPS (%.4f, %.4f)".format(lat, lon)
            }
        }.getOrElse {
            "GPS (%.2f, %.2f)".format(lat, lon) to "GPS (%.4f, %.4f)".format(lat, lon)
        }
    }

    suspend fun searchCities(query: String, languageCode: String): List<CitySearchResult> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        runCatching {
            geoApi.searchCity(query = query.trim(), language = languageCode)
                .results.orEmpty()
                .mapNotNull { item ->
                    val name = item.name ?: return@mapNotNull null
                    val lat = item.latitude ?: return@mapNotNull null
                    val lon = item.longitude ?: return@mapNotNull null
                    CitySearchResult(
                        name = name,
                        admin1 = item.admin1,
                        country = item.country,
                        latitude = lat,
                        longitude = lon,
                        timezone = item.timezone,
                    )
                }
        }.getOrDefault(emptyList())
    }

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityDisplay: String,
        isGpsSynced: Boolean,
    ): WeatherData? = withContext(Dispatchers.IO) {
        runCatching {
            val res = weatherApi.getForecast(latitude = latitude, longitude = longitude)
            val current = res.current ?: return@runCatching null

            val hourlyTimes = res.hourly?.time.orEmpty()
            val hourlyTemps = res.hourly?.temperature2m.orEmpty()
            val hourlyCodes = res.hourly?.weatherCode.orEmpty()

            val currentHourIso = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US).format(System.currentTimeMillis())
            val startIndex = hourlyTimes.indexOfFirst { it.startsWith(currentHourIso) }.coerceAtLeast(0)
            val hourlyList = (startIndex until minOf(startIndex + 10, hourlyTimes.size)).map { idx ->
                val rawTime = hourlyTimes[idx]
                val hourLabel = rawTime.substringAfter("T", rawTime).take(5)
                HourlyWeatherItem(
                    hourLabel = hourLabel,
                    temperatureC = hourlyTemps.getOrElse(idx) { current.temperature2m ?: 15.0 },
                    weatherCode = hourlyCodes.getOrElse(idx) { current.weatherCode ?: 0 },
                )
            }

            val dailyDates = res.daily?.time.orEmpty()
            val dailyCodes = res.daily?.weatherCode.orEmpty()
            val dailyMax = res.daily?.temperature2mMax.orEmpty()
            val dailyMin = res.daily?.temperature2mMin.orEmpty()
            val parseFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outFmt = SimpleDateFormat("EEE d", Locale.getDefault())

            val dailyList = dailyDates.indices.map { idx ->
                val dateLabel = runCatching {
                    val d = parseFmt.parse(dailyDates[idx])
                    if (d != null) outFmt.format(d) else dailyDates[idx]
                }.getOrDefault(dailyDates[idx])
                DailyWeatherItem(
                    dayLabel = dateLabel,
                    weatherCode = dailyCodes.getOrElse(idx) { 0 },
                    tempMaxC = dailyMax.getOrElse(idx) { current.temperature2m ?: 18.0 },
                    tempMinC = dailyMin.getOrElse(idx) { current.temperature2m ?: 10.0 },
                )
            }

            WeatherData(
                cityDisplay = cityDisplay,
                temperatureC = current.temperature2m ?: 15.0,
                apparentTemperatureC = current.apparentTemperature ?: current.temperature2m ?: 15.0,
                weatherCode = current.weatherCode ?: 0,
                windSpeedKmh = current.windSpeed10m ?: 0.0,
                humidityPercent = current.relativeHumidity2m ?: 60,
                tempMaxC = dailyMax.firstOrNull() ?: (current.temperature2m ?: 18.0),
                tempMinC = dailyMin.firstOrNull() ?: (current.temperature2m ?: 10.0),
                isDay = (current.isDay ?: 1) == 1,
                isGpsSynced = isGpsSynced,
                hourly = hourlyList,
                daily = dailyList,
            )
        }.getOrNull()
    }
}
