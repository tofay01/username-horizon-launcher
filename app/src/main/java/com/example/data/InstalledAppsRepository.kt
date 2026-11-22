package com.example.data

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.model.AppItem
import com.example.model.NetworkStatus
import com.example.model.NetworkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    suspend fun loadInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPackage = context.packageName

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val resolved = LinkedHashMap<String, ResolveInfo>()
        querySafe(pm, leanbackIntent).forEach { info ->
            val pkg = info.activityInfo?.packageName ?: return@forEach
            if (pkg != selfPackage) resolved[pkg] = info
        }
        querySafe(pm, launcherIntent).forEach { info ->
            val pkg = info.activityInfo?.packageName ?: return@forEach
            if (pkg != selfPackage && !resolved.containsKey(pkg)) {
                resolved[pkg] = info
            }
        }

        resolved.values.mapNotNull { info ->
            runCatching {
                val activityInfo = info.activityInfo ?: return@runCatching null
                val pkg = activityInfo.packageName ?: return@runCatching null
                val label = runCatching { info.loadLabel(pm)?.toString()?.trim() }.getOrNull().orEmpty().ifBlank { pkg }
                val icon = runCatching { info.loadIcon(pm) }.getOrNull()
                val banner = runCatching { info.activityInfo.loadBanner(pm) }.getOrNull()
                val isSys = ((activityInfo.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM) != 0
                AppItem(
                    packageName = pkg,
                    label = label,
                    activityName = activityInfo.name.orEmpty(),
                    icon = icon,
                    banner = banner,
                    isSystemApp = isSys,
                )
            }.getOrNull()
        }.sortedBy { it.label.lowercase() }
    }

    @Suppress("DEPRECATION")
    private fun querySafe(pm: PackageManager, intent: Intent): List<ResolveInfo> {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
            } else {
                pm.queryIntentActivities(intent, 0)
            }
        }.getOrDefault(emptyList())
    }

    fun launchApp(packageName: String): Boolean {
        return runCatching {
            val pm = context.packageManager
            val intent = pm.getLeanbackLaunchIntentForPackage(packageName)
                ?: pm.getLaunchIntentForPackage(packageName)
                ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAppInfo(packageName: String): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    @Suppress("DEPRECATION")
    fun uninstallApp(packageName: String, launcherContext: Context? = null): Boolean {
        val targetContext = launcherContext ?: context
        val isActivity = targetContext is android.app.Activity
        val packageUri = Uri.fromParts("package", packageName, null)
        val candidates = listOf(
            Intent(Intent.ACTION_DELETE, packageUri).apply {
                if (!isActivity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri).apply {
                if (!isActivity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        for (intent in candidates) {
            val started = runCatching {
                targetContext.startActivity(intent)
                true
            }.getOrDefault(false)
            if (started) return true
        }
        return openAppInfo(packageName)
    }

    fun openSystemSettings(): Boolean {
        val candidates = mutableListOf(
            Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
        )
        context.packageManager.getLeanbackLaunchIntentForPackage("com.android.tv.settings")?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            candidates.add(it)
        }
        context.packageManager.getLaunchIntentForPackage("com.android.tv.settings")?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            candidates.add(it)
        }
        context.packageManager.getLaunchIntentForPackage("com.android.settings")?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            candidates.add(it)
        }
        for (intent in candidates) {
            val ok = runCatching {
                context.startActivity(intent)
                true
            }.getOrDefault(false)
            if (ok) return true
        }
        return false
    }

    fun openNetworkSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val started = runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
        if (!started) openSystemSettings()
    }

    fun isDefaultHomeLauncher(): Boolean = isDefaultHomeLauncher(context)

    fun getDefaultHomeRoleIntent(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return null
    }

    fun openDefaultHomeSettings(): Intent? {
        val roleIntent = getDefaultHomeRoleIntent()
        if (roleIntent != null) return roleIntent
        openDefaultHomeFallbackSettings()
        return null
    }

    fun openDefaultHomeFallbackSettings(): Boolean {
        val candidates = buildList {
            add(Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                add(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
            add(
                Intent.createChooser(
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                    },
                    null,
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
        for (intent in candidates) {
            val launched = runCatching {
                context.startActivity(intent)
                true
            }.getOrDefault(false)
            if (launched) return true
        }
        return openSystemSettings()
    }

    companion object {
        fun isDefaultHomeLauncher(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = runCatching { context.getSystemService(RoleManager::class.java) }.getOrNull()
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return true
                }
            }
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolved = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.resolveActivity(
                        homeIntent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                }
            }.getOrNull()
            return resolved?.activityInfo?.packageName == context.packageName
        }
    }

    fun getCurrentNetworkStatus(): NetworkStatus {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return NetworkStatus(NetworkType.OFFLINE, "Hors ligne")
            val network = cm.activeNetwork ?: return NetworkStatus(NetworkType.OFFLINE, "Hors ligne")
            val caps = cm.getNetworkCapabilities(network) ?: return NetworkStatus(NetworkType.OFFLINE, "Hors ligne")
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                    NetworkStatus(NetworkType.ETHERNET, "Ethernet")
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                    NetworkStatus(NetworkType.WIFI, "Wi-Fi")
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                    NetworkStatus(NetworkType.CELLULAR, "4G / 5G")
                else -> NetworkStatus(NetworkType.OFFLINE, "Hors ligne")
            }
        }.getOrDefault(NetworkStatus(NetworkType.OFFLINE, "Hors ligne"))
    }
}
