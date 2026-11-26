package com.example.data

import android.content.Context
import android.net.Uri
import com.example.R
import com.example.model.LauncherLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class OnlineVideoWallpaperSpec(
    val id: String,
    val wikimediaFileTitle: String,
    val fallbackStreamUrl: String,
    val posterResId: Int,
    val titleFr: String,
    val titleAr: String,
    val titleEn: String,
) {
    fun localizedTitle(language: LauncherLanguage): String = when (language) {
        LauncherLanguage.FR -> titleFr
        LauncherLanguage.AR -> titleAr
        LauncherLanguage.EN -> titleEn
    }
}

object OnlineVideoWallpaperRepository {
    const val USER_AGENT = "HorizonTVLauncher/1.0 (AndroidTV; Media3)"
    private const val MAX_CACHE_BYTES = 45L * 1024L * 1024L
    private const val MIN_VALID_CACHE_BYTES = 64L * 1024L

    val presets: List<OnlineVideoWallpaperSpec> = listOf(
        OnlineVideoWallpaperSpec(
            id = "online_ocean_big_sur",
            wikimediaFileTitle = "File:Waves_off_of_Hurricane_Point_Vista_Point,_Big_Sur.webm",
            fallbackStreamUrl = "https://upload.wikimedia.org/wikipedia/commons/8/8e/Waves_off_of_Hurricane_Point_Vista_Point%2C_Big_Sur.webm",
            posterResId = R.drawable.img_video_ocean_waves,
            titleFr = "Vagues de l'Océan · Big Sur",
            titleAr = "أمواج المحيط · بيغ سور",
            titleEn = "Ocean Waves · Big Sur",
        ),
        OnlineVideoWallpaperSpec(
            id = "online_sand_beach_waves",
            wikimediaFileTitle = "File:Aerial_view_of_sand_beach._Top_view_sea_waves._Drone_footage.webm",
            fallbackStreamUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d7/Aerial_view_of_sand_beach._Top_view_sea_waves._Drone_footage.webm",
            posterResId = R.drawable.img_video_sand_beach,
            titleFr = "Plage de Sable · Vue Aérienne",
            titleAr = "شاطئ رملي وأمواج البحر",
            titleEn = "Sandy Beach · Aerial Waves",
        ),
        OnlineVideoWallpaperSpec(
            id = "online_adriatic_coast",
            wikimediaFileTitle = "File:A_Walk_Through_Top_Piran_Beach,_Slovenia_-_Stunning_Adriatic_Coastal_Views_in_4K.webm",
            fallbackStreamUrl = "https://upload.wikimedia.org/wikipedia/commons/5/52/A_Walk_Through_Top_Piran_Beach%2C_Slovenia_-_Stunning_Adriatic_Coastal_Views_in_4K.webm",
            posterResId = R.drawable.img_video_adriatic_coast,
            titleFr = "Rivage & Plage Adriatique",
            titleAr = "ساحل البحر الأدرياتيكي",
            titleEn = "Adriatic Coastal Beach",
        ),
        OnlineVideoWallpaperSpec(
            id = "online_forest_waterfall",
            wikimediaFileTitle = "File:Vrabchanski_waterfall_video.webm",
            fallbackStreamUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Vrabchanski_waterfall_video.webm",
            posterResId = R.drawable.img_video_forest_waterfall,
            titleFr = "Cascade en Forêt",
            titleAr = "شلال الغابة الخضراء",
            titleEn = "Forest Waterfall",
        ),
        OnlineVideoWallpaperSpec(
            id = "online_emerald_forest",
            wikimediaFileTitle = "File:Aerial_views_of_a_forest_in_Belarus.webm",
            fallbackStreamUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Aerial_views_of_a_forest_in_Belarus.webm",
            posterResId = R.drawable.img_video_emerald_forest,
            titleFr = "Forêt Émeraude · Vue Aérienne",
            titleAr = "غابة زمردية · تصوير جوي",
            titleEn = "Emerald Forest · Aerial",
        ),
    )

    private val presetsById: Map<String, OnlineVideoWallpaperSpec> = presets.associateBy { it.id }
    private val resolvedStreamCache = ConcurrentHashMap<String, String>()
    private val cachingInFlight = ConcurrentHashMap.newKeySet<String>()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    fun findById(wallpaperId: String): OnlineVideoWallpaperSpec? = presetsById[wallpaperId]

    fun isOnlineVideoWallpaper(wallpaperId: String): Boolean = presetsById.containsKey(wallpaperId)

    fun posterDrawableRes(wallpaperId: String): Int? = presetsById[wallpaperId]?.posterResId

    fun cachedFile(context: Context, wallpaperId: String): File {
        val dir = File(context.filesDir, "online_wallpapers").apply { mkdirs() }
        return File(dir, "$wallpaperId.webm")
    }

    fun immediateUriIfCached(context: Context, wallpaperId: String): String? {
        if (!isOnlineVideoWallpaper(wallpaperId)) return null
        val file = cachedFile(context, wallpaperId)
        if (file.exists() && file.length() >= MIN_VALID_CACHE_BYTES) {
            return Uri.fromFile(file).toString()
        }
        return resolvedStreamCache[wallpaperId]
    }

    suspend fun resolvePlayableUri(context: Context, wallpaperId: String): String? {
        val spec = presetsById[wallpaperId] ?: return null
        val localFile = cachedFile(context, wallpaperId)
        if (localFile.exists() && localFile.length() >= MIN_VALID_CACHE_BYTES) {
            return Uri.fromFile(localFile).toString()
        }
        resolvedStreamCache[wallpaperId]?.let { return it }

        val resolved = withContext(Dispatchers.IO) {
            queryOptimalWikimediaStreamUrl(spec.wikimediaFileTitle) ?: spec.fallbackStreamUrl
        }
        resolvedStreamCache[wallpaperId] = resolved
        return resolved
    }

    suspend fun cacheStreamInBackground(context: Context, wallpaperId: String, streamUrl: String) {
        if (!isOnlineVideoWallpaper(wallpaperId)) return
        if (!streamUrl.startsWith("http://") && !streamUrl.startsWith("https://")) return
        val destFile = cachedFile(context, wallpaperId)
        if (destFile.exists() && destFile.length() >= MIN_VALID_CACHE_BYTES) return
        if (!cachingInFlight.add(wallpaperId)) return

        try {
            withContext(Dispatchers.IO) {
                runCatching {
                    val request = Request.Builder()
                        .url(streamUrl)
                        .header("User-Agent", USER_AGENT)
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@use
                        val body = response.body ?: return@use
                        val declaredLen = body.contentLength()
                        if (declaredLen > MAX_CACHE_BYTES) return@use

                        val tempFile = File(destFile.parentFile, "$wallpaperId.tmp")
                        var copied = 0L
                        body.byteStream().use { input ->
                            tempFile.outputStream().use { output ->
                                val buffer = ByteArray(32 * 1024)
                                while (true) {
                                    val read = input.read(buffer)
                                    if (read <= 0) break
                                    copied += read
                                    if (copied > MAX_CACHE_BYTES) {
                                        runCatching { tempFile.delete() }
                                        return@use
                                    }
                                    output.write(buffer, 0, read)
                                }
                            }
                        }
                        if (copied >= MIN_VALID_CACHE_BYTES) {
                            if (destFile.exists()) destFile.delete()
                            tempFile.renameTo(destFile)
                        } else {
                            runCatching { tempFile.delete() }
                        }
                    }
                }
            }
        } finally {
            cachingInFlight.remove(wallpaperId)
        }
    }

    internal fun selectBestStreamFromApiJson(jsonText: String): String? = runCatching {
        val root = JSONObject(jsonText)
        val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: return null
        val pageKey = pages.keys().asSequence().firstOrNull() ?: return null
        val videoInfoArr = pages.optJSONObject(pageKey)?.optJSONArray("videoinfo") ?: return null
        if (videoInfoArr.length() == 0) return null
        val info = videoInfoArr.getJSONObject(0)

        val derivatives = info.optJSONArray("derivatives")
        var bestSrc: String? = null
        var bestScore = Int.MIN_VALUE

        if (derivatives != null) {
            for (i in 0 until derivatives.length()) {
                val item = derivatives.optJSONObject(i) ?: continue
                val src = normalizeHttpsUrl(item.optString("src")) ?: continue
                val type = item.optString("type").lowercase()
                val height = item.optInt("height", 0)
                if (height !in 360..1080) continue
                val codecScore = when {
                    type.contains("mp4") -> 400
                    type.contains("vp9") -> 300
                    type.contains("vp8") -> 220
                    type.contains("webm") -> 200
                    else -> 50
                }
                val resScore = when (height) {
                    in 720..1080 -> 300 + (height / 10)
                    in 480..719 -> 200 + (height / 10)
                    else -> 100
                }
                val total = codecScore + resScore
                if (total > bestScore) {
                    bestScore = total
                    bestSrc = src
                }
            }
        }

        bestSrc ?: normalizeHttpsUrl(info.optString("url"))
    }.getOrNull()

    private fun normalizeHttpsUrl(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        return when {
            trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("//") -> "https:$trimmed"
            trimmed.startsWith("http://") -> "https://${trimmed.removePrefix("http://")}"
            else -> null
        }
    }

    private fun queryOptimalWikimediaStreamUrl(fileTitle: String): String? = runCatching {
        val encoded = URLEncoder.encode(fileTitle, "UTF-8")
        val apiUrl =
            "https://commons.wikimedia.org/w/api.php?action=query&titles=$encoded&prop=videoinfo&viprop=url%7Cderivatives&format=json"
        val request = Request.Builder()
            .url(apiUrl)
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            selectBestStreamFromApiJson(body)
        }
    }.getOrNull()
}
