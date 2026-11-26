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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LocalizedStrings
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.ui.LauncherUiState
import com.example.ui.components.LocalTvFocusEnabled
import com.example.ui.components.TvFocusableSurface
import com.example.ui.components.newsCategoryBadgeBg
import com.example.ui.components.newsCategoryLabel
import com.example.ui.components.resolveNewsTickerFontFamily
import com.example.ui.icons.Newspaper
import com.example.ui.icons.OpenInNew
import com.example.ui.icons.Palette
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite

@Composable
fun NewsReaderDialog(
    state: LauncherUiState,
    strings: LocalizedStrings,
    accent: Color,
    onRefresh: () -> Unit,
    onOpenArticleLink: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val settings = state.settings
    var selectedCategory by rememberSaveable { mutableStateOf<NewsCategory?>(null) }
    var selectedArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    val primaryFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        runCatching { primaryFocus.requestFocus() }
    }

    val filteredArticles = remember(state.newsArticles, selectedCategory) {
        if (selectedCategory == null) {
            state.newsArticles
        } else {
            state.newsArticles.filter { it.category == selectedCategory }
        }
    }
    val selectedArticle = remember(state.newsArticles, selectedArticleId) {
        selectedArticleId?.let { id -> state.newsArticles.firstOrNull { it.id == id } }
    }
    val tickerFont = resolveNewsTickerFontFamily(settings.newsTickerFontType)

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
                    .testTag("news_reader_dialog"),
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
                                    .background(Color(0xFFDC2626).copy(alpha = 0.85f)),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Newspaper,
                                    contentDescription = strings.newsDialogTitle,
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = strings.newsDialogTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = strings.newsTickerBadgeLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
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
                                onClick = onOpenSettings,
                                accentColor = accent,
                                shape = RoundedCornerShape(14.dp),
                                baseColor = accent.copy(alpha = 0.22f),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("news_customize_style_btn"),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Palette,
                                        contentDescription = strings.newsSettingsAction,
                                        tint = PureWhite,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = strings.newsSettingsAction,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = PureWhite,
                                        maxLines = 1,
                                    )
                                }
                            }
                            TvFocusableSurface(
                                onClick = onRefresh,
                                accentColor = accent,
                                shape = CircleShape,
                                baseColor = Color.White.copy(alpha = 0.09f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("news_refresh_btn"),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = strings.newsRefreshAction,
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
                                    .testTag("news_close_btn"),
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

                    // Category filter tabs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        NewsCategoryFilterChip(
                            label = strings.newsCategoryAll,
                            selected = selectedCategory == null,
                            accent = accent,
                            focusRequester = primaryFocus,
                            testTag = "news_tab_all",
                            onClick = {
                                selectedArticleId = null
                                selectedCategory = null
                            },
                        )
                        NewsCategory.entries.forEach { cat ->
                            NewsCategoryFilterChip(
                                label = newsCategoryLabel(cat, strings),
                                selected = selectedCategory == cat,
                                accent = newsCategoryBadgeBg(cat),
                                testTag = "news_tab_${cat.id}",
                                onClick = {
                                    selectedArticleId = null
                                    selectedCategory = cat
                                },
                            )
                        }
                    }

                    // Article list or Article detail
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 210.dp, max = 360.dp),
                    ) {
                        when {
                            selectedArticle != null -> {
                                NewsArticleDetailView(
                                    article = selectedArticle,
                                    strings = strings,
                                    accent = accent,
                                    fontFamily = tickerFont,
                                    onBackToList = { selectedArticleId = null },
                                    onOpenLink = {
                                        if (selectedArticle.link.isNotBlank()) {
                                            onOpenArticleLink(selectedArticle.link)
                                        }
                                    },
                                )
                            }
                            settings.enabledNewsCategories.isEmpty() -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .testTag("news_empty_categories_state"),
                                ) {
                                    Text(
                                        text = strings.newsEmptyCategories,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSlate,
                                    )
                                }
                            }
                            state.isLoadingNews && filteredArticles.isEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .testTag("news_loading_indicator"),
                                ) {
                                    CircularProgressIndicator(color = accent, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = strings.newsLoadingLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSlate,
                                    )
                                }
                            }
                            !state.newsError.isNullOrBlank() && filteredArticles.isEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .testTag("news_error_state"),
                                ) {
                                    Text(
                                        text = strings.newsOfflineError,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFFCA5A5),
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TextButton(onClick = onRefresh) {
                                        Text(strings.newsRefreshAction, color = accent)
                                    }
                                }
                            }
                            filteredArticles.isEmpty() -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .testTag("news_empty_state"),
                                ) {
                                    Text(
                                        text = strings.newsOfflineError,
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
                                        .heightIn(max = 360.dp)
                                        .testTag("news_articles_list"),
                                ) {
                                    items(filteredArticles, key = { it.id }) { article ->
                                        NewsArticleCard(
                                            article = article,
                                            strings = strings,
                                            accent = accent,
                                            fontFamily = tickerFont,
                                            onClick = { selectedArticleId = article.id },
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
private fun NewsCategoryFilterChip(
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
private fun NewsArticleCard(
    article: NewsArticle,
    strings: LocalizedStrings,
    accent: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
) {
    val catColor = newsCategoryBadgeBg(article.category)
    TvFocusableSurface(
        onClick = onClick,
        accentColor = accent,
        shape = RoundedCornerShape(14.dp),
        baseColor = Color.White.copy(alpha = 0.07f),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .testTag("news_article_item_${article.id}"),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(catColor.copy(alpha = 0.88f))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = newsCategoryLabel(article.category, strings),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = PureWhite,
                        maxLines = 1,
                    )
                }
                val meta = listOfNotNull(
                    article.sourceName.takeIf { it.isNotBlank() },
                    article.publishedAt.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = PureWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (article.summary.isNotBlank()) {
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = fontFamily),
                    color = MutedSlate,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NewsArticleDetailView(
    article: NewsArticle,
    strings: LocalizedStrings,
    accent: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    onBackToList: () -> Unit,
    onOpenLink: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("news_article_detail_view"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(newsCategoryBadgeBg(article.category))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(
                    text = newsCategoryLabel(article.category, strings),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite,
                )
            }
            Text(
                text = listOfNotNull(
                    article.sourceName.takeIf { it.isNotBlank() },
                    article.publishedAt.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MutedSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = article.title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
            ),
            color = PureWhite,
        )

        if (article.summary.isNotBlank()) {
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = fontFamily,
                    lineHeight = 22.sp,
                ),
                color = PureWhite.copy(alpha = 0.88f),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TvFocusableSurface(
                onClick = onBackToList,
                accentColor = accent,
                shape = RoundedCornerShape(12.dp),
                baseColor = Color.White.copy(alpha = 0.10f),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("news_detail_back_btn"),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.backLabel,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = strings.backLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = PureWhite,
                    )
                }
            }

            if (article.link.startsWith("https://", ignoreCase = true) ||
                article.link.startsWith("http://", ignoreCase = true)
            ) {
                TvFocusableSurface(
                    onClick = onOpenLink,
                    accentColor = accent,
                    shape = RoundedCornerShape(12.dp),
                    baseColor = accent.copy(alpha = 0.82f),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("news_open_article_btn"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = strings.newsOpenArticleAction,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = strings.newsOpenArticleAction,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite,
                        )
                    }
                }
            }
        }
    }
}
