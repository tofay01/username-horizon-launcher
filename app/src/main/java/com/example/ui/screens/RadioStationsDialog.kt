package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RadioRepository
import com.example.model.LocalizedStrings
import com.example.model.RadioPlaybackStatus
import com.example.model.RadioStation
import com.example.ui.LauncherUiState
import com.example.ui.components.LocalTvFocusEnabled
import com.example.ui.components.TvFocusableSurface
import com.example.ui.icons.GraphicEq
import com.example.ui.icons.Radio
import com.example.ui.icons.StarBorder
import com.example.ui.icons.Stop
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite

private enum class RadioDialogTab {
    LOCAL,
    FAVORITES,
    WORLD,
}

@Composable
fun RadioStationsDialog(
    state: LauncherUiState,
    strings: LocalizedStrings,
    accent: Color,
    onPlayStation: (RadioStation) -> Unit,
    onTogglePlayStop: () -> Unit,
    onRefresh: () -> Unit,
    onSelectCountry: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onToggleFavoriteStation: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val settings = state.settings
    val favoriteIds = settings.favoriteRadioStationIds
    var selectedTab by rememberSaveable { mutableStateOf(RadioDialogTab.LOCAL) }
    var searchText by rememberSaveable(state.radioSearchQuery) { mutableStateOf(state.radioSearchQuery) }
    val primaryFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        runCatching { primaryFocus.requestFocus() }
    }

    val displayedStations = remember(state.radioStations, selectedTab, favoriteIds, searchText) {
        val base = when (selectedTab) {
            RadioDialogTab.FAVORITES -> state.radioStations.filter { favoriteIds.contains(it.stationUuid) }
            RadioDialogTab.LOCAL, RadioDialogTab.WORLD -> state.radioStations
        }
        val trimmed = searchText.trim()
        if (trimmed.isEmpty()) {
            base
        } else {
            val filteredLocal = base.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                    it.state.contains(trimmed, ignoreCase = true) ||
                    it.tags.contains(trimmed, ignoreCase = true)
            }
            filteredLocal.ifEmpty { base }
        }
    }

    val detectedCountryLabel = remember(state.radioDetectedCountryCode, settings.language) {
        RadioRepository.countryOptionForCode(state.radioDetectedCountryCode)?.displayName(settings.language)
            ?: state.radioDetectedCountryCode
    }

    CompositionLocalProvider(LocalTvFocusEnabled provides true) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0A1B3D).copy(alpha = 0.96f),
                border = BorderStroke(1.5.dp, accent.copy(alpha = 0.65f)),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .testTag("radio_stations_dialog"),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                ) {
                    // Header row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.22f)),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Radio,
                                    contentDescription = strings.radioTitle,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = strings.radioStationsTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${strings.radioDetectedLocationPrefix} · ${settings.shortCityName} ($detectedCountryLabel)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TvFocusableSurface(
                                onClick = onRefresh,
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.09f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("radio_refresh_btn"),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = strings.radioRefreshAction,
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            TvFocusableSurface(
                                onClick = onDismiss,
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.09f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("radio_close_btn"),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = strings.backLabel,
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Active / Now Playing bar
                    val activeStation = state.activeRadioStation
                    val isLiveOrLoading = state.radioStatus == RadioPlaybackStatus.PLAYING ||
                        state.radioStatus == RadioPlaybackStatus.LOADING
                    if (activeStation != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isLiveOrLoading) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.07f),
                            border = BorderStroke(
                                1.dp,
                                if (state.radioStatus == RadioPlaybackStatus.ERROR) Color(0xFFF87171).copy(alpha = 0.65f)
                                else accent.copy(alpha = 0.45f),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("radio_now_playing_card"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = if (state.radioStatus == RadioPlaybackStatus.PLAYING) Icons.Rounded.GraphicEq else Icons.Rounded.Radio,
                                        contentDescription = null,
                                        tint = if (state.radioStatus == RadioPlaybackStatus.PLAYING) EmeraldGlow else accent,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeStation.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = PureWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        val statusSubtitle = when (state.radioStatus) {
                                            RadioPlaybackStatus.PLAYING -> {
                                                val meta = listOfNotNull(
                                                    strings.radioLiveBadge,
                                                    activeStation.codec.takeIf { it.isNotBlank() },
                                                    activeStation.bitrate.takeIf { it > 0 }?.let { "$it kbps" },
                                                ).joinToString(" · ")
                                                meta
                                            }
                                            RadioPlaybackStatus.LOADING -> strings.radioBufferingLabel
                                            RadioPlaybackStatus.ERROR -> strings.radioStreamError
                                            RadioPlaybackStatus.IDLE -> activeStation.country.ifBlank { activeStation.countryCode }
                                        }
                                        if (statusSubtitle.isNotBlank()) {
                                            Text(
                                                text = statusSubtitle,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = when (state.radioStatus) {
                                                    RadioPlaybackStatus.PLAYING -> EmeraldGlow
                                                    RadioPlaybackStatus.ERROR -> Color(0xFFFCA5A5)
                                                    else -> MutedSlate
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                TvFocusableSurface(
                                    onClick = onTogglePlayStop,
                                    accentColor = accent,
                                    shape = RoundedCornerShape(12.dp),
                                    baseColor = if (isLiveOrLoading) Color(0xFFDC2626).copy(alpha = 0.85f) else accent.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                        .testTag("radio_dialog_play_stop_btn"),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (isLiveOrLoading) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                            contentDescription = if (isLiveOrLoading) strings.radioStopAction else strings.radioPlayAction,
                                            tint = PureWhite,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = if (isLiveOrLoading) strings.radioStopAction else strings.radioPlayAction,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = PureWhite,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Filter tabs + Country pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        RadioFilterChip(
                            label = "${strings.radioLocalTab} (${state.radioDetectedCountryCode})",
                            selected = selectedTab == RadioDialogTab.LOCAL && state.radioFilterCountryCode == state.radioDetectedCountryCode,
                            accent = accent,
                            focusRequester = primaryFocus,
                            testTag = "radio_tab_local",
                            onClick = {
                                selectedTab = RadioDialogTab.LOCAL
                                onSelectCountry(state.radioDetectedCountryCode)
                            },
                        )
                        RadioFilterChip(
                            label = "${strings.radioFavoritesTab} (${favoriteIds.size})",
                            selected = selectedTab == RadioDialogTab.FAVORITES,
                            accent = accent,
                            testTag = "radio_tab_favorites",
                            onClick = { selectedTab = RadioDialogTab.FAVORITES },
                        )
                        RadioFilterChip(
                            label = strings.radioAllCountriesTab,
                            selected = selectedTab == RadioDialogTab.WORLD && state.radioFilterCountryCode == null,
                            accent = accent,
                            testTag = "radio_tab_world",
                            onClick = {
                                selectedTab = RadioDialogTab.WORLD
                                onSelectCountry(null)
                            },
                        )

                        RadioRepository.QUICK_COUNTRIES.forEach { opt ->
                            if (!opt.code.equals(state.radioDetectedCountryCode, ignoreCase = true)) {
                                RadioFilterChip(
                                    label = opt.displayName(settings.language),
                                    selected = selectedTab != RadioDialogTab.FAVORITES &&
                                        state.radioFilterCountryCode.equals(opt.code, ignoreCase = true),
                                    accent = accent,
                                    testTag = "radio_country_chip_${opt.code}",
                                    onClick = {
                                        selectedTab = RadioDialogTab.WORLD
                                        onSelectCountry(opt.code)
                                    },
                                )
                            }
                        }
                    }

                    // Search field
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = strings.radioSearchHint,
                                color = MutedSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = accent,
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                TvFocusableSurface(
                                    onClick = {
                                        searchText = ""
                                        onSearch("")
                                    },
                                    accentColor = accent,
                                    shape = CircleShape,
                                    baseColor = Color.Transparent,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                            tint = MutedSlate,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(searchText) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = accent,
                            unfocusedBorderColor = PureWhite.copy(alpha = 0.25f),
                            cursorColor = accent,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("radio_search_input"),
                    )

                    // Stations list / states
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 190.dp, max = 300.dp),
                    ) {
                        when {
                            state.isLoadingRadio -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .testTag("radio_loading_indicator"),
                                ) {
                                    CircularProgressIndicator(color = accent, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = strings.radioLoadingLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSlate,
                                    )
                                }
                            }
                            !state.radioError.isNullOrBlank() && displayedStations.isEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .testTag("radio_error_state"),
                                ) {
                                    Text(
                                        text = strings.radioOfflineError,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFFCA5A5),
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TextButton(onClick = onRefresh) {
                                        Text(strings.radioRefreshAction, color = accent)
                                    }
                                }
                            }
                            displayedStations.isEmpty() -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .testTag("radio_empty_state"),
                                ) {
                                    Text(
                                        text = strings.radioEmptyResults,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSlate,
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 300.dp)
                                        .testTag("radio_stations_list"),
                                ) {
                                    items(displayedStations, key = { it.stationUuid }) { station ->
                                        val isCurrent = activeStation?.stationUuid == station.stationUuid
                                        val isStationPlaying = isCurrent &&
                                            (state.radioStatus == RadioPlaybackStatus.PLAYING || state.radioStatus == RadioPlaybackStatus.LOADING)
                                        val isFav = favoriteIds.contains(station.stationUuid)
                                        RadioStationRow(
                                            station = station,
                                            isActive = isStationPlaying,
                                            isFavorite = isFav,
                                            accent = accent,
                                            strings = strings,
                                            onClick = { onPlayStation(station) },
                                            onToggleFavorite = { onToggleFavoriteStation(station.stationUuid) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioFilterChip(
    label: String,
    selected: Boolean,
    accent: Color,
    testTag: String,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
) {
    TvFocusableSurface(
        onClick = onClick,
        accentColor = accent,
        shape = RoundedCornerShape(12.dp),
        baseColor = if (selected) accent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
        isSelected = selected,
        focusRequester = focusRequester,
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                color = PureWhite,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RadioStationRow(
    station: RadioStation,
    isActive: Boolean,
    isFavorite: Boolean,
    accent: Color,
    strings: LocalizedStrings,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        TvFocusableSurface(
            onClick = onClick,
            accentColor = accent,
            shape = RoundedCornerShape(14.dp),
            baseColor = if (isActive) accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
            isSelected = isActive,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 52.dp)
                .testTag("radio_station_item_${station.stationUuid}"),
        ) { isFocused ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) EmeraldGlow.copy(alpha = 0.22f)
                            else accent.copy(alpha = if (isFocused) 0.34f else 0.18f),
                        ),
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                        contentDescription = strings.radioPlayAction,
                        tint = if (isActive) EmeraldGlow else PureWhite,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val details = buildList {
                        station.state.takeIf { it.isNotBlank() }?.let { add(it) }
                        station.country.takeIf { it.isNotBlank() }?.let { add(it) }
                        station.codec.takeIf { it.isNotBlank() }?.let { add(it) }
                        if (station.bitrate > 0) add("${station.bitrate} kbps")
                    }.joinToString(" · ")
                    if (details.isNotBlank()) {
                        Text(
                            text = details,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActive) EmeraldGlow else MutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        TvFocusableSurface(
            onClick = onToggleFavorite,
            accentColor = accent,
            shape = RoundedCornerShape(14.dp),
            baseColor = Color.White.copy(alpha = 0.07f),
            modifier = Modifier
                .size(48.dp)
                .testTag("radio_fav_btn_${station.stationUuid}"),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = if (isFavorite) strings.radioFavoriteRemove else strings.radioFavoriteAdd,
                    tint = if (isFavorite) Color(0xFFFACC15) else PureWhite.copy(alpha = 0.72f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
