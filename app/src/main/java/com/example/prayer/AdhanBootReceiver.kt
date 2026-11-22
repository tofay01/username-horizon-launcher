package com.example.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.LauncherPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdhanBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val settings = LauncherPreferencesRepository(context).settingsFlow.first()
                val data = OfflinePrayerCalculator.calculate(
                    latitude = settings.latitude,
                    longitude = settings.longitude,
                    timezoneId = settings.timezoneId,
                    methodId = settings.prayerMethodId,
                    cityDisplay = settings.shortCityName,
                )
                AdhanScheduler(context).scheduleDailyPrayers(data, settings)
            }
            pendingResult.finish()
        }
    }
}
