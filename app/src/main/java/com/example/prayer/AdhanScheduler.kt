package com.example.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.model.AdhanAlertMode
import com.example.model.LauncherSettings
import com.example.model.PrayerKey
import com.example.model.PrayerTimesData
import java.util.Calendar
import java.util.TimeZone

class AdhanScheduler(private val context: Context) {

    fun scheduleDailyPrayers(
        prayerTimes: PrayerTimesData,
        settings: LauncherSettings,
    ) {
        val alarmManager = runCatching {
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        }.getOrNull() ?: return
        cancelAll()

        if (!settings.adhanEnabled || settings.adhanMode == AdhanAlertMode.SILENT) return

        val tz = runCatching { TimeZone.getTimeZone(prayerTimes.timezone) }.getOrElse { TimeZone.getDefault() }
        val nowMillis = System.currentTimeMillis()
        val playAudio = settings.adhanMode == AdhanAlertMode.AUDIO_AND_NOTIFICATION

        prayerTimes.entries.forEachIndexed { index, entry ->
            if (!settings.enabledPrayers.contains(entry.key)) return@forEachIndexed

            val parts = entry.time24.take(5).split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@forEachIndexed
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: return@forEachIndexed

            val cal = Calendar.getInstance(tz).apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= nowMillis + 5000L) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_KEY, entry.key.id)
                putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_LABEL, entry.key.labelFr)
                putExtra(AdhanAlarmReceiver.EXTRA_CITY_LABEL, settings.shortCityName)
                putExtra(AdhanAlarmReceiver.EXTRA_PLAY_AUDIO, playAudio)
                putExtra(AdhanAlarmReceiver.EXTRA_CUSTOM_AUDIO_URI, settings.customAdhanAudioUri)
                putExtra(AdhanAlarmReceiver.EXTRA_TIMEZONE_ID, settings.timezoneId)
            }

            val pendingIntent = runCatching {
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE_BASE + index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            }.getOrNull() ?: return@forEachIndexed

            val canExact = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }
            }.getOrDefault(false)

            runCatching {
                if (canExact) {
                    val showIntent = PendingIntent.getActivity(
                        context,
                        REQUEST_CODE_BASE + 100 + index,
                        Intent(context, com.example.MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    )
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(cal.timeInMillis, showIntent),
                        pendingIntent,
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis,
                        pendingIntent,
                    )
                }
            }
        }
    }

    fun cancelAll() {
        runCatching {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            PrayerKey.entries.indices.forEach { index ->
                val intent = Intent(context, AdhanAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE_BASE + index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_BASE = 8800
    }
}
