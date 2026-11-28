package com.example.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.io.File
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GitHubReleaseAsset(
    val name: String? = null,
    @Json(name = "browser_download_url") val browserDownloadUrl: String? = null,
    val size: Long = 0L,
    @Json(name = "content_type") val contentType: String? = null,
)

@JsonClass(generateAdapter = true)
data class GitHubReleaseResponse(
    @Json(name = "tag_name") val tagName: String? = null,
    val name: String? = null,
    val body: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubReleaseAsset> = emptyList(),
)

data class AppUpdateInfo(
    val versionTag: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val htmlUrl: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
)

sealed interface UpdateCheckResult {
    data class UpdateAvailable(val info: AppUpdateInfo) : UpdateCheckResult
    data class UpToDate(val latestTag: String) : UpdateCheckResult
    data object RepoNotConfigured : UpdateCheckResult
    data class NoApkAssetFound(val latestTag: String, val htmlUrl: String) : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}

sealed interface AppUpdateUiState {
    data object Idle : AppUpdateUiState
    data object Checking : AppUpdateUiState
    data object RepoSetupRequired : AppUpdateUiState
    data class Available(val info: AppUpdateInfo) : AppUpdateUiState
    data class Downloading(
        val info: AppUpdateInfo,
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : AppUpdateUiState
    data class ReadyToInstall(
        val info: AppUpdateInfo,
        val apkFilePath: String,
        val needsInstallPermission: Boolean,
    ) : AppUpdateUiState
    data class SignatureMismatch(
        val info: AppUpdateInfo,
        val apkFilePath: String,
    ) : AppUpdateUiState
}

enum class ApkValidationFailure {
    CORRUPT_ARCHIVE,
    PACKAGE_MISMATCH,
    NOT_NEWER_THAN_INSTALLED,
    RELEASE_ASSET_OUTDATED,
    SIGNATURE_MISMATCH,
}

sealed interface DownloadedApkValidation {
    data class Valid(val versionCode: Long, val versionName: String) : DownloadedApkValidation
    data class Invalid(
        val failure: ApkValidationFailure,
        val archiveVersionName: String? = null,
        val archiveVersionCode: Long? = null,
    ) : DownloadedApkValidation
}

interface GitHubReleasesApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Accept") accept: String = "application/vnd.github+json",
        @Header("User-Agent") userAgent: String = "HorizonLauncher/${BuildConfig.VERSION_NAME}",
    ): GitHubReleaseResponse
}

class GitHubUpdateRepository(private val context: Context) {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val api: GitHubReleasesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubReleasesApi::class.java)
    }

    suspend fun checkLatestRelease(
        repoInput: String,
        currentVersionName: String = BuildConfig.VERSION_NAME,
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val slug = normalizeRepoSlug(repoInput) ?: return@withContext UpdateCheckResult.RepoNotConfigured
        val parts = slug.split("/")
        val owner = parts[0]
        val repo = parts[1]

        runCatching {
            val release = api.getLatestRelease(owner = owner, repo = repo)
            val tag = release.tagName?.trim().orEmpty()
            if (tag.isBlank() || release.draft) {
                return@runCatching UpdateCheckResult.UpToDate("v$currentVersionName")
            }
            if (!isNewerVersion(tag, currentVersionName)) {
                return@runCatching UpdateCheckResult.UpToDate(tag)
            }
            val apkAsset = selectBestApkAsset(release.assets)
                ?: return@runCatching UpdateCheckResult.NoApkAssetFound(
                    latestTag = tag,
                    htmlUrl = release.htmlUrl.orEmpty(),
                )
            val downloadUrl = apkAsset.browserDownloadUrl.orEmpty()
            if (downloadUrl.isBlank()) {
                return@runCatching UpdateCheckResult.NoApkAssetFound(
                    latestTag = tag,
                    htmlUrl = release.htmlUrl.orEmpty(),
                )
            }
            UpdateCheckResult.UpdateAvailable(
                AppUpdateInfo(
                    versionTag = tag,
                    releaseTitle = release.name?.takeIf { it.isNotBlank() } ?: "Horizon Launcher $tag",
                    releaseNotes = release.body?.trim().orEmpty(),
                    htmlUrl = release.htmlUrl.orEmpty(),
                    apkDownloadUrl = downloadUrl,
                    apkFileName = apkAsset.name ?: "HorizonLauncher.apk",
                    apkSizeBytes = apkAsset.size,
                )
            )
        }.getOrElse { throwable ->
            val msg = when (throwable) {
                is HttpException -> when (throwable.code()) {
                    404 -> "404"
                    else -> "HTTP ${throwable.code()}"
                }
                else -> throwable.localizedMessage ?: "Network error"
            }
            UpdateCheckResult.Error(msg)
        }
    }

    suspend fun downloadApk(
        info: AppUpdateInfo,
        onProgress: (percent: Int, downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            updatesDir.listFiles()?.forEach { old -> runCatching { old.delete() } }

            val targetFile = File(updatesDir, "HorizonLauncher-update.apk")
            val request = Request.Builder()
                .url(info.apkDownloadUrl)
                .header("User-Agent", "HorizonLauncher/${BuildConfig.VERSION_NAME}")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("HTTP ${response.code}")
                }
                val body = response.body ?: error("Empty download body")
                val totalBytes = body.contentLength().let { if (it > 0L) it else info.apkSizeBytes }

                body.byteStream().use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(32768)
                        var downloaded = 0L
                        var lastReportedPercent = -1
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            val percent = if (totalBytes > 0L) {
                                ((downloaded * 100L) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                0
                            }
                            if (percent != lastReportedPercent || downloaded == totalBytes) {
                                lastReportedPercent = percent
                                onProgress(percent, downloaded, totalBytes)
                            }
                        }
                        output.flush()
                    }
                }
            }
            require(targetFile.exists() && targetFile.length() > 0L) { "Empty APK file" }
            targetFile
        }
    }

    fun canInstallPackages(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            runCatching { context.packageManager.canRequestPackageInstalls() }.getOrDefault(true)
    }

    fun createUnknownSourcesSettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Suppress("DEPRECATION")
    fun validateDownloadedApk(
        apkFile: File,
        expectedVersionTag: String,
        currentVersionName: String = BuildConfig.VERSION_NAME,
        currentVersionCode: Long = BuildConfig.VERSION_CODE.toLong(),
    ): DownloadedApkValidation {
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            return DownloadedApkValidation.Invalid(ApkValidationFailure.CORRUPT_ARCHIVE)
        }
        val pkgInfo = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(0L),
                )
            } else {
                context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            }
        }.getOrNull() ?: return DownloadedApkValidation.Invalid(ApkValidationFailure.CORRUPT_ARCHIVE)

        val archiveVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.longVersionCode
        } else {
            pkgInfo.versionCode.toLong()
        }
        val signaturesMatch = hasMatchingSigningCertificate(apkFile.absolutePath)
        return validateArchiveMetadata(
            archivePackageName = pkgInfo.packageName,
            expectedPackageName = context.packageName,
            archiveVersionCode = archiveVersionCode,
            archiveVersionName = pkgInfo.versionName.orEmpty(),
            expectedVersionTag = expectedVersionTag,
            currentVersionCode = currentVersionCode,
            currentVersionName = currentVersionName,
            signaturesMatch = signaturesMatch,
        )
    }

    @Suppress("DEPRECATION")
    fun hasMatchingSigningCertificate(apkFilePath: String): Boolean {
        return runCatching {
            val installedCerts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
                extractSigningBytes(info)
            } else {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES,
                )
                extractSigningBytes(info)
            }
            val archiveCerts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageArchiveInfo(
                    apkFilePath,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
                info?.let { extractSigningBytes(it) }.orEmpty()
            } else {
                val info = context.packageManager.getPackageArchiveInfo(
                    apkFilePath,
                    PackageManager.GET_SIGNATURES,
                )
                info?.let { extractSigningBytes(it) }.orEmpty()
            }
            if (installedCerts.isEmpty() || archiveCerts.isEmpty()) {
                true
            } else {
                installedCerts.any { installed -> archiveCerts.any { archive -> installed.contentEquals(archive) } }
            }
        }.getOrDefault(true)
    }

    @Suppress("DEPRECATION")
    private fun extractSigningBytes(pkgInfo: android.content.pm.PackageInfo): List<ByteArray> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = pkgInfo.signingInfo
            if (signingInfo != null) {
                val signers = if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
                if (!signers.isNullOrEmpty()) {
                    return signers.map { it.toByteArray() }
                }
            }
        }
        return pkgInfo.signatures?.map { it.toByteArray() }.orEmpty()
    }

    fun createUninstallSelfIntent(): Intent {
        return Intent(
            Intent.ACTION_DELETE,
            Uri.parse("package:${context.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun launchApkInstaller(apkFilePath: String): Boolean {
        val apkFile = File(apkFilePath)
        if (!apkFile.exists() || apkFile.length() <= 0L) return false
        return runCatching {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile,
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    companion object {
        fun validateArchiveMetadata(
            archivePackageName: String?,
            expectedPackageName: String,
            archiveVersionCode: Long,
            archiveVersionName: String,
            expectedVersionTag: String,
            currentVersionCode: Long,
            currentVersionName: String,
            signaturesMatch: Boolean = true,
        ): DownloadedApkValidation {
            if (archivePackageName != expectedPackageName) {
                return DownloadedApkValidation.Invalid(
                    failure = ApkValidationFailure.PACKAGE_MISMATCH,
                    archiveVersionName = archiveVersionName.takeIf { it.isNotBlank() },
                    archiveVersionCode = archiveVersionCode,
                )
            }
            if (!signaturesMatch) {
                return DownloadedApkValidation.Invalid(
                    failure = ApkValidationFailure.SIGNATURE_MISMATCH,
                    archiveVersionName = archiveVersionName.takeIf { it.isNotBlank() },
                    archiveVersionCode = archiveVersionCode,
                )
            }
            val cleanName = archiveVersionName.trim()
            if (archiveVersionCode < currentVersionCode) {
                return DownloadedApkValidation.Invalid(
                    failure = ApkValidationFailure.NOT_NEWER_THAN_INSTALLED,
                    archiveVersionName = cleanName.takeIf { it.isNotBlank() },
                    archiveVersionCode = archiveVersionCode,
                )
            }
            val isNewerThanInstalled = archiveVersionCode > currentVersionCode ||
                (cleanName.isNotBlank() && isNewerVersion(cleanName, currentVersionName))
            if (!isNewerThanInstalled) {
                return DownloadedApkValidation.Invalid(
                    failure = ApkValidationFailure.NOT_NEWER_THAN_INSTALLED,
                    archiveVersionName = cleanName.takeIf { it.isNotBlank() },
                    archiveVersionCode = archiveVersionCode,
                )
            }
            if (cleanName.isNotBlank() && isNewerVersion(expectedVersionTag, cleanName)) {
                return DownloadedApkValidation.Invalid(
                    failure = ApkValidationFailure.RELEASE_ASSET_OUTDATED,
                    archiveVersionName = cleanName,
                    archiveVersionCode = archiveVersionCode,
                )
            }
            return DownloadedApkValidation.Valid(
                versionCode = archiveVersionCode,
                versionName = cleanName,
            )
        }
        private val REPO_SLUG_REGEX = Regex("^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+$")

        fun normalizeRepoSlug(rawInput: String): String? {
            var cleaned = rawInput.trim()
            if (cleaned.isEmpty()) return null
            cleaned = cleaned
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
            if (cleaned.startsWith("github.com/", ignoreCase = true)) {
                cleaned = cleaned.substringAfter("github.com/", "")
            }
            cleaned = cleaned.trim('/')
            if (cleaned.endsWith(".git", ignoreCase = true)) {
                cleaned = cleaned.dropLast(4)
            }
            val segments = cleaned.split("/").filter { it.isNotBlank() }
            if (segments.size < 2) return null
            val candidate = "${segments[0]}/${segments[1]}"
            return if (REPO_SLUG_REGEX.matches(candidate)) candidate else null
        }

        fun parseVersionParts(rawVersion: String): List<Int> {
            val cleaned = rawVersion.trim()
                .removePrefix("v")
                .removePrefix("V")
                .substringBefore("-")
                .substringBefore("+")
            if (cleaned.isBlank()) return emptyList()
            return cleaned.split(".").map { segment ->
                segment.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
            }
        }

        fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
            val remote = parseVersionParts(remoteTag)
            val current = parseVersionParts(currentVersion)
            if (remote.isEmpty() || current.isEmpty()) return false
            val maxLen = maxOf(remote.size, current.size)
            for (i in 0 until maxLen) {
                val r = remote.getOrElse(i) { 0 }
                val c = current.getOrElse(i) { 0 }
                if (r != c) return r > c
            }
            return false
        }

        fun selectBestApkAsset(assets: List<GitHubReleaseAsset>): GitHubReleaseAsset? {
            val apks = assets.filter { asset ->
                val name = asset.name.orEmpty().lowercase()
                val url = asset.browserDownloadUrl.orEmpty()
                name.endsWith(".apk") && url.isNotBlank()
            }
            if (apks.isEmpty()) return null
            return apks.firstOrNull { it.name.orEmpty().contains("horizon", ignoreCase = true) }
                ?: apks.firstOrNull { !it.name.orEmpty().contains("unsigned", ignoreCase = true) }
                ?: apks.first()
        }
    }
}
