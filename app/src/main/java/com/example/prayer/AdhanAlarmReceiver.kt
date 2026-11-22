package com.example.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.LauncherPreferencesRepository
import com.example.model.AdhanAlertMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AdhanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerKeyId = intent.getStringExtra(EXTRA_PRAYER_KEY) ?: "prayer"
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_LABEL) ?: "Prière"
        val cityName = intent.getStringExtra(EXTRA_CITY_LABEL) ?: ""
        val extraPlayAudio = intent.getBooleanExtra(EXTRA_PLAY_AUDIO, false)
        val extraCustomUri = intent.getStringExtra(EXTRA_CUSTOM_AUDIO_URI)
        val tzId = intent.getStringExtra(EXTRA_TIMEZONE_ID)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val settings = runCatching {
                    LauncherPreferencesRepository(context).settingsFlow.first()
                }.getOrNull()

                val effectiveTz = runCatching {
                    TimeZone.getTimeZone(settings?.timezoneId ?: tzId ?: TimeZone.getDefault().id)
                }.getOrElse { TimeZone.getDefault() }

                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                    timeZone = effectiveTz
                }.format(Date())

                if (AdhanPlaybackService.markPrayerTriggeredIfNew(context, prayerKeyId, dateKey)) {
                    val enabled = settings?.adhanEnabled ?: true
                    val playAudio = if (settings != null) {
                        enabled && settings.adhanMode == AdhanAlertMode.AUDIO_AND_NOTIFICATION
                    } else {
                        extraPlayAudio
                    }
                    val notifyOnly = if (settings != null) {
                        enabled && settings.adhanMode == AdhanAlertMode.NOTIFICATION_ONLY
                    } else {
                        !extraPlayAudio
                    }

                    val effectiveUri = settings?.customAdhanAudioUri ?: extraCustomUri
                    val effectiveCity = settings?.shortCityName ?: cityName

                    if (playAudio) {
                        AdhanPlaybackService.startAdhan(
                            context = context,
                            prayerName = prayerName,
                            customUri = effectiveUri,
                            cityName = effectiveCity,
                        )
                    } else if (notifyOnly) {
                        AdhanPlaybackService.showPrayerNotificationOnly(
                            context = context,
                            prayerName = prayerName,
                            cityName = effectiveCity,
                        )
                    }
                }

                // Reschedule tomorrow's prayers cleanly
                if (settings != null) {
                    val pt = OfflinePrayerCalculator.calculate(
                        latitude = settings.latitude,
                        longitude = settings.longitude,
                        timezoneId = settings.timezoneId,
                        methodId = settings.prayerMethodId,
                        cityDisplay = settings.shortCityName,
                    )
                    AdhanScheduler(context).scheduleDailyPrayers(pt, settings)
                }
            }
            pendingResult.finish()
        }
    }

    companion object {
        const val EXTRA_PRAYER_KEY = "extra_prayer_key"
        const val EXTRA_PRAYER_LABEL = "extra_prayer_label"
        const val EXTRA_CITY_LABEL = "extra_city_label"
        const val EXTRA_PLAY_AUDIO = "extra_play_audio"
        const val EXTRA_CUSTOM_AUDIO_URI = "extra_custom_audio_uri"
        const val EXTRA_TIMEZONE_ID = "extra_timezone_id"
    }
}
