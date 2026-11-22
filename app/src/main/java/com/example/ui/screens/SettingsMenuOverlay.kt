package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import com.example.ui.icons.Apps
import com.example.ui.icons.DragIndicator
import com.example.ui.icons.Tune
import com.example.ui.icons.Tv
import com.example.ui.icons.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherDestination
import com.example.model.LauncherLanguage
import com.example.model.LauncherStrings
import com.example.ui.LauncherUiState
import com.example.ui.components.CurvedSettingsDrawerContainer
import com.example.ui.components.LocalTvFocusEnabled
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite

@Composable
fun SettingsMenuOverlay(
    state: LauncherUiState,
    onDismiss: () -> Unit,
    onNavigate: (LauncherDestination) -> Unit,
    onOpenSystemSettings: () -> Unit,
    onChangeLanguage: (LauncherLanguage) -> Unit,
) {
    BackHandler { onDismiss() }

    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor
    val surfaceColor = Color(settings.themePreset.surfaceHex)
    val firstItemFocusRequester = remember { FocusRequester() }
    val appLayoutDirection = LocalLayoutDirection.current

    LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        runCatching { firstItemFocusRequester.requestFocus() }
    }

    CompositionLocalProvider(LocalTvFocusEnabled provides true) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.44f))
                .focusProperties { canFocus = false }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 10.dp, bottom = 14.dp),
            contentAlignment = AbsoluteAlignment.TopRight,
        ) {
            val isCompact = maxWidth < 640.dp
            val drawerWidth = if (isCompact) (maxWidth * 0.88f).coerceAtMost(360.dp) else 376.dp
            val headerHeight = if (isCompact) 58.dp else 66.dp
            val neckInset = if (isCompact) 34.dp else 48.dp
            val midBowInset = if (isCompact) 54.dp else 82.dp
            val bottomInset = if (isCompact) 18.dp else 26.dp
            val headerLeftPad = neckInset + 10.dp
            val bodyLeftPad = midBowInset + 8.dp

            CurvedSettingsDrawerContainer(
                headerHeight = headerHeight,
                neckInset = neckInset,
                midBowInset = midBowInset,
                bottomInset = bottomInset,
                surfaceColor = surfaceColor,
                accentColor = accent,
                modifier = Modifier
                    .width(drawerWidth)
                    .focusProperties { canFocus = false }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .testTag("settings_menu_panel"),
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Flared Header Row (above the bright glowing horizontal neon divider)
                        CompositionLocalProvider(LocalLayoutDirection provides appLayoutDirection) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(headerHeight)
                                    .padding(
                                        start = if (appLayoutDirection == LayoutDirection.Rtl) 14.dp else headerLeftPad,
                                        end = if (appLayoutDirection == LayoutDirection.Rtl) headerLeftPad else 14.dp,
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = accent.copy(alpha = 0.24f),
                                        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.85f)),
                                        modifier = Modifier.size(34.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(
                                                imageVector = Icons.Rounded.Settings,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = strings.settingsButton,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TvFocusableSurface(
                                    onClick = onDismiss,
                                    accentColor = accent,
                                    shape = CircleShape,
                                    baseColor = Color.White.copy(alpha = 0.08f),
                                    baseBorderColor = Color.White.copy(alpha = 0.18f),
                                    modifier = Modifier.size(40.dp).testTag("settings_menu_close"),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = strings.backLabel,
                                            tint = PureWhite.copy(alpha = 0.9f),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Curved Drawer Body (physical left inset ensures items never touch concave neon curve)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(start = bodyLeftPad, end = 16.dp, top = 8.dp, bottom = 14.dp),
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides appLayoutDirection) {
                                MenuOptionRow(
                                    label = strings.menuTvSettings,
                                    icon = Icons.Rounded.Tv,
                                    accent = accent,
                                    highlighted = true,
                                    focusRequester = firstItemFocusRequester,
                                    testTag = "menu_tv_settings",
                                    onClick = {
                                        onDismiss()
                                        onOpenSystemSettings()
                                    },
                                )
                                DrawerHairlineDivider(accent)
                                MenuOptionRow(
                                    label = strings.menuLauncherSettings,
                                    icon = Icons.Rounded.Tune,
                                    accent = accent,
                                    testTag = "menu_launcher_settings",
                                    onClick = { onNavigate(LauncherDestination.LAUNCHER_SETTINGS) },
                                )
                                DrawerHairlineDivider(accent)
                                MenuOptionRow(
                                    label = strings.menuAppLibrary,
                                    icon = Icons.Rounded.Apps,
                                    accent = accent,
                                    testTag = "menu_app_library",
                                    onClick = { onNavigate(LauncherDestination.APP_LIBRARY) },
                                )
                                DrawerHairlineDivider(accent)
                                MenuOptionRow(
                                    label = strings.menuEditFavorites,
                                    icon = Icons.Rounded.Star,
                                    accent = accent,
                                    testTag = "menu_edit_favorites",
                                    onClick = { onNavigate(LauncherDestination.EDIT_FAVORITES) },
                                )
                                DrawerHairlineDivider(accent)
                                MenuOptionRow(
                                    label = strings.menuReorderApps,
                                    icon = Icons.Rounded.DragIndicator,
                                    accent = accent,
                                    testTag = "menu_reorder_apps",
                                    onClick = { onNavigate(LauncherDestination.REORDER_APPS) },
                                )
                                DrawerHairlineDivider(accent)
                                MenuOptionRow(
                                    label = strings.menuSearchApps,
                                    icon = Icons.Rounded.Search,
                                    accent = accent,
                                    testTag = "menu_search_apps",
                                    onClick = { onNavigate(LauncherDestination.SEARCH_APPS) },
                                )
                                DrawerHairlineDivider(accent)
                                MenuOptionRow(
                                    label = strings.menuHiddenApps,
                                    icon = Icons.Rounded.VisibilityOff,
                                    accent = accent,
                                    testTag = "menu_hidden_apps",
                                    onClick = { onNavigate(LauncherDestination.HIDDEN_APPS) },
                                )
                                DrawerHairlineDivider(accent)
                                MenuOptionRow(
                                    label = strings.menuAboutSupport,
                                    icon = Icons.Rounded.Info,
                                    accent = accent,
                                    testTag = "menu_about_support",
                                    onClick = { onNavigate(LauncherDestination.ABOUT_SUPPORT) },
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    LauncherLanguage.entries.forEach { lang ->
                                        val selected = lang == settings.language
                                        TvFocusableSurface(
                                            onClick = { onChangeLanguage(lang) },
                                            accentColor = accent,
                                            shape = RoundedCornerShape(10.dp),
                                            baseColor = Color.White.copy(alpha = 0.08f),
                                            baseBorderColor = Color.White.copy(alpha = 0.18f),
                                            isSelected = selected,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .testTag("lang_switch_${lang.code}"),
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(
                                                    text = lang.circleCode,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = PureWhite,
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
    }
}

@Composable
private fun DrawerHairlineDivider(accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(1.dp)
            .background(accent.copy(alpha = 0.24f))
    )
}

@Composable
private fun MenuOptionRow(
    label: String,
    icon: ImageVector,
    accent: Color,
    testTag: String,
    highlighted: Boolean = false,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    TvFocusableSurface(
        onClick = onClick,
        accentColor = accent,
        shape = RoundedCornerShape(12.dp),
        baseColor = Color.Transparent,
        focusedColor = accent.copy(alpha = 0.38f),
        baseBorderColor = Color.Transparent,
        isSelected = highlighted,
        focusRequester = focusRequester,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag(testTag),
    ) { isFocused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused || highlighted) PureWhite else PureWhite.copy(alpha = 0.88f),
                modifier = Modifier.size(19.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (isFocused) PureWhite else PureWhite.copy(alpha = 0.94f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
