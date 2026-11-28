package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherSettings
import com.example.model.LocalizedStrings
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.NewsTickerFontType
import com.example.ui.icons.Newspaper
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite

fun resolveNewsTickerFontFamily(type: NewsTickerFontType): FontFamily = when (type) {
    NewsTickerFontType.DEFAULT -> PlusJakartaFontFamily
    NewsTickerFontType.MODERN -> OutfitFontFamily
    NewsTickerFontType.ARABIC -> ArabicFontFamily
    NewsTickerFontType.SERIF -> FontFamily.Serif
    NewsTickerFontType.MONO -> FontFamily.Monospace
}

fun newsCategoryColor(category: NewsCategory, isLightBackground: Boolean = false): Color = when (category) {
    NewsCategory.POLITICS -> if (isLightBackground) Color(0xFFB91C1C) else Color(0xFFF87171)
    NewsCategory.SPORTS -> if (isLightBackground) Color(0xFF047857) else Color(0xFF34D399)
    NewsCategory.WORLD -> if (isLightBackground) Color(0xFF0369A1) else Color(0xFF38BDF8)
}

fun newsCategoryBadgeBg(category: NewsCategory): Color = when (category) {
    NewsCategory.POLITICS -> Color(0xFFDC2626)
    NewsCategory.SPORTS -> Color(0xFF059669)
    NewsCategory.WORLD -> Color(0xFF0284C7)
}

fun newsCategoryLabel(category: NewsCategory, strings: LocalizedStrings): String = when (category) {
    NewsCategory.POLITICS -> strings.newsCategoryPolitics
    NewsCategory.SPORTS -> strings.newsCategorySports
    NewsCategory.WORLD -> strings.newsCategoryWorld
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvNewsTickerBar(
    articles: List<NewsArticle>,
    isLoading: Boolean,
    errorMessage: String?,
    settings: LauncherSettings,
    strings: LocalizedStrings,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompactHeight: Boolean = false,
    interactive: Boolean = true,
) {
    val bgBaseColor = Color(settings.newsTickerBgColorHex)
    val bgAlpha = settings.newsTickerBgAlpha.coerceIn(0.20f, 1.0f)
    val effectiveBg = bgBaseColor.copy(alpha = bgAlpha)
    val isLightBg = bgBaseColor.luminance() > 0.5f && bgAlpha >= 0.45f
    val borderCol = if (isLightBg) {
        Color(0xFF0F172A).copy(alpha = 0.28f)
    } else {
        Color.White.copy(alpha = 0.22f)
    }
    val textColor = Color(settings.newsTickerTextColorHex)
    val fontSizeSp = settings.newsTickerFontSizeSp.coerceIn(11, 20)
    val tickerFontFamily = resolveNewsTickerFontFamily(settings.newsTickerFontType)
    val barHeight = if (isCompactHeight) {
        (fontSizeSp + 17).coerceIn(29, 40).dp
    } else {
        (fontSizeSp + 21).coerceIn(32, 46).dp
    }

    val tickerAnnotated = remember(articles, strings, textColor, isLightBg) {
        buildAnnotatedString {
            articles.forEachIndexed { idx, article ->
                if (idx > 0) {
                    withStyle(SpanStyle(color = textColor.copy(alpha = 0.48f), fontWeight = FontWeight.Bold)) {
                        append("   ●   ")
                    }
                }
                withStyle(
                    SpanStyle(
                        color = newsCategoryColor(article.category, isLightBackground = isLightBg),
                        fontWeight = FontWeight.ExtraBold,
                    )
                ) {
                    append("[${newsCategoryLabel(article.category, strings)}] ")
                }
                withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.SemiBold)) {
                    append(article.title)
                }
                if (article.sourceName.isNotBlank()) {
                    withStyle(SpanStyle(color = textColor.copy(alpha = 0.68f), fontWeight = FontWeight.Normal)) {
                        append(" (${article.sourceName})")
                    }
                }
            }
        }
    }

    val innerContent: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDC2626))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Newspaper,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = strings.newsTickerBadgeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = PureWhite,
                        maxLines = 1,
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.weight(1f),
            ) {
                if (articles.isNotEmpty()) {
                    Text(
                        text = tickerAnnotated,
                        style = TextStyle(
                            fontFamily = tickerFontFamily,
                            fontSize = fontSizeSp.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                velocity = 44.dp,
                            ),
                    )
                } else {
                    val statusText = when {
                        settings.enabledNewsCategories.isEmpty() -> strings.newsEmptyCategories
                        isLoading -> strings.newsLoadingLabel
                        !errorMessage.isNullOrBlank() -> strings.newsOfflineError
                        else -> strings.newsLoadingLabel
                    }
                    Text(
                        text = statusText,
                        style = TextStyle(
                            fontFamily = tickerFontFamily,
                            fontSize = fontSizeSp.sp,
                            color = textColor.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    if (interactive) {
        val clickInteractionSource = remember { MutableInteractionSource() }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = effectiveBg,
            border = BorderStroke(1.dp, borderCol),
            modifier = modifier
                .height(barHeight)
                .focusProperties { canFocus = false }
                .clickable(
                    interactionSource = clickInteractionSource,
                    indication = null,
                    onClick = onClick,
                ),
        ) {
            innerContent()
        }
    } else {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = effectiveBg,
            border = BorderStroke(1.dp, borderCol),
            modifier = modifier.height(barHeight),
        ) {
            innerContent()
        }
    }
}
