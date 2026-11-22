package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.InstalledAppsRepository
import com.example.model.AppItem
import com.example.model.LauncherSettings
import com.example.model.LauncherStrings
import com.example.ui.LauncherUiState
import com.example.ui.components.AppLibraryTileCard
import com.example.ui.components.GlassSettingCardRow
import com.example.ui.components.LocalTvFocusEnabled
import com.example.ui.components.TvFocusableSurface
import com.example.ui.icons.Apps
import com.example.ui.icons.ArrowDownward
import com.example.ui.icons.ArrowUpward
import com.example.ui.icons.StarBorder
import com.example.ui.icons.Visibility
import com.example.ui.icons.VisibilityOff
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite

@Composable
private fun SubScreenScaffold(
    title: String,
    accent: Color,
    backLabel: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    BackHandler { onBack() }
    val backFocusRequester = remember { FocusRequester() }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        runCatching { backFocusRequester.requestFocus() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xF0081B44), Color(0xF6040D22))))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1040.dp)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TvFocusableSurface(
                    onClick = onBack,
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    focusRequester = backFocusRequester,
                    modifier = Modifier.size(48.dp).testTag("subscreen_back_button"),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = backLabel,
                            tint = PureWhite,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = PureWhite,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun AppLibraryScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onOpenContextApp: (AppItem) -> Unit,
) {
    BackHandler { onBack() }
    val strings = LauncherStrings.get(state.settings.language)
    val accent = state.settings.accentColor
    val surfaceHex = Color(state.settings.themePreset.surfaceHex)
    val favPackages = remember(state.favoriteApps) { state.favoriteApps.map { it.packageName }.toSet() }
    val closeFocusRequester = remember { FocusRequester() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        runCatching { closeFocusRequester.requestFocus() }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalTvFocusEnabled provides true) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.44f))
                    .focusProperties { canFocus = false }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack,
                    )
            )

            val isCompact = maxWidth < 640.dp || maxHeight < 500.dp
            val bottomClearance = if (maxHeight < 560.dp) 132.dp else 156.dp
            val availableAboveDock = (maxHeight - bottomClearance - 20.dp).coerceAtLeast(240.dp)
            val popupSide = if (isCompact) {
                minOf(maxWidth * 0.92f, availableAboveDock)
            } else {
                minOf(maxWidth * 0.54f, availableAboveDock).coerceIn(320.dp, 470.dp)
            }
            val popupWidth = if (isCompact) (maxWidth * 0.92f).coerceAtMost(440.dp) else (popupSide + 50.dp).coerceAtMost(maxWidth * 0.68f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = surfaceHex.copy(alpha = 0.94f),
                    border = BorderStroke(2.dp, accent.copy(alpha = 0.85f)),
                    shadowElevation = 24.dp,
                    modifier = Modifier
                        .padding(bottom = bottomClearance, top = 12.dp, start = 16.dp, end = 16.dp)
                        .size(width = popupWidth, height = popupSide)
                        .focusProperties { canFocus = false }
                        .pointerInput(Unit) { detectTapGestures { /* keep popup open on interior tap */ } }
                        .testTag("app_library_popup_window"),
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    surfaceHex.copy(alpha = 0.96f),
                                    Color(0xF5040E27),
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
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
                            Surface(
                                shape = CircleShape,
                                color = accent.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
                                modifier = Modifier.size(34.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Rounded.Apps,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            Text(
                                text = strings.menuAppLibrary,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Surface(
                                shape = CircleShape,
                                color = accent.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                            ) {
                                Text(
                                    text = "${state.visibleAppsOrdered.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                        TvFocusableSurface(
                            onClick = onBack,
                            accentColor = accent,
                            shape = CircleShape,
                            baseColor = Color.White.copy(alpha = 0.10f),
                            focusRequester = closeFocusRequester,
                            modifier = Modifier.size(40.dp).testTag("subscreen_back_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = strings.backLabel,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        accent.copy(alpha = 0.65f),
                                        accent.copy(alpha = 0.25f),
                                        Color.Transparent,
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = strings.appLibraryLongPressHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = accent.copy(alpha = 0.92f),
                        modifier = Modifier.fillMaxWidth().testTag("app_library_long_press_hint"),
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val apps = state.visibleAppsOrdered
                    if (apps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Aucune application trouvée", color = MutedSlate)
                        }
                    } else {
                        val cols = if (isCompact) 3 else 4
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(cols),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 6.dp, bottom = 14.dp, start = 4.dp, end = 4.dp),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        ) {
                            items(apps, key = { it.packageName }) { app ->
                                AppLibraryTileCard(
                                    app = app,
                                    settings = state.settings,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onOpenContextApp(app) },
                                    isFavoriteBadge = favPackages.contains(app.packageName),
                                    isCompact = isCompact,
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

@Composable
fun SearchAppsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
) {
    val strings = LauncherStrings.get(state.settings.language)
    val accent = state.settings.accentColor
    var query by remember { mutableStateOf("") }

    SubScreenScaffold(
        title = strings.menuSearchApps,
        accent = accent,
        backLabel = strings.backLabel,
        onBack = onBack,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.menuSearchApps, color = MutedSlate) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = accent) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedContainerColor = GlassBlue.copy(alpha = 0.50f),
                    unfocusedContainerColor = GlassBlue.copy(alpha = 0.40f),
                ),
                modifier = Modifier.fillMaxWidth().testTag("search_apps_input"),
            )
            Spacer(modifier = Modifier.height(14.dp))

            val results = remember(state.visibleAppsOrdered, query) {
                if (query.isBlank()) state.visibleAppsOrdered
                else state.visibleAppsOrdered.filter { it.label.contains(query, ignoreCase = true) }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(results, key = { it.packageName }) { app ->
                    GlassSettingCardRow(
                        title = app.label,
                        subtitle = app.packageName,
                        trailingText = "Ouvrir",
                        accentColor = accent,
                        onClick = { onLaunchApp(app) },
                        testTag = "search_result_${app.packageName}",
                    )
                }
            }
        }
    }
}

@Composable
fun EditFavoritesScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val strings = LauncherStrings.get(state.settings.language)
    val accent = state.settings.accentColor
    val favSet = state.favoriteApps.map { it.packageName }.toSet()

    SubScreenScaffold(
        title = strings.menuEditFavorites,
        accent = accent,
        backLabel = strings.backLabel,
        onBack = onBack,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text(
                    text = "${strings.favoritesDockTitle}: ${state.favoriteApps.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            items(state.visibleAppsOrdered, key = { it.packageName }) { app ->
                val isFav = favSet.contains(app.packageName)
                TvFocusableSurface(
                    onClick = { onToggleFavorite(app.packageName) },
                    accentColor = accent,
                    shape = RoundedCornerShape(16.dp),
                    baseColor = GlassBlue.copy(alpha = 0.50f),
                    isSelected = isFav,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_fav_${app.packageName}"),
                ) { isFocused ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = app.label, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFocused) PureWhite.copy(alpha = 0.9f) else MutedSlate,
                            )
                        }
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = if (isFav || isFocused) PureWhite else MutedSlate,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReorderAppsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onMoveApp: (String, Int) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
) {
    val strings = LauncherStrings.get(state.settings.language)
    val accent = state.settings.accentColor

    SubScreenScaffold(
        title = strings.menuReorderApps,
        accent = accent,
        backLabel = strings.backLabel,
        onBack = onBack,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text(
                    text = "Favoris (Barre principale)",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            items(state.favoriteApps, key = { "fav_${it.packageName}" }) { app ->
                ReorderRow(
                    app = app,
                    accent = accent,
                    onMoveUp = { onMoveFavorite(app.packageName, -1) },
                    onMoveDown = { onMoveFavorite(app.packageName, 1) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toutes les applications",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            items(state.visibleAppsOrdered, key = { "all_${it.packageName}" }) { app ->
                ReorderRow(
                    app = app,
                    accent = accent,
                    onMoveUp = { onMoveApp(app.packageName, -1) },
                    onMoveDown = { onMoveApp(app.packageName, 1) },
                )
            }
        }
    }
}

@Composable
private fun ReorderRow(
    app: AppItem,
    accent: Color,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassBlue.copy(alpha = 0.52f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
                modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TvFocusableSurface(
                    onClick = onMoveUp,
                    accentColor = accent,
                    shape = RoundedCornerShape(10.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = "Monter", tint = PureWhite)
                    }
                }
                TvFocusableSurface(
                    onClick = onMoveDown,
                    accentColor = accent,
                    shape = RoundedCornerShape(10.dp),
                    baseColor = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = "Descendre", tint = PureWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun HiddenAppsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onToggleHidden: (String) -> Unit,
) {
    val strings = LauncherStrings.get(state.settings.language)
    val accent = state.settings.accentColor

    SubScreenScaffold(
        title = strings.menuHiddenApps,
        accent = accent,
        backLabel = strings.backLabel,
        onBack = onBack,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (state.hiddenApps.isNotEmpty()) {
                item {
                    Text(
                        text = "Applications actuellement masquées (appuyer pour réafficher)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accent,
                    )
                }
                items(state.hiddenApps, key = { "hid_${it.packageName}" }) { app ->
                    GlassSettingCardRow(
                        title = app.label,
                        subtitle = app.packageName,
                        trailingText = "Réafficher",
                        leadingIcon = Icons.Rounded.Visibility,
                        accentColor = accent,
                        isHighlighted = true,
                        onClick = { onToggleHidden(app.packageName) },
                        testTag = "unhide_${app.packageName}",
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Masquer une application",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                )
            }
            items(state.visibleAppsOrdered, key = { "vis_${it.packageName}" }) { app ->
                GlassSettingCardRow(
                    title = app.label,
                    subtitle = app.packageName,
                    trailingText = "Masquer",
                    leadingIcon = Icons.Rounded.VisibilityOff,
                    accentColor = accent,
                    onClick = { onToggleHidden(app.packageName) },
                    testTag = "hide_${app.packageName}",
                )
            }
        }
    }
}

@Composable
fun AboutSupportScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit = {},
    onRequestDefaultHome: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor
    var showEditDialog by remember { mutableStateOf(false) }
    var isDefaultHome by remember {
        mutableStateOf(InstalledAppsRepository.isDefaultHomeLauncher(context))
    }
    val waDigits = remember(settings.developerWhatsApp) {
        settings.developerWhatsApp.filter { it.isDigit() }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultHome = InstalledAppsRepository.isDefaultHomeLauncher(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SubScreenScaffold(
        title = strings.menuAboutSupport,
        accent = accent,
        backLabel = strings.backLabel,
        onBack = onBack,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text(
                    text = strings.aboutDeveloperSectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                    modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
                )
            }
            item {
                GlassSettingCardRow(
                    title = settings.developerName,
                    subtitle = strings.aboutDeveloperNameLabel,
                    leadingIcon = Icons.Rounded.Person,
                    accentColor = accent,
                    onClick = { showEditDialog = true },
                    testTag = "about_developer_name_card",
                )
            }
            item {
                GlassSettingCardRow(
                    title = strings.aboutContactEmailLabel,
                    subtitle = settings.developerEmail,
                    trailingText = strings.aboutOpenAction,
                    leadingIcon = Icons.Rounded.Email,
                    accentColor = accent,
                    isHighlighted = true,
                    onClick = {
                        val email = settings.developerEmail.trim()
                        if (email.isBlank()) {
                            showEditDialog = true
                        } else {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                                putExtra(Intent.EXTRA_SUBJECT, "Horizon Launcher Support")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val launched = runCatching {
                                context.startActivity(intent)
                                true
                            }.getOrDefault(false)
                            if (!launched) {
                                Toast.makeText(context, strings.aboutEmailLaunchError, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    testTag = "about_contact_email_btn",
                )
            }
            item {
                GlassSettingCardRow(
                    title = strings.aboutContactWhatsappLabel,
                    subtitle = settings.developerWhatsApp.takeIf { it.isNotBlank() } ?: strings.aboutWhatsappNotSetHint,
                    trailingText = if (waDigits.isNotEmpty()) strings.aboutOpenAction else null,
                    leadingIcon = Icons.Rounded.Phone,
                    accentColor = EmeraldGlow,
                    isHighlighted = waDigits.isNotEmpty(),
                    onClick = {
                        if (waDigits.isEmpty()) {
                            showEditDialog = true
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waDigits")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val launched = runCatching {
                                context.startActivity(intent)
                                true
                            }.getOrDefault(false)
                            if (!launched) {
                                Toast.makeText(context, strings.aboutWhatsappLaunchError, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    testTag = "about_contact_whatsapp_btn",
                )
            }
            item {
                GlassSettingCardRow(
                    title = strings.aboutEditDeveloperInfo,
                    leadingIcon = Icons.Rounded.Edit,
                    accentColor = accent,
                    onClick = { showEditDialog = true },
                    testTag = "about_edit_developer_btn",
                )
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.aboutSystemSectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
            item {
                GlassSettingCardRow(
                    title = strings.aboutSetDefaultHomeTitle,
                    subtitle = strings.aboutSetDefaultHomeDesc,
                    trailingText = if (isDefaultHome) strings.aboutDefaultHomeActive else strings.aboutSetDefaultHomeAction,
                    leadingIcon = Icons.Rounded.Home,
                    accentColor = if (isDefaultHome) EmeraldGlow else accent,
                    isHighlighted = !isDefaultHome,
                    onClick = onRequestDefaultHome,
                    testTag = "about_set_default_home_btn",
                )
            }
            item {
                GlassSettingCardRow(
                    title = "Horizon Launcher",
                    subtitle = "Version ${com.example.BuildConfig.VERSION_NAME} · Smart TV & Android Adaptive Launcher",
                    leadingIcon = Icons.Rounded.Info,
                    trailingText = "v${com.example.BuildConfig.VERSION_NAME}",
                    accentColor = accent,
                    onClick = {},
                    testTag = "about_version_card",
                )
            }
            item {
                GlassSettingCardRow(
                    title = "Applications détectées",
                    subtitle = "${state.allApps.size} applications installées · ${state.favoriteApps.size} favoris · ${state.hiddenApps.size} masquées",
                    leadingIcon = Icons.Rounded.CheckCircle,
                    accentColor = EmeraldGlow,
                    onClick = {},
                    testTag = "about_apps_diag",
                )
            }
            item {
                GlassSettingCardRow(
                    title = "Localisation GPS & Prières",
                    subtitle = "${settings.cityName} (${settings.timezoneId})",
                    trailingText = if (state.hasLocationPermission) "GPS OK" else "Manuel",
                    accentColor = accent,
                    onClick = {},
                    testTag = "about_gps_diag",
                )
            }
            item {
                GlassSettingCardRow(
                    title = "Appareil & Système",
                    subtitle = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                    accentColor = accent,
                    onClick = {},
                    testTag = "about_device_diag",
                )
            }
        }
    }

    if (showEditDialog) {
        var nameInput by remember(settings.developerName) { mutableStateOf(settings.developerName) }
        var emailInput by remember(settings.developerEmail) { mutableStateOf(settings.developerEmail) }
        var whatsappInput by remember(settings.developerWhatsApp) { mutableStateOf(settings.developerWhatsApp) }

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
                onDismissRequest = { showEditDialog = false },
                containerColor = Color(0xFF0C1E44),
                titleContentColor = PureWhite,
                modifier = Modifier.testTag("developer_edit_dialog"),
                title = { Text(strings.aboutEditDeveloperInfo) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(strings.aboutDeveloperNameLabel) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("developer_name_input"),
                        )
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(strings.aboutContactEmailLabel) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("developer_email_input"),
                        )
                        OutlinedTextField(
                            value = whatsappInput,
                            onValueChange = { whatsappInput = it },
                            label = { Text(strings.aboutContactWhatsappLabel) },
                            placeholder = { Text(strings.aboutDeveloperWhatsappPlaceholder, color = MutedSlate) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("developer_whatsapp_input"),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val cleanName = nameInput.trim().ifBlank { LauncherSettings().developerName }
                            val cleanEmail = emailInput.trim().ifBlank { LauncherSettings().developerEmail }
                            val cleanWa = whatsappInput.trim()
                            onUpdateSettings {
                                it.copy(
                                    developerName = cleanName,
                                    developerEmail = cleanEmail,
                                    developerWhatsApp = cleanWa,
                                )
                            }
                            showEditDialog = false
                        },
                        modifier = Modifier.testTag("developer_save_btn"),
                    ) {
                        Text(strings.saveAndClose, color = accent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditDialog = false },
                        modifier = Modifier.testTag("developer_cancel_btn"),
                    ) {
                        Text(strings.aboutCancelLabel, color = MutedSlate)
                    }
                },
            )
        }
    }
}
