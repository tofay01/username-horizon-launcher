package com.example.prayer

import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone as IcuTimeZone
import com.example.model.LauncherLanguage
import com.example.model.NextPrayerInfo
import com.example.model.PrayerKey
import com.example.model.PrayerTimesData
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

object OfflinePrayerCalculator {

    private data class MethodAngles(
        val fajrAngle: Double,
        val ishaAngle: Double,
        val ishaMinutesAfterMaghrib: Double? = null,
    )

    private fun anglesForMethod(methodId: Int): MethodAngles = when (methodId) {
        12 -> MethodAngles(12.0, 12.0) // UOIF France
        2 -> MethodAngles(15.0, 15.0)  // ISNA
        3 -> MethodAngles(18.0, 17.0)  // MWL
        4 -> MethodAngles(18.5, 0.0, ishaMinutesAfterMaghrib = 90.0) // Makkah
        5 -> MethodAngles(19.5, 17.5)  // Egypt
        19 -> MethodAngles(18.0, 17.0) // Algeria
        21 -> MethodAngles(19.0, 17.0) // Morocco
        13 -> MethodAngles(18.0, 17.0) // Diyanet
        1 -> MethodAngles(18.0, 18.0)  // Karachi
        else -> MethodAngles(18.0, 17.0)
    }

    fun calculate(
        latitude: Double,
        longitude: Double,
        timezoneId: String,
        methodId: Int,
        cityDisplay: String,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneId)),
    ): PrayerTimesData {
        val tz = TimeZone.getTimeZone(timezoneId)
        val cal = (calendar.clone() as Calendar).apply { timeZone = tz }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val tzOffsetHours = tz.getOffset(cal.timeInMillis) / 3600000.0

        val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
        val angles = anglesForMethod(methodId)

        var fajr = sunAngleTime(jd, angles.fajrAngle, latitude, counterClockwise = true)
        val sunrise = sunAngleTime(jd, 0.833, latitude, counterClockwise = true)
        val dhuhr = midDay(jd)
        val asr = asrTime(jd, 1.0, latitude)
        val sunset = sunAngleTime(jd, 0.833, latitude, counterClockwise = false)
        var isha = if (angles.ishaMinutesAfterMaghrib != null) {
            sunset + angles.ishaMinutesAfterMaghrib / 60.0
        } else {
            sunAngleTime(jd, angles.ishaAngle, latitude, counterClockwise = false)
        }

        // High-latitude safety adjustment if polar/twilight angle cannot be reached
        val nightDuration = (sunrise + 24.0) - sunset
        if (fajr.isNaN()) fajr = sunrise - nightDuration * (angles.fajrAngle / 60.0)
        if (isha.isNaN()) isha = sunset + nightDuration * (angles.ishaAngle / 60.0)

        fun adjust(hours: Double): String {
            if (hours.isNaN() || hours.isInfinite()) return "00:00"
            val local = fixHour(hours + tzOffsetHours - longitude / 15.0)
            if (local.isNaN() || local.isInfinite()) return "00:00"
            val totalMin = (local * 60.0).roundToInt().let { ((it % 1440) + 1440) % 1440 }
            val h = totalMin / 60
            val m = totalMin % 60
            return "%02d:%02d".format(Locale.US, h, m)
        }

        val hijriEn = formatHijriDate(LauncherLanguage.EN, timezoneId, cal.timeInMillis)
        val hijriAr = formatHijriDate(LauncherLanguage.AR, timezoneId, cal.timeInMillis)

        return PrayerTimesData(
            cityDisplay = cityDisplay,
            latitude = latitude,
            longitude = longitude,
            timezone = timezoneId,
            methodId = methodId,
            fajr = adjust(fajr),
            sunrise = adjust(sunrise),
            dhuhr = adjust(dhuhr),
            asr = adjust(asr),
            maghrib = adjust(sunset),
            isha = adjust(isha),
            hijriDate = hijriEn,
            hijriDateAr = hijriAr,
            sourceLabel = "Aladhan",
        )
    }

    private val hijriMonthsFr = listOf(
        "Mouharram",
        "Safar",
        "Rabia al-Awwal",
        "Rabia al-Thani",
        "Joumada al-Oula",
        "Joumada al-Thania",
        "Rajab",
        "Chaabane",
        "Ramadan",
        "Chawwal",
        "Dhou al-Qi'da",
        "Dhou al-Hijja",
    )

    private val hijriMonthsEn = listOf(
        "Muharram",
        "Safar",
        "Rabi' al-Awwal",
        "Rabi' al-Thani",
        "Jumada al-Ula",
        "Jumada al-Akhirah",
        "Rajab",
        "Sha'ban",
        "Ramadan",
        "Shawwal",
        "Dhu al-Qi'dah",
        "Dhu al-Hijjah",
    )

    private val hijriMonthsAr = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الثاني",
        "جمادى الأولى",
        "جمادى الثانية",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة",
    )

    fun formatHijriDate(
        language: LauncherLanguage,
        timezoneId: String = TimeZone.getDefault().id,
        nowMillis: Long = System.currentTimeMillis(),
    ): String = runCatching {
        val icuTz = IcuTimeZone.getTimeZone(timezoneId)
        val islamicCal = IslamicCalendar(icuTz).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            timeInMillis = nowMillis
        }
        val day = islamicCal.get(IslamicCalendar.DAY_OF_MONTH)
        val monthIdx = islamicCal.get(IslamicCalendar.MONTH).coerceIn(0, 11)
        val year = islamicCal.get(IslamicCalendar.YEAR)
        val months = when (language) {
            LauncherLanguage.FR -> hijriMonthsFr
            LauncherLanguage.AR -> hijriMonthsAr
            LauncherLanguage.EN -> hijriMonthsEn
        }
        val monthName = months[monthIdx]
        if (language == LauncherLanguage.AR) {
            String.format(Locale.US, "%d %s %d هـ", day, monthName, year)
        } else {
            String.format(Locale.US, "%d %s %d", day, monthName, year)
        }
    }.getOrDefault("")

    fun computeNextPrayer(
        data: PrayerTimesData,
        nowMillis: Long = System.currentTimeMillis(),
        is24Hour: Boolean = true,
    ): NextPrayerInfo {
        val tz = runCatching { TimeZone.getTimeZone(data.timezone) }.getOrElse { TimeZone.getDefault() }
        val cal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val entries = data.entries
        val next = entries.firstOrNull { it.minutesFromMidnight > currentMinutes }
        val targetEntry = next ?: entries.first()
        val diffMinutes = if (next != null) {
            targetEntry.minutesFromMidnight - currentMinutes
        } else {
            (1440 - currentMinutes) + targetEntry.minutesFromMidnight
        }

        val hours = diffMinutes / 60
        val mins = diffMinutes % 60

        val formattedTime = if (is24Hour) {
            targetEntry.time24.take(5)
        } else {
            val parts = targetEntry.time24.take(5).split(":")
            val h24 = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val amPm = if (h24 >= 12) "PM" else "AM"
            val h12 = when {
                h24 == 0 -> 12
                h24 > 12 -> h24 - 12
                else -> h24
            }
            "$h12:${"%02d".format(Locale.US, m)} $amPm"
        }

        return NextPrayerInfo(
            key = targetEntry.key,
            timeFormatted = formattedTime,
            remainingHours = hours,
            remainingMinutes = mins,
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l)) / 15.0
        val eqT = q / 15.0 - fixHour(ra)
        val decl = asinDeg(sinDeg(e) * sinDeg(l))
        return decl to eqT
    }

    private fun midDay(jd: Double): Double {
        val eqT = sunPosition(jd + 0.5).second
        return fixHour(12.0 - eqT)
    }

    private fun sunAngleTime(
        jd: Double,
        angle: Double,
        latitude: Double,
        counterClockwise: Boolean,
    ): Double {
        val t = jd + if (counterClockwise) 0.25 else 0.75
        val decl = sunPosition(t).first
        val noon = midDay(jd)
        val numerator = -sinDeg(angle) - sinDeg(decl) * sinDeg(latitude)
        val denominator = cosDeg(decl) * cosDeg(latitude)
        val ratio = (numerator / denominator)
        if (ratio < -1.0 || ratio > 1.0) return Double.NaN
        val hourAngle = acosDeg(ratio) / 15.0
        return noon + if (counterClockwise) -hourAngle else hourAngle
    }

    private fun asrTime(jd: Double, factor: Double, latitude: Double): Double {
        val decl = sunPosition(jd + 0.54).first
        val angle = -atanDeg(1.0 / (factor + tanDeg(kotlin.math.abs(latitude - decl))))
        return sunAngleTime(jd, angle, latitude, counterClockwise = false)
    }

    private fun fixAngle(a: Double): Double = a - 360.0 * floor(a / 360.0)
    private fun fixHour(a: Double): Double = a - 24.0 * floor(a / 24.0)
    private fun degToRad(d: Double): Double = d * Math.PI / 180.0
    private fun radToDeg(r: Double): Double = r * 180.0 / Math.PI
    private fun sinDeg(d: Double): Double = sin(degToRad(d))
    private fun cosDeg(d: Double): Double = cos(degToRad(d))
    private fun tanDeg(d: Double): Double = tan(degToRad(d))
    private fun asinDeg(x: Double): Double = radToDeg(asin(x.coerceIn(-1.0, 1.0)))
    private fun acosDeg(x: Double): Double = radToDeg(acos(x.coerceIn(-1.0, 1.0)))
    private fun atanDeg(x: Double): Double = radToDeg(kotlin.math.atan(x))
    private fun atan2Deg(y: Double, x: Double): Double = radToDeg(atan2(y, x))
}
