package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.model.LocalizedStrings
import com.example.ui.components.LocalTvFocusEnabled
import com.example.ui.components.TvFocusableSurface
import com.example.ui.icons.SystemUpdate
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import com.example.update.AppUpdateUiState
import com.example.update.GitHubUpdateRepository

@Composable
fun GitHubUpdateDialogHost(
    updateState: AppUpdateUiState,
    currentRepo: String,
    strings: LocalizedStrings,
    accent: Color,
    onSaveRepo: (String, Boolean) -> Boolean,
    onDownloadAndInstall: () -> Unit,
    onRetryInstall: () -> Unit,
    onRequestInstallPermission: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (updateState) {
        AppUpdateUiState.Idle, AppUpdateUiState.Checking -> Unit

        AppUpdateUiState.RepoSetupRequired -> {
            GitHubRepoConfigDialog(
                initialRepo = currentRepo,
                strings = strings,
                accent = accent,
                onSaveAndCheck = { raw -> onSaveRepo(raw, true) },
                onSaveOnly = { raw -> onSaveRepo(raw, false) },
                onDismiss = onDismiss,
            )
        }

        is AppUpdateUiState.Available,
        is AppUpdateUiState.Downloading,
        is AppUpdateUiState.ReadyToInstall -> {
            val info = when (updateState) {
                is AppUpdateUiState.Available -> updateState.info
                is AppUpdateUiState.Downloading -> updateState.info
                is AppUpdateUiState.ReadyToInstall -> updateState.info
                else -> return
            }
            val primaryFocusRequester = remember { FocusRequester() }
            LaunchedEffect(updateState::class) {
                androidx.compose.runtime.withFrameNanos { }
                runCatching { primaryFocusRequester.requestFocus() }
            }

            BackHandler {
                if (updateState !is AppUpdateUiState.Downloading) {
                    onDismiss()
                }
            }

            CompositionLocalProvider(LocalTvFocusEnabled provides true) {
                AlertDialog(
                    onDismissRequest = {
                        if (updateState !is AppUpdateUiState.Downloading) onDismiss()
                    },
                    containerColor = Color(0xFF0C1E44),
                    titleContentColor = PureWhite,
                    modifier = Modifier.testTag("github_update_dialog"),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SystemUpdate,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(28.dp),
                            )
                            Column {
                                Text(
                                    text = strings.updateAvailableTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PureWhite,
                                )
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME} → ${info.versionTag}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldGlow,
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            if (info.apkSizeBytes > 0L) {
                                val sizeMb = String.format(java.util.Locale.US, "%.1f MB", info.apkSizeBytes / (1024f * 1024f))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = accent.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                                ) {
                                    Text(
                                        text = "${info.apkFileName} · $sizeMb",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }

                            if (info.releaseNotes.isNotBlank()) {
                                Text(
                                    text = info.releaseNotes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSlate,
                                )
                            }

                            when (updateState) {
                                is AppUpdateUiState.Downloading -> {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${strings.updateDownloadingLabel} ${updateState.progressPercent}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = accent,
                                    )
                                    LinearProgressIndicator(
                                        progress = { (updateState.progressPercent / 100f).coerceIn(0f, 1f) },
                                        color = accent,
                                        trackColor = Color.White.copy(alpha = 0.16f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .testTag("github_update_progress"),
                                    )
                                }

                                is AppUpdateUiState.ReadyToInstall -> {
                                    if (updateState.needsInstallPermission) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0x33F59E0B),
                                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(10.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Info,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                Text(
                                                    text = strings.updatePermissionNeededDesc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = PureWhite,
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldGlow,
                                            )
                                            Text(
                                                text = strings.updateReadyToInstallLabel,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = EmeraldGlow,
                                            )
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    },
                    confirmButton = {
                        when (updateState) {
                            is AppUpdateUiState.Available -> {
                                TvFocusableSurface(
                                    onClick = onDownloadAndInstall,
                                    accentColor = accent,
                                    shape = RoundedCornerShape(12.dp),
                                    baseColor = accent.copy(alpha = 0.25f),
                                    focusedColor = accent.copy(alpha = 0.55f),
                                    isSelected = true,
                                    focusRequester = primaryFocusRequester,
                                    modifier = Modifier.testTag("github_update_download_btn"),
                                ) {
                                    Text(
                                        text = strings.updateDownloadInstallBtn,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    )
                                }
                            }

                            is AppUpdateUiState.Downloading -> Unit

                            is AppUpdateUiState.ReadyToInstall -> {
                                if (updateState.needsInstallPermission) {
                                    TvFocusableSurface(
                                        onClick = onRequestInstallPermission,
                                        accentColor = Color(0xFFF59E0B),
                                        shape = RoundedCornerShape(12.dp),
                                        baseColor = Color(0x33F59E0B),
                                        focusedColor = Color(0x66F59E0B),
                                        isSelected = true,
                                        focusRequester = primaryFocusRequester,
                                        modifier = Modifier.testTag("github_update_permission_btn"),
                                    ) {
                                        Text(
                                            text = strings.updateGrantPermissionBtn,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        )
                                    }
                                } else {
                                    TvFocusableSurface(
                                        onClick = onRetryInstall,
                                        accentColor = EmeraldGlow,
                                        shape = RoundedCornerShape(12.dp),
                                        baseColor = EmeraldGlow.copy(alpha = 0.28f),
                                        focusedColor = EmeraldGlow.copy(alpha = 0.55f),
                                        isSelected = true,
                                        focusRequester = primaryFocusRequester,
                                        modifier = Modifier.testTag("github_update_install_btn"),
                                    ) {
                                        Text(
                                            text = strings.updateInstallNowBtn,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        )
                                    }
                                }
                            }

                            else -> Unit
                        }
                    },
                    dismissButton = {
                        if (updateState !is AppUpdateUiState.Downloading) {
                            TvFocusableSurface(
                                onClick = onDismiss,
                                accentColor = accent,
                                shape = RoundedCornerShape(12.dp),
                                baseColor = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.testTag("github_update_later_btn"),
                            ) {
                                Text(
                                    text = strings.updateLaterBtn,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MutedSlate,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun GitHubRepoConfigDialog(
    initialRepo: String,
    strings: LocalizedStrings,
    accent: Color,
    onSaveAndCheck: (String) -> Boolean,
    onSaveOnly: (String) -> Boolean,
    onDismiss: () -> Unit,
) {
    BackHandler { onDismiss() }

    var repoText by remember(initialRepo) { mutableStateOf(initialRepo) }
    var showError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent,
        unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
        focusedLabelColor = accent,
        unfocusedLabelColor = MutedSlate,
        focusedContainerColor = GlassBlue.copy(alpha = 0.45f),
        unfocusedContainerColor = GlassBlue.copy(alpha = 0.35f),
    )

    CompositionLocalProvider(LocalTvFocusEnabled provides true) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF0C1E44),
            titleContentColor = PureWhite,
            modifier = Modifier.testTag("github_repo_dialog"),
            title = {
                Text(
                    text = strings.githubRepoTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = strings.githubRepoDialogDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                    )
                    OutlinedTextField(
                        value = repoText,
                        onValueChange = {
                            repoText = it
                            showError = false
                        },
                        label = { Text(strings.githubRepoInputLabel) },
                        placeholder = { Text("username/horizon-launcher", color = MutedSlate) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_repo_input"),
                    )
                    if (showError) {
                        Text(
                            text = strings.githubRepoSaveInvalid,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF43F5E),
                        )
                    }
                }
            },
            confirmButton = {
                TvFocusableSurface(
                    onClick = {
                        val trimmed = repoText.trim()
                        if (trimmed.isNotEmpty() && GitHubUpdateRepository.normalizeRepoSlug(trimmed) == null) {
                            showError = true
                        } else if (!onSaveAndCheck(trimmed)) {
                            showError = true
                        }
                    },
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = accent.copy(alpha = 0.25f),
                    focusedColor = accent.copy(alpha = 0.55f),
                    isSelected = true,
                    modifier = Modifier.testTag("github_repo_save_check_btn"),
                ) {
                    Text(
                        text = strings.githubRepoSaveAndCheckBtn,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TvFocusableSurface(
                        onClick = {
                            val trimmed = repoText.trim()
                            if (trimmed.isNotEmpty() && GitHubUpdateRepository.normalizeRepoSlug(trimmed) == null) {
                                showError = true
                            } else if (onSaveOnly(trimmed)) {
                                onDismiss()
                            } else {
                                showError = true
                            }
                        },
                        accentColor = accent,
                        shape = RoundedCornerShape(12.dp),
                        baseColor = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.testTag("github_repo_save_btn"),
                    ) {
                        Text(
                            text = strings.saveAndClose,
                            style = MaterialTheme.typography.labelLarge,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    TvFocusableSurface(
                        onClick = onDismiss,
                        accentColor = accent,
                        shape = RoundedCornerShape(12.dp),
                        baseColor = Color.White.copy(alpha = 0.06f),
                        modifier = Modifier.testTag("github_repo_cancel_btn"),
                    ) {
                        Text(
                            text = strings.aboutCancelLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MutedSlate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }
            },
        )
    }
}
