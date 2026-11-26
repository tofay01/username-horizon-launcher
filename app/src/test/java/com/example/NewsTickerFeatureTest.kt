package com.example

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.NewsTickerRepository
import com.example.model.AppItem
import com.example.model.LauncherDestination
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.NewsTickerFontType
import com.example.ui.LauncherUiState
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LauncherSettingsScreen
import com.example.ui.theme.HorizonLauncherTheme
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h720dp-land")
class NewsTickerFeatureTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun repository_parsesRssAndAtom_cleansHtml_andInterleavesCategories() {
        val rssXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Test Politics</title>
                <item>
                  <title><![CDATA[Summit opens in <b>Geneva</b> &amp; Brussels]]></title>
                  <description><![CDATA[<p>Leaders meet for &laquo;diplomatic&raquo; talks.</p>]]></description>
                  <link>https://example.com/politics-1</link>
                  <pubDate>Tue, 25 Nov 2026 10:00:00 GMT</pubDate>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val atomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <entry>
                <title>Championship final kicks off tonight &#8212; Live</title>
                <summary>Two unbeaten clubs face off.</summary>
                <link rel="alternate" href="https://example.com/sports-1"/>
                <updated>2026-11-25T10:15:00Z</updated>
              </entry>
            </feed>
        """.trimIndent()

        val politics = NewsTickerRepository.parseRssOrAtomXml(
            xml = rssXml,
            category = NewsCategory.POLITICS,
            sourceName = "BBC",
        )
        val sports = NewsTickerRepository.parseRssOrAtomXml(
            xml = atomXml,
            category = NewsCategory.SPORTS,
            sourceName = "ESPN",
        )
        val world = listOf(
            NewsArticle(
                id = "w1",
                title = "Global climate accord reaches milestone",
                link = "https://example.com/world-1",
                sourceName = "France 24",
                category = NewsCategory.WORLD,
            ),
            NewsArticle(
                id = "w_dup",
                title = "Summit opens in Geneva & Brussels",
                link = "https://example.com/dup",
                sourceName = "France 24",
                category = NewsCategory.WORLD,
            ),
        )

        assertEquals(1, politics.size)
        assertEquals("Summit opens in Geneva & Brussels", politics[0].title)
        assertEquals("Leaders meet for «diplomatic» talks.", politics[0].summary)
        assertEquals("https://example.com/politics-1", politics[0].link)

        assertEquals(1, sports.size)
        assertEquals("Championship final kicks off tonight — Live", sports[0].title)
        assertEquals("https://example.com/sports-1", sports[0].link)

        val interleaved = NewsTickerRepository.interleaveAndDeduplicate(
            mapOf(
                NewsCategory.POLITICS to politics,
                NewsCategory.SPORTS to sports,
                NewsCategory.WORLD to world,
            )
        )
        assertEquals(3, interleaved.size)
        assertEquals(NewsCategory.POLITICS, interleaved[0].category)
        assertEquals(NewsCategory.SPORTS, interleaved[1].category)
        assertEquals(NewsCategory.WORLD, interleaved[2].category)
    }

    @Test
    fun homeScreen_newsTickerSitsBelowFavoritesDockWithoutAlteringDockSize_andOpensReader() {
        val sampleApps = listOf(
            AppItem("com.test.yt", "YouTube", "MainActivity", ColorDrawable(AndroidColor.RED)),
            AppItem("com.test.nf", "Netflix", "MainActivity", ColorDrawable(AndroidColor.BLACK)),
        )
        val sampleNews = listOf(
            NewsArticle(
                id = "pol_1",
                title = "New regional economic treaty signed today",
                summary = "Delegates ratified trade provisions after two days of meetings.",
                link = "https://example.com/pol1",
                sourceName = "France 24",
                category = NewsCategory.POLITICS,
            ),
            NewsArticle(
                id = "spt_1",
                title = "Champions League quarter-final draw announced",
                summary = "Exciting clashes headline the spring knockout round.",
                link = "https://example.com/spt1",
                sourceName = "BBC Sport",
                category = NewsCategory.SPORTS,
            ),
            NewsArticle(
                id = "wld_1",
                title = "Major deep-sea research expedition returns",
                link = "https://example.com/wld1",
                sourceName = "BBC World",
                category = NewsCategory.WORLD,
            ),
        )
        var navigatedDest: LauncherDestination? = null
        var openedArticleUrl: String? = null

        val stateHolder = mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(
                    language = LauncherLanguage.EN,
                    favoritePackages = sampleApps.map { it.packageName },
                    showNewsTicker = true,
                ),
                allApps = sampleApps,
                favoriteApps = sampleApps,
                visibleAppsOrdered = sampleApps,
                newsArticles = sampleNews,
            )
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = stateHolder.value,
                    isHomeActive = true,
                    onNavigate = { navigatedDest = it },
                    onLaunchApp = {},
                    onOpenContextApp = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onHideApp = {},
                    onOpenAppInfo = {},
                    onOpenNetworkSettings = {},
                    onExecuteGesture = {},
                    onSelectHdmiSource = {},
                    onStopAdhan = {},
                    onOpenNewsArticle = { openedArticleUrl = it },
                )
            }
        }

        composeRule.waitForIdle()
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val dockBoundsWithTicker = composeRule.onNodeWithTag("home_favorites_dock")
            .assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot
        val tickerBounds = composeRule.onNodeWithTag("home_news_ticker_bar")
            .assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot

        assertTrue(
            "Expected news ticker (${tickerBounds.top}) strictly below favorites dock (${dockBoundsWithTicker.bottom})",
            tickerBounds.top >= dockBoundsWithTicker.bottom - 1f,
        )
        assertTrue(
            "Expected news ticker (${tickerBounds.bottom}) to fit cleanly inside viewport (${rootBounds.bottom})",
            tickerBounds.bottom <= rootBounds.bottom + 2f,
        )

        // Click ticker -> opens reader dialog -> filter by Sports -> inspect article -> customize style
        composeRule.onNodeWithTag("home_news_ticker_bar").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("news_reader_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("news_tab_sports").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("news_article_item_spt_1").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("news_article_detail_view").assertIsDisplayed()
        composeRule.onNodeWithTag("news_open_article_btn").performClick()
        composeRule.waitForIdle()
        assertEquals("https://example.com/spt1", openedArticleUrl)

        composeRule.onNodeWithTag("news_customize_style_btn").performClick()
        composeRule.waitForIdle()
        assertEquals(LauncherDestination.LAUNCHER_SETTINGS, navigatedDest)

        // Turning ticker off keeps Favorites dock geometry completely intact
        stateHolder.value = stateHolder.value.copy(
            settings = stateHolder.value.settings.copy(showNewsTicker = false)
        )
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithTag("home_news_ticker_bar").fetchSemanticsNodes().isEmpty())
        val dockBoundsWithoutTicker = composeRule.onNodeWithTag("home_favorites_dock")
            .assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected unchanged Favorites dock dimensions: with=$dockBoundsWithTicker vs without=$dockBoundsWithoutTicker",
            abs(dockBoundsWithTicker.width - dockBoundsWithoutTicker.width) < 1f &&
                abs(dockBoundsWithTicker.height - dockBoundsWithoutTicker.height) < 1f,
        )
    }

    @Test
    @Config(sdk = [34], qualifiers = "w960dp-h540dp-land")
    fun homeScreen_fitsFavoritesDockAndNewsTickerCleanlyOn540dpTvViewport() {
        val sampleApps = (1..7).map { i ->
            AppItem("com.test.app$i", "App $i", "MainActivity", ColorDrawable(AndroidColor.BLUE))
        }
        val state = LauncherUiState(
            settings = LauncherSettings(
                appsPerRow = 7,
                rowsBelowFavorites = 0,
                favoritePackages = sampleApps.map { it.packageName },
                showNewsTicker = true,
            ),
            allApps = sampleApps,
            favoriteApps = sampleApps,
            visibleAppsOrdered = sampleApps,
            newsArticles = listOf(
                NewsArticle(
                    id = "w1",
                    title = "Live headline inside compact TV viewport",
                    sourceName = "BBC",
                    category = NewsCategory.WORLD,
                )
            ),
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = state,
                    isHomeActive = true,
                    onNavigate = {},
                    onLaunchApp = {},
                    onOpenContextApp = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onHideApp = {},
                    onOpenAppInfo = {},
                    onOpenNetworkSettings = {},
                    onExecuteGesture = {},
                    onSelectHdmiSource = {},
                    onStopAdhan = {},
                )
            }
        }

        composeRule.waitForIdle()
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val dockBounds = composeRule.onNodeWithTag("home_favorites_dock").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val tickerBounds = composeRule.onNodeWithTag("home_news_ticker_bar").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

        assertTrue(
            "Expected dock (${dockBounds.bottom}) above ticker (${tickerBounds.top})",
            tickerBounds.top >= dockBounds.bottom - 1f,
        )
        assertTrue(
            "Expected ticker (${tickerBounds.bottom}) to fit inside 540dp viewport (${rootBounds.bottom})",
            tickerBounds.bottom <= rootBounds.bottom + 2f,
        )
    }

    @Test
    fun launcherSettings_updatesNewsTickerFontSizeColorBackgroundAndCategories() {
        val stateHolder = mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(language = LauncherLanguage.EN)
            )
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                LauncherSettingsScreen(
                    state = stateHolder.value,
                    onBack = {},
                    onNavigate = {},
                    onUpdateSettings = { transform ->
                        stateHolder.value = stateHolder.value.copy(
                            settings = transform(stateHolder.value.settings)
                        )
                    },
                    onFollowThemeReset = {},
                    onRequestDefaultHome = {},
                    onCheckUpdates = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("news_ticker_preview").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("news_cat_toggle_sports").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertFalse(stateHolder.value.settings.enabledNewsCategories.contains(NewsCategory.SPORTS))

        composeRule.onNodeWithTag("news_font_type_arabic").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(NewsTickerFontType.ARABIC, stateHolder.value.settings.newsTickerFontType)

        val beforeSize = stateHolder.value.settings.newsTickerFontSizeSp
        composeRule.onNodeWithTag("news_font_size_increase").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(beforeSize + 1, stateHolder.value.settings.newsTickerFontSizeSp)

        composeRule.onNodeWithTag("news_text_color_FF38BDF8").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(0xFF38BDF8L, stateHolder.value.settings.newsTickerTextColorHex)

        composeRule.onNodeWithTag("news_bg_color_FF090D16").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(0xFF090D16L, stateHolder.value.settings.newsTickerBgColorHex)

        composeRule.onNodeWithTag("news_bg_opacity_decrease").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(0.76f, stateHolder.value.settings.newsTickerBgAlpha, 0.02f)

        composeRule.onNodeWithTag("toggle_show_news_ticker").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertFalse(stateHolder.value.settings.showNewsTicker)
    }
}
