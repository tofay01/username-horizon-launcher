package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import kotlin.system.exitProcess

class HorizonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.FINGERPRINT.equals("robolectric", ignoreCase = true)) return

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val now = System.currentTimeMillis()
            val prefs = runCatching {
                getSharedPreferences(CRASH_PREFS, Context.MODE_PRIVATE)
            }.getOrNull()
            val lastTime = prefs?.getLong(KEY_LAST_CRASH_TIME, 0L) ?: 0L

            // Prevent infinite restart loop if crash happens within 2 seconds repeatedly
            if (now - lastTime < 2000L) {
                previousHandler?.uncaughtException(thread, throwable)
                return@setDefaultUncaughtExceptionHandler
            }

            val trace = runCatching { Log.getStackTraceString(throwable) }
                .getOrDefault(throwable.toString())
            val summary = "Thread: ${thread.name}\nSDK: ${Build.VERSION.SDK_INT} (${Build.MANUFACTURER} ${Build.MODEL})\n\n$trace"

            runCatching {
                prefs?.edit()
                    ?.putString(KEY_LAST_CRASH, summary)
                    ?.putLong(KEY_LAST_CRASH_TIME, now)
                    ?.commit()
            }

            val launched = runCatching {
                val intent = Intent(this, CrashDiagnosticActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(CrashDiagnosticActivity.EXTRA_TRACE, summary)
                }
                startActivity(intent)
                true
            }.getOrDefault(false)

            if (launched) {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            } else {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    companion object {
        const val CRASH_PREFS = "horizon_crash_log"
        const val KEY_LAST_CRASH = "last_crash"
        const val KEY_LAST_CRASH_TIME = "last_crash_time"
    }
}
