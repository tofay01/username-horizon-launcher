package com.example.power

import android.accessibilityservice.AccessibilityService
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

class HorizonDeviceAdminReceiver : DeviceAdminReceiver()

class HorizonPowerAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event monitoring needed; only used for user-triggered TV lock/sleep global action.
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (instanceRef?.get() === this) {
            instanceRef = null
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (instanceRef?.get() === this) {
            instanceRef = null
        }
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var instanceRef: WeakReference<HorizonPowerAccessibilityService>? = null

        fun isConnected(): Boolean = instanceRef?.get() != null

        fun performSleepOrPowerMenu(): Boolean {
            val service = instanceRef?.get() ?: return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ||
                    service.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            } else {
                service.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            }
        }
    }
}

enum class TvPowerOffResult {
    EXECUTED,
    NEEDS_SETUP,
}

object TvPowerController {

    fun deviceAdminComponent(context: Context): ComponentName {
        return ComponentName(context, HorizonDeviceAdminReceiver::class.java)
    }

    fun isDeviceAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return runCatching { dpm.isAdminActive(deviceAdminComponent(context)) }.getOrDefault(false)
    }

    fun canRequestDeviceAdmin(context: Context): Boolean {
        val intent = createEnableDeviceAdminIntent(context, "")
        return runCatching {
            intent.resolveActivity(context.packageManager) != null
        }.getOrDefault(false)
    }

    fun createEnableDeviceAdminIntent(context: Context, explanation: String): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent(context))
            if (explanation.isNotBlank()) {
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)
            }
        }
    }

    fun createAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun powerOffOrSleepTv(context: Context): TvPowerOffResult {
        if (HorizonPowerAccessibilityService.performSleepOrPowerMenu()) {
            return TvPowerOffResult.EXECUTED
        }
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        if (dpm != null && isDeviceAdminActive(context)) {
            val locked = runCatching {
                dpm.lockNow()
                true
            }.getOrDefault(false)
            if (locked) {
                return TvPowerOffResult.EXECUTED
            }
        }
        return TvPowerOffResult.NEEDS_SETUP
    }
}
