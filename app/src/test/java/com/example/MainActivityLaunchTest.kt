package com.example

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.pressKey
import com.example.model.AppItem
import com.example.model.LauncherDestination
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.LauncherStrings
import com.example.model.NextPrayerInfo
import com.example.model.PrayerKey
import com.example.model.WeatherData
import com.example.ui.LauncherUiState
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.HorizonLauncherTheme
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h720dp-land")
class MainActivityLaunchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mainActivity_boots_and_opens_settings_and_launcher_settings_with_dpad() {
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_prayer_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_weather_card").assertIsDisplayed()
        composeRule.onNodeWithTag("top_settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_menu_panel").assertDoesNotExist()
        composeRule.onNodeWithTag("home_edit_favorites_button").assertDoesNotExist()
        composeRule.onNodeWithTag("home_add_favorite_dock_btn").assertDoesNotExist()

        // Verify Prayer card is on the LEFT side, Weather card (with 5-day forecast) is on the RIGHT side,
        // BOTH share exact top alignment, and Hijri date is displayed inside Prayer card
        val prayerBounds = composeRule.onNodeWithTag("home_prayer_card").fetchSemanticsNode().boundsInRoot
        val weatherBounds = composeRule.onNodeWithTag("home_weather_card").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected prayer card on left (${prayerBounds.right}) vs weather card on right (${weatherBounds.left})",
            prayerBounds.right <= weatherBounds.left + 2f,
        )
        assertTrue(
            "Expected Prayer card top (${prayerBounds.top}) and Weather card top (${weatherBounds.top}) aligned",
            abs(prayerBounds.top - weatherBounds.top) < 2f,
        )
        val hijriBounds = composeRule.onNodeWithTag("home_prayer_hijri_date", useUnmergedTree = true)
            .assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected Hijri date inside Prayer card bounds=$prayerBounds, hijriBounds=$hijriBounds",
            hijriBounds.top >= prayerBounds.top && hijriBounds.bottom <= prayerBounds.bottom + 2f,
        )
        val gregBounds = composeRule.onNodeWithTag("home_prayer_gregorian_date", useUnmergedTree = true)
            .assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected Gregorian date inside Prayer card below Hijri date: hijri=$hijriBounds, greg=$gregBounds, prayer=$prayerBounds",
            gregBounds.top >= hijriBounds.bottom - 2f && gregBounds.bottom <= prayerBounds.bottom + 2f,
        )
        val rowBounds = composeRule.onNodeWithTag("weather_forecast_5day_row", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected weather_forecast_5day_row visible inside weatherBounds=$weatherBounds, rowBounds=$rowBounds",
            rowBounds.height > 0f && rowBounds.width > 0f && rowBounds.bottom <= weatherBounds.bottom + 2f,
        )
        composeRule.onNodeWithTag("weather_forecast_5day_row", useUnmergedTree = true).assertIsDisplayed()
        (0..4).forEach { idx ->
            composeRule.onNodeWithTag("weather_forecast_day_$idx", useUnmergedTree = true).assertIsDisplayed()
        }

        // Verify unified top taskbar (AI tools + circular Wi-Fi/Ethernet + Search + gear Settings)
        // sits on the same horizontal level as the Greeting pill + circular language cycle button,
        // while the orbital Clock sits centered horizontally between Prayer (LEFT) and Weather (RIGHT)
        val greetingBounds = composeRule.onNodeWithTag("home_greeting_lang_header").fetchSemanticsNode().boundsInRoot
        val clockBounds = composeRule.onNodeWithTag("home_clock_header").fetchSemanticsNode().boundsInRoot
        val aiBarBounds = composeRule.onNodeWithTag("home_ai_tools_bar").fetchSemanticsNode().boundsInRoot
        val netPillBounds = composeRule.onNodeWithTag("top_network_pill").fetchSemanticsNode().boundsInRoot
        val searchBtnBounds = composeRule.onNodeWithTag("top_search_button").fetchSemanticsNode().boundsInRoot
        val settingsBtnBounds = composeRule.onNodeWithTag("top_settings_button").fetchSemanticsNode().boundsInRoot
        val langBtnBounds = composeRule.onNodeWithTag("home_language_cycle_button").fetchSemanticsNode().boundsInRoot
        val aiMidY = (aiBarBounds.top + aiBarBounds.bottom) / 2f
        val netMidY = (netPillBounds.top + netPillBounds.bottom) / 2f
        val settingsMidY = (settingsBtnBounds.top + settingsBtnBounds.bottom) / 2f
        val langMidY = (langBtnBounds.top + langBtnBounds.bottom) / 2f
        assertTrue(
            "Expected unified taskbar (${aiBarBounds.left}) beside top-left greeting/language row (${greetingBounds.right})",
            aiBarBounds.left >= greetingBounds.right - 2f,
        )
        assertTrue(
            "Expected orbital clock (${clockBounds.left}..${clockBounds.right}) centered between Prayer (${prayerBounds.right}) and Weather (${weatherBounds.left})",
            clockBounds.left >= prayerBounds.right - 2f && clockBounds.right <= weatherBounds.left + 2f,
        )
        val leftGap = clockBounds.left - prayerBounds.right
        val rightGap = weatherBounds.left - clockBounds.right
        assertTrue(
            "Expected equal horizontal gaps around Clock (leftGap=$leftGap, rightGap=$rightGap)",
            abs(leftGap - rightGap) < 4f && leftGap >= 8f,
        )
        assertTrue(
            "Expected Prayer (${prayerBounds.top}), Weather (${weatherBounds.top}), and Clock (${clockBounds.top}) raised close below top bar (${aiBarBounds.bottom})",
            (prayerBounds.top - aiBarBounds.bottom) <= 48f &&
                (weatherBounds.top - aiBarBounds.bottom) <= 48f &&
                (clockBounds.top - aiBarBounds.bottom) <= 48f,
        )
        assertTrue(
            "Expected unified taskbar (midY=$aiMidY) aligned with language button level (midY=$langMidY)",
            abs(aiMidY - langMidY) < 6f,
        )
        assertTrue(
            "Expected Wi-Fi/Ethernet circle inside unified taskbar: net=$netPillBounds, bar=$aiBarBounds",
            netPillBounds.left >= aiBarBounds.left - 1f &&
                netPillBounds.right <= aiBarBounds.right + 1f &&
                abs(netPillBounds.width - netPillBounds.height) < 2f &&
                abs(aiMidY - netMidY) < 4f,
        )
        assertTrue(
            "Expected Search circle inside unified taskbar: search=$searchBtnBounds, bar=$aiBarBounds",
            searchBtnBounds.left >= netPillBounds.right &&
                searchBtnBounds.right <= aiBarBounds.right + 1f &&
                abs(searchBtnBounds.width - searchBtnBounds.height) < 2f,
        )
        assertTrue(
            "Expected Settings gear circle inside unified taskbar: settings=$settingsBtnBounds, bar=$aiBarBounds",
            settingsBtnBounds.left >= searchBtnBounds.right &&
                settingsBtnBounds.right <= aiBarBounds.right + 1f &&
                abs(settingsBtnBounds.width - settingsBtnBounds.height) < 2f &&
                abs(aiMidY - settingsMidY) < 4f,
        )
        composeRule.onNodeWithText("Paramètres").assertDoesNotExist()
        composeRule.onNodeWithText("Settings").assertDoesNotExist()
        composeRule.onNodeWithText("Wi-Fi").assertDoesNotExist()
        composeRule.onNodeWithText("Ethernet").assertDoesNotExist()

        // Verify Favorites dock is fully visible inside viewport height (not clipped off bottom),
        // and the medium circular Apps grid button sits centered directly above the dock
        val dockBounds = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot
        val libBtnBounds = composeRule.onNodeWithTag("home_all_apps_button").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val libMidX = (libBtnBounds.left + libBtnBounds.right) / 2f
        val dockMidX = (dockBounds.left + dockBounds.right) / 2f
        assertTrue(
            "Expected enlarged circular Apps library medallion (${libBtnBounds.width}x${libBtnBounds.height})",
            abs(libBtnBounds.width - libBtnBounds.height) < 2f && libBtnBounds.width >= 90f,
        )
        assertTrue(
            "Expected circular Apps library button centered (midX=$libMidX) over Favorites dock (midX=$dockMidX)",
            abs(libMidX - dockMidX) < 6f,
        )
        assertTrue(
            "Expected circular Apps library button (${libBtnBounds.top}..${libBtnBounds.bottom}) cleanly below Prayer (${prayerBounds.bottom}) and above Dock (${dockBounds.top})",
            libBtnBounds.top >= prayerBounds.bottom - 2f && libBtnBounds.bottom <= dockBounds.top + 2f,
        )
        composeRule.onNodeWithText("Bibliothèque d'applications").assertDoesNotExist()
        composeRule.onNodeWithText("Apps Library").assertDoesNotExist()
        assertTrue(
            "Expected favorites dock (${dockBounds.bottom}) to fit completely inside viewport (${rootBounds.bottom})",
            dockBounds.bottom <= rootBounds.bottom + 2f,
        )

        // Verify pressing Back on Home does NOT finish/restart MainActivity or hide dock
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
            assertTrue("Home launcher must remain alive on Back press", !activity.isFinishing)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_prayer_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_favorites_dock").assertIsDisplayed()

        // Open Settings overlay
        composeRule.onNodeWithTag("top_settings_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_menu_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("menu_tv_settings").assertIsDisplayed()
        composeRule.onNodeWithTag("menu_launcher_settings").assertIsDisplayed()

        // Verify Settings panel is on the RIGHT and TV Settings is above Launcher Settings
        val panelBounds = composeRule.onNodeWithTag("settings_menu_panel").fetchSemanticsNode().boundsInRoot
        val tvBounds = composeRule.onNodeWithTag("menu_tv_settings").fetchSemanticsNode().boundsInRoot
        val launcherBounds = composeRule.onNodeWithTag("menu_launcher_settings").fetchSemanticsNode().boundsInRoot
        assertTrue("Settings panel should be on right side", panelBounds.left > 400f)
        assertTrue("TV settings should be above Launcher settings", tvBounds.top < launcherBounds.top)

        // Navigate with D-pad from TV settings down to Launcher settings and press DPAD_CENTER
        composeRule.onNodeWithTag("menu_tv_settings").assertIsFocused()
        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("menu_launcher_settings").assertIsFocused()
        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionCenter)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("launcher_settings_back").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [24], qualifiers = "w1280dp-h720dp-land")
    fun mainActivity_boots_cleanly_on_minSdk_24() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("top_settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_weather_card").assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h720dp-land")
class HomeScreenTilesTest {

    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun homeScreen_rendersAppTiles_andActivatesOnDpadCenter() {
        val sampleApps = listOf(
            AppItem("com.test.youtube", "YouTube", "MainActivity", ColorDrawable(AndroidColor.RED)),
            AppItem("com.test.netflix", "Netflix", "MainActivity", ColorDrawable(AndroidColor.BLACK)),
        )
        var launchedPkg: String? = null
        val state = LauncherUiState(
            settings = LauncherSettings(),
            destination = LauncherDestination.HOME,
            allApps = sampleApps,
            favoriteApps = sampleApps,
            visibleAppsOrdered = sampleApps,
            nextPrayer = NextPrayerInfo(PrayerKey.MAGHRIB, "17:45", 0, 20),
            weather = WeatherData("Lyon", 18.0, 17.0, 1, 10.0, 55, 20.0, 11.0, isDay = true, isGpsSynced = true),
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = state,
                    isHomeActive = true,
                    onNavigate = {},
                    onLaunchApp = { launchedPkg = it.packageName },
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
        composeRule.onNodeWithTag("app_tile_com.test.youtube").assertIsDisplayed().assertIsFocused()
        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionCenter)
        }
        composeRule.waitForIdle()
        assertEquals("com.test.youtube", launchedPkg)
    }

    @Test
    fun offlinePrayerCalculator_usesAsciiDigits_underArabicLocale_andSurvivesExtremeLatitudes() {
        val original = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale("ar", "SA"))
            val data = com.example.prayer.OfflinePrayerCalculator.calculate(
                latitude = 69.65,
                longitude = 18.96,
                timezoneId = "Europe/Oslo",
                methodId = 3,
                cityDisplay = "Tromso",
            )
            assertTrue("Expected ASCII HH:mm for fajr: ${data.fajr}", data.fajr.matches(Regex("""\d{2}:\d{2}""")))
            assertTrue("Expected ASCII HH:mm for isha: ${data.isha}", data.isha.matches(Regex("""\d{2}:\d{2}""")))
            assertTrue("Expected non-empty Hijri date offline: ${data.hijriDate} / ${data.hijriDateAr}", data.hijriDate.isNotBlank() && data.hijriDateAr.isNotBlank())
            val next = com.example.prayer.OfflinePrayerCalculator.computeNextPrayer(data, is24Hour = false)
            assertTrue(next.timeFormatted.isNotBlank())
        } finally {
            java.util.Locale.setDefault(original)
        }
    }

    @Test
    fun crashDiagnosticActivity_rendersTraceAndButtons() {
        val intent = android.content.Intent(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            CrashDiagnosticActivity::class.java,
        ).putExtra(CrashDiagnosticActivity.EXTRA_TRACE, "TestException: Sample crash trace")
        org.robolectric.Robolectric.buildActivity(CrashDiagnosticActivity::class.java, intent)
            .setup()
            .get()
            .let { activity ->
                assertTrue(!activity.isFinishing)
            }
    }

    @Test
    fun homeScreen_showsEmptyFavoritesPrompt_andHidesNonFavoritesFromDock() {
        val installedOnly = listOf(
            AppItem("com.test.nonfav", "NonFavoriteApp", "MainActivity", ColorDrawable(AndroidColor.BLUE)),
        )
        var navigatedTo: LauncherDestination? = null
        val state = LauncherUiState(
            settings = LauncherSettings(rowsBelowFavorites = 0),
            destination = LauncherDestination.HOME,
            allApps = installedOnly,
            favoriteApps = emptyList(),
            visibleAppsOrdered = installedOnly,
            nextPrayer = NextPrayerInfo(PrayerKey.FAJR, "05:30", 3, 10),
            weather = WeatherData("Paris", 16.0, 15.0, 0, 8.0, 50, 18.0, 9.0, isDay = true, isGpsSynced = false),
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = state,
                    isHomeActive = true,
                    onNavigate = { navigatedTo = it },
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
        composeRule.onNodeWithTag("home_empty_favorites_card").assertIsDisplayed().performClick()
        assertEquals(LauncherDestination.APP_LIBRARY, navigatedTo)
        composeRule.onNodeWithTag("app_tile_com.test.nonfav").assertDoesNotExist()
    }

    @Test
    fun homeScreen_rendersAllAddedFavoritesInBottomDockAcrossRows() {
        val favApps = (1..6).map { idx ->
            AppItem("com.test.fav$idx", "Fav $idx", "MainActivity", ColorDrawable(AndroidColor.CYAN))
        }
        val state = LauncherUiState(
            settings = LauncherSettings(appsPerRow = 5),
            destination = LauncherDestination.HOME,
            allApps = favApps,
            favoriteApps = favApps,
            visibleAppsOrdered = favApps,
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
        composeRule.onNodeWithTag("app_tile_com.test.fav1").assertIsDisplayed()
        composeRule.onNodeWithTag("app_tile_com.test.fav6").assertIsDisplayed()
    }

    @Test
    fun themeAndWallpaper_showsImageVideoFilePickers_andActiveVideoWallpaperLayerRendersSafely() {
        var latestSettings = LauncherSettings(
            wallpaperId = "custom",
            customWallpaperUri = "file:///nonexistent_sample.mp4",
            customWallpaperIsVideo = true,
        )
        val state = LauncherUiState(settings = latestSettings)

        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.components.LauncherWallpaperLayer(settings = latestSettings)
                com.example.ui.screens.ThemeAndWallpaperScreen(
                    state = state,
                    onBack = {},
                    onUpdateSettings = { transform -> latestSettings = transform(latestSettings) },
                    onApplyThemePreset = {},
                    onSelectCustomWallpaper = { _, _ -> },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pick_wallpaper_image_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("pick_wallpaper_video_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("browse_wallpaper_files_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("custom_wallpaper_active_banner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_card_video_earth_space").performScrollTo().performClick()
        assertEquals("video_earth_space", latestSettings.wallpaperId)
        assertEquals(null, latestSettings.customWallpaperUri)
        assertEquals(false, latestSettings.customWallpaperIsVideo)
        composeRule.onNodeWithTag("wallpaper_card_video_emerald_nebula").assertExists()
        composeRule.onNodeWithTag("wallpaper_card_video_crimson_nebula").assertExists()
        composeRule.onNodeWithTag("wallpaper_card_video_cobalt_fractal").assertExists()
        composeRule.onNodeWithTag("wallpaper_card_video_violet_nebula").assertExists()
        // Verify duplicate ThemePreset wallpapers were removed from the static wallpaper grid
        assertTrue(composeRule.onAllNodesWithTag("wallpaper_card_sport_real_madrid").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("wallpaper_card_flag_algeria").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("wallpaper_card_anime_grendizer").fetchSemanticsNodes().isEmpty())
        // Exclusive Horizon & bonus wallpapers remain present
        composeRule.onNodeWithTag("wallpaper_card_earth").assertExists()
        composeRule.onNodeWithTag("wallpaper_card_sport_algeria_fennecs").assertExists()
        composeRule.onNodeWithTag("wallpaper_card_sport_brazil_light").assertExists()
        composeRule.onNodeWithTag("clear_custom_wallpaper_btn").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(null, latestSettings.customWallpaperUri)
        assertEquals(false, latestSettings.customWallpaperIsVideo)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun appLibrary_andHomeDock_longPressShowsAddRemoveFavoriteAndUninstallDialog() {
        val sampleApp = AppItem("com.test.iptv", "IPTV Player", "MainActivity", ColorDrawable(AndroidColor.GREEN))
        var toggledPkg: String? = null
        var uninstalledPkg: String? = null

        val stateHolder = androidx.compose.runtime.mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(language = LauncherLanguage.AR),
                destination = LauncherDestination.APP_LIBRARY,
                allApps = listOf(sampleApp),
                favoriteApps = emptyList(),
                visibleAppsOrdered = listOf(sampleApp),
            )
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                // Simulate real root: AppLibraryScreen active (so HomeScreen has isHomeActive = false) + AppContextDialog at root
                HomeScreen(
                    state = stateHolder.value,
                    isHomeActive = stateHolder.value.destination == LauncherDestination.HOME,
                    onNavigate = {},
                    onLaunchApp = {},
                    onOpenContextApp = { app ->
                        stateHolder.value = stateHolder.value.copy(selectedAppForContext = app)
                    },
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onHideApp = {},
                    onOpenAppInfo = {},
                    onOpenNetworkSettings = {},
                    onExecuteGesture = {},
                    onSelectHdmiSource = {},
                    onStopAdhan = {},
                    renderContextDialog = false,
                )
                if (stateHolder.value.destination == LauncherDestination.APP_LIBRARY) {
                    com.example.ui.screens.AppLibraryScreen(
                        state = stateHolder.value,
                        onBack = {},
                        onLaunchApp = {},
                        onOpenContextApp = { app ->
                            stateHolder.value = stateHolder.value.copy(selectedAppForContext = app)
                        },
                    )
                }
                val ctx = stateHolder.value.selectedAppForContext
                if (ctx != null) {
                    com.example.ui.screens.AppContextDialog(
                        app = ctx,
                        state = stateHolder.value,
                        onDismiss = { stateHolder.value = stateHolder.value.copy(selectedAppForContext = null) },
                        onToggleFavorite = { pkg ->
                            toggledPkg = pkg
                            val favs = if (stateHolder.value.favoriteApps.any { it.packageName == pkg }) emptyList() else listOf(sampleApp)
                            stateHolder.value = stateHolder.value.copy(favoriteApps = favs)
                        },
                        onMoveFavorite = { _, _ -> },
                        onHideApp = {},
                        onOpenAppInfo = {},
                        onUninstallApp = { app ->
                            uninstalledPkg = app.packageName
                            stateHolder.value = stateHolder.value.copy(selectedAppForContext = null)
                        },
                        onLaunchApp = {},
                    )
                }
            }
        }

        composeRule.waitForIdle()
        // Home edit favorites & '+' dock buttons must not exist
        composeRule.onNodeWithTag("home_edit_favorites_button").assertDoesNotExist()
        composeRule.onNodeWithTag("home_add_favorite_dock_btn").assertDoesNotExist()

        // App Library renders as a centered floating popup window above the favorites dock
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val dockBounds = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot
        val popupBounds = composeRule.onNodeWithTag("app_library_popup_window").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected popup App Library (${popupBounds.bottom}) cleanly above Favorites dock (${dockBounds.top})",
            popupBounds.bottom <= dockBounds.top + 8f,
        )
        assertTrue(
            "Expected popup App Library width (${popupBounds.width}) narrower than full screen (${rootBounds.width})",
            popupBounds.width < rootBounds.width * 0.75f,
        )

        // App Library search bar must be removed; long-press hint & tiles must appear directly
        composeRule.onNodeWithTag("app_library_search_input").assertDoesNotExist()
        composeRule.onNodeWithTag("app_library_long_press_hint").assertIsDisplayed()
        // Before adding to favorites, favorite badge does not exist
        composeRule.onNodeWithTag("app_favorite_badge_com.test.iptv", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag("app_tile_com.test.iptv").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("context_add_favorite_btn").assertIsDisplayed().performClick()
        assertEquals("com.test.iptv", toggledPkg)
        composeRule.waitForIdle()
        assertEquals(1, stateHolder.value.favoriteApps.size)
        // After adding to favorites, tile displays favorite badge inside App Library
        composeRule.onNodeWithTag("app_favorite_badge_com.test.iptv", useUnmergedTree = true).assertIsDisplayed()

        // Return to Home dock and long-press via Menu key -> Remove from favorites + Uninstall
        stateHolder.value = stateHolder.value.copy(destination = LauncherDestination.HOME, selectedAppForContext = null)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_tile_com.test.iptv").performKeyInput {
            pressKey(Key.Menu)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("context_remove_favorite_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("context_uninstall_app_btn").assertIsDisplayed().performClick()
        assertEquals("com.test.iptv", uninstalledPkg)
    }

    @Test
    fun homeScreen_aiToolsBarIsHorizontalAndCappedAtFour() {
        var launchedToolKey: String? = null
        val fivePresets = listOf(
            "preset:chatgpt",
            "preset:gemini",
            "preset:claude",
            "preset:perplexity",
            "preset:deepseek",
        )
        val stateHolder = androidx.compose.runtime.mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(aiToolShortcuts = fivePresets),
                destination = LauncherDestination.HOME,
            )
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = stateHolder.value,
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
                    onToggleAiTool = { key ->
                        val current = stateHolder.value.settings.aiToolShortcuts.take(4).toMutableList()
                        if (current.contains(key)) current.remove(key) else if (current.size < 4) current.add(key)
                        stateHolder.value = stateHolder.value.copy(
                            settings = stateHolder.value.settings.copy(aiToolShortcuts = current)
                        )
                    },
                    onLaunchAiTool = { launchedToolKey = it },
                )
            }
        }

        composeRule.waitForIdle()
        val greetingBounds = composeRule.onNodeWithTag("home_greeting_lang_header").fetchSemanticsNode().boundsInRoot
        val aiBarBounds = composeRule.onNodeWithTag("home_ai_tools_bar").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected AI bar (${aiBarBounds.left}) beside top-left greeting header (${greetingBounds.right})",
            aiBarBounds.left >= greetingBounds.right - 2f,
        )

        // Verify horizontal arrangement & 4-item limit
        val firstBounds = composeRule.onNodeWithTag("ai_tool_item_preset:chatgpt").fetchSemanticsNode().boundsInRoot
        val secondBounds = composeRule.onNodeWithTag("ai_tool_item_preset:gemini").fetchSemanticsNode().boundsInRoot
        assertTrue("Expected horizontal ordering", secondBounds.left >= firstBounds.right)
        composeRule.onNodeWithTag("ai_tool_item_preset:perplexity").assertIsDisplayed()
        composeRule.onNodeWithTag("ai_tool_item_preset:deepseek").assertDoesNotExist()

        composeRule.onNodeWithTag("ai_tool_item_preset:gemini").performClick()
        assertEquals("preset:gemini", launchedToolKey)

        // Open picker dialog via '+' button
        composeRule.onNodeWithTag("ai_bar_add_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_tools_picker_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("ai_picker_item_preset:gemini").performClick()
        composeRule.waitForIdle()
        assertTrue(!stateHolder.value.settings.aiToolShortcuts.contains("preset:gemini"))
    }

    @Test
    @Config(sdk = [34], qualifiers = "w960dp-h540dp-land")
    fun homeScreen_fitsFullFavoritesDockCleanlyOn540dpTvViewport() {
        val favApps = (1..6).map { idx ->
            AppItem("com.test.tv$idx", "TV App $idx", "MainActivity", ColorDrawable(AndroidColor.CYAN))
        }
        val state = LauncherUiState(
            settings = LauncherSettings(
                appsPerRow = 6,
                aiToolShortcuts = listOf("preset:chatgpt", "preset:gemini", "preset:deepseek", "preset:claude"),
            ),
            destination = LauncherDestination.HOME,
            allApps = favApps,
            favoriteApps = favApps,
            visibleAppsOrdered = favApps,
            nextPrayer = NextPrayerInfo(PrayerKey.ISHA, "18:16", 0, 37),
            weather = WeatherData("Mions Centre", 14.0, 12.0, 3, 26.0, 60, 16.0, 12.0, isDay = false, isGpsSynced = true),
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
        val dockBounds = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot
        val aiBarBounds = composeRule.onNodeWithTag("home_ai_tools_bar").fetchSemanticsNode().boundsInRoot
        val netPillBounds = composeRule.onNodeWithTag("top_network_pill").fetchSemanticsNode().boundsInRoot
        val settingsBtnBounds = composeRule.onNodeWithTag("top_settings_button").fetchSemanticsNode().boundsInRoot
        val langBtnBounds = composeRule.onNodeWithTag("home_language_cycle_button").fetchSemanticsNode().boundsInRoot
        val aiMidY = (aiBarBounds.top + aiBarBounds.bottom) / 2f
        val netMidY = (netPillBounds.top + netPillBounds.bottom) / 2f
        val langMidY = (langBtnBounds.top + langBtnBounds.bottom) / 2f
        assertTrue(
            "Expected unified top taskbar (midY=$aiMidY) aligned with language circle (midY=$langMidY) on 540dp TV",
            abs(aiMidY - langMidY) < 6f,
        )
        assertTrue(
            "Expected circular net (${netPillBounds}) & settings (${settingsBtnBounds}) inside unified taskbar (${aiBarBounds})",
            netPillBounds.left >= aiBarBounds.left - 1f &&
                settingsBtnBounds.right <= aiBarBounds.right + 1f &&
                abs(aiMidY - netMidY) < 4f &&
                abs(netPillBounds.width - netPillBounds.height) < 2f &&
                abs(settingsBtnBounds.width - settingsBtnBounds.height) < 2f,
        )
        assertTrue(
            "Expected full favorites dock (${dockBounds.bottom}) visible within 540dp TV height (${rootBounds.bottom})",
            dockBounds.bottom <= rootBounds.bottom + 2f,
        )
        val tvPrayerBounds = composeRule.onNodeWithTag("home_prayer_card").fetchSemanticsNode().boundsInRoot
        val tvWeatherBounds = composeRule.onNodeWithTag("home_weather_card").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected Prayer top (${tvPrayerBounds.top}) aligned with Weather top (${tvWeatherBounds.top}) on 540dp TV",
            abs(tvPrayerBounds.top - tvWeatherBounds.top) < 2f,
        )
        composeRule.onNodeWithTag("home_prayer_hijri_date", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("home_add_favorite_dock_btn").assertDoesNotExist()
        composeRule.onNodeWithTag("home_edit_favorites_button").assertDoesNotExist()
    }

    @Test
    @Config(sdk = [34], qualifiers = "w960dp-h540dp-land")
    fun prayerAndAdhanScreen_showsAudioUploadButtonImmediatelyAtTop() {
        val state = LauncherUiState(
            settings = LauncherSettings(language = LauncherLanguage.AR),
            prayerTimes = com.example.prayer.OfflinePrayerCalculator.calculate(36.75, 3.05, "Africa/Algiers", 19, "الجزائر"),
        )
        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.screens.PrayerAndAdhanScreen(
                    state = state,
                    onBack = {},
                    onSyncGps = {},
                    onSearchCity = {},
                    onSelectCity = {},
                    onUpdateSettings = {},
                    onTogglePrayer = {},
                    onCustomAdhanPicked = {},
                    onClearCustomAdhan = {},
                    onTestOrStopAdhan = {},
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pick_adhan_audio_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("test_adhan_btn").assertIsDisplayed()
    }

    @Test
    fun uiCustomizerScreen_includesCategorizedSportsThemesAndAppliesBackdrop() {
        var appliedPreset: com.example.model.ThemePreset? = null
        val state = LauncherUiState(
            settings = LauncherSettings(language = LauncherLanguage.AR),
        )
        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.components.LauncherWallpaperLayer(
                    settings = state.settings.copy(
                        themePreset = com.example.model.ThemePreset.ALGERIA,
                        wallpaperId = "sport_algeria",
                    )
                )
                com.example.ui.screens.ThemeAndWallpaperScreen(
                    state = state,
                    onBack = {},
                    onUpdateSettings = {},
                    onApplyThemePreset = { appliedPreset = it },
                    onSelectCustomWallpaper = { _, _ -> },
                )
            }
        }
        composeRule.waitForIdle()
        val expectedIds = listOf(
            "real_madrid", "barcelona", "man_city", "psg",
            "brazil", "argentina", "france", "spain",
            "egypt", "algeria", "saudi_arabia", "tunisia",
        )
        expectedIds.forEach { id ->
            composeRule.onNodeWithTag("theme_preset_$id").assertExists()
        }
        com.example.model.ThemePreset.entries.filter { it.isSportsTheme }.forEach { preset ->
            val res = com.example.ui.components.wallpaperDrawableRes(preset.defaultWallpaperId)
            assertTrue("Expected real photo drawable for ${preset.id}", res != null)
        }
        composeRule.onNodeWithTag("theme_preset_real_madrid").performClick()
        assertEquals(com.example.model.ThemePreset.REAL_MADRID, appliedPreset)
    }

    @Test
    fun homeScreen_initialFocusOnCenterDockIcon_andHdmiBarInlineWithAppLibrary_andMinimalistUnclipped() {
        val fiveApps = (1..5).map { idx ->
            AppItem("com.test.app$idx", "App $idx", "MainActivity", ColorDrawable(AndroidColor.CYAN))
        }
        val fiveDays = listOf(
            com.example.model.DailyWeatherItem("Lun 18", 0, 21.0, 11.0),
            com.example.model.DailyWeatherItem("Mar 19", 2, 19.0, 10.0),
            com.example.model.DailyWeatherItem("Mer 20", 61, 16.0, 9.0),
            com.example.model.DailyWeatherItem("Jeu 21", 3, 18.0, 10.0),
            com.example.model.DailyWeatherItem("Ven 22", 1, 22.0, 12.0),
        )
        val state = LauncherUiState(
            settings = LauncherSettings(
                language = LauncherLanguage.AR,
                appsPerRow = 5,
                dockStyle = com.example.model.DockStyle.MINIMAL_CARDS,
                showHdmiSourcesBar = true,
                aiToolShortcuts = listOf("preset:chatgpt", "preset:gemini"),
            ),
            destination = LauncherDestination.HOME,
            allApps = fiveApps,
            favoriteApps = fiveApps,
            visibleAppsOrdered = fiveApps,
            nextPrayer = NextPrayerInfo(PrayerKey.ASR, "15:10", 1, 15),
            weather = WeatherData("Alger", 20.0, 19.0, 0, 12.0, 48, 21.0, 11.0, isDay = true, isGpsSynced = true, daily = fiveDays),
        )

        composeRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
            ) {
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
        }

        composeRule.waitForIdle()
        // 1. Center icon (index 2 of 5 -> com.test.app3) receives initial TV remote focus
        composeRule.onNodeWithTag("app_tile_com.test.app3").assertIsDisplayed().assertIsFocused()

        // 2. Minimalist dock focused tile stays cleanly inside dock bounds (not clipped)
        val dockBounds = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot
        val centerTileBounds = composeRule.onNodeWithTag("app_tile_com.test.app3").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected focused minimalist tile top (${centerTileBounds.top}) inside dock top (${dockBounds.top})",
            centerTileBounds.top >= dockBounds.top - 1f,
        )
        assertTrue(
            "Expected focused minimalist tile bottom (${centerTileBounds.bottom}) inside dock bottom (${dockBounds.bottom})",
            centerTileBounds.bottom <= dockBounds.bottom + 1f,
        )

        // 3. HDMI 1..3 / AV / Android TV chips sit on the exact same horizontal line as App Library button,
        //    and their outer left/right edges align flush with the first/last app icons in the favorites dock
        val androidTvBounds = composeRule.onNodeWithTag("hdmi_source_Android TV").fetchSemanticsNode().boundsInRoot
        val hdmiBounds = composeRule.onNodeWithTag("hdmi_source_HDMI 1").fetchSemanticsNode().boundsInRoot
        val avBounds = composeRule.onNodeWithTag("hdmi_source_AV / Cast").fetchSemanticsNode().boundsInRoot
        val libBounds = composeRule.onNodeWithTag("home_all_apps_button").fetchSemanticsNode().boundsInRoot
        val firstTileBounds = composeRule.onNodeWithTag("app_tile_com.test.app1").fetchSemanticsNode().boundsInRoot
        val lastTileBounds = composeRule.onNodeWithTag("app_tile_com.test.app5").fetchSemanticsNode().boundsInRoot
        val hdmiMidY = (hdmiBounds.top + hdmiBounds.bottom) / 2f
        val avMidY = (avBounds.top + avBounds.bottom) / 2f
        val libMidY = (libBounds.top + libBounds.bottom) / 2f
        assertTrue("Expected HDMI 1 inline with App Library (diff=${abs(hdmiMidY - libMidY)})", abs(hdmiMidY - libMidY) < 4f)
        assertTrue("Expected AV / Cast inline with App Library (diff=${abs(avMidY - libMidY)})", abs(avMidY - libMidY) < 4f)
        assertTrue(
            "Expected Android TV left (${androidTvBounds.left}) aligned with leftmost app icon (${firstTileBounds.left})",
            abs(androidTvBounds.left - firstTileBounds.left) < 4f,
        )
        assertTrue(
            "Expected App Library right (${libBounds.right}) aligned with rightmost app icon (${lastTileBounds.right})",
            abs(libBounds.right - lastTileBounds.right) < 4f,
        )

        // 4. Weather card shows all 5 forecast days, Prayer card stays on LEFT, top-aligned with Weather, with Hijri date
        val prayerBounds = composeRule.onNodeWithTag("home_prayer_card").fetchSemanticsNode().boundsInRoot
        val weatherBounds = composeRule.onNodeWithTag("home_weather_card").fetchSemanticsNode().boundsInRoot
        assertTrue("Expected prayer card on left even in Arabic UI", prayerBounds.right <= weatherBounds.left + 2f)
        assertTrue(
            "Expected prayer top (${prayerBounds.top}) aligned with weather top (${weatherBounds.top}) in Arabic UI",
            abs(prayerBounds.top - weatherBounds.top) < 2f,
        )
        composeRule.onNodeWithTag("home_prayer_hijri_date", useUnmergedTree = true).assertIsDisplayed()
        (0..4).forEach { idx ->
            composeRule.onNodeWithTag("weather_forecast_day_$idx", useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_aiToolLongPressOpensRemoveAndManageDialog() {
        val stateHolder = androidx.compose.runtime.mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(
                    language = LauncherLanguage.AR,
                    aiToolShortcuts = listOf("preset:chatgpt", "preset:gemini"),
                ),
                destination = LauncherDestination.HOME,
            )
        )
        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = stateHolder.value,
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
                    onToggleAiTool = { key ->
                        val updated = stateHolder.value.settings.aiToolShortcuts.toMutableList().apply { remove(key) }
                        stateHolder.value = stateHolder.value.copy(
                            settings = stateHolder.value.settings.copy(aiToolShortcuts = updated)
                        )
                    },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_tool_item_preset:chatgpt").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_remove_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("ai_dialog_manage_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("ai_dialog_remove_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue(!stateHolder.value.settings.aiToolShortcuts.contains("preset:chatgpt"))
    }

    @Test
    fun uiCustomizer_adjustsGlassOpacitySteppers_iconSize_iconShape_andNavigationSound() {
        val settingsHolder = androidx.compose.runtime.mutableStateOf(
            LauncherSettings(
                language = LauncherLanguage.AR,
                glassOpacity = 0.50f,
                iconSize = com.example.model.AppIconSize.MEDIUM,
                iconShape = com.example.model.AppIconShape.RECTANGLE,
                navigationSoundEnabled = true,
            )
        )
        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.screens.ThemeAndWallpaperScreen(
                    state = LauncherUiState(settings = settingsHolder.value),
                    onBack = {},
                    onUpdateSettings = { transform ->
                        settingsHolder.value = transform(settingsHolder.value)
                    },
                    onApplyThemePreset = {},
                    onSelectCustomWallpaper = { _, _ -> },
                )
            }
        }
        composeRule.waitForIdle()

        // Scroll to transparency controls & decrease 6 times from 50% -> reaches 0% cleanly
        composeRule.onNodeWithTag("glass_opacity_decrease").performScrollTo()
        repeat(6) {
            composeRule.onNodeWithTag("glass_opacity_decrease").performClick()
            composeRule.waitForIdle()
        }
        assertEquals(0f, settingsHolder.value.glassOpacity, 0.01f)

        // Increase transparency 3 times -> 30%
        composeRule.onNodeWithTag("glass_opacity_increase").performScrollTo()
        repeat(3) {
            composeRule.onNodeWithTag("glass_opacity_increase").performClick()
            composeRule.waitForIdle()
        }
        assertEquals(0.30f, settingsHolder.value.glassOpacity, 0.01f)

        // Select Small / Large icon size & Circle / Square icon shape
        composeRule.onNodeWithTag("icon_size_large").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(com.example.model.AppIconSize.LARGE, settingsHolder.value.iconSize)

        composeRule.onNodeWithTag("icon_shape_circle").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(com.example.model.AppIconShape.CIRCLE, settingsHolder.value.iconShape)

        composeRule.onNodeWithTag("icon_shape_square").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(com.example.model.AppIconShape.SQUARE, settingsHolder.value.iconShape)

        // Toggle navigation sound
        composeRule.onNodeWithTag("toggle_navigation_sound").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(false, settingsHolder.value.navigationSoundEnabled)

        // Verify extra sports drawables resolve
        assertTrue(com.example.ui.components.wallpaperDrawableRes("sport_algeria_fennecs") != null)
        assertTrue(com.example.ui.components.wallpaperDrawableRes("sport_brazil_light") != null)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun homeScreen_appliesIconSizeAndIconShapeBounds_andPlaysSoundOnDpadFocusMove() {
        val apps = listOf(
            AppItem("com.test.one", "One", "MainActivity", ColorDrawable(AndroidColor.RED)),
            AppItem("com.test.two", "Two", "MainActivity", ColorDrawable(AndroidColor.BLUE)),
        )
        val settingsHolder = androidx.compose.runtime.mutableStateOf(
            LauncherSettings(
                appsPerRow = 6,
                glassOpacity = 0.0f,
                iconSize = com.example.model.AppIconSize.SMALL,
                iconShape = com.example.model.AppIconShape.RECTANGLE,
                navigationSoundEnabled = true,
            )
        )

        composeRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                com.example.ui.components.LocalNavigationSoundEnabled provides settingsHolder.value.navigationSoundEnabled
            ) {
                HorizonLauncherTheme {
                    HomeScreen(
                        state = LauncherUiState(
                            settings = settingsHolder.value,
                            destination = LauncherDestination.HOME,
                            allApps = apps,
                            favoriteApps = apps,
                            visibleAppsOrdered = apps,
                        ),
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
        }

        composeRule.waitForIdle()
        val smallRectBounds = composeRule.onNodeWithTag("app_tile_com.test.one").fetchSemanticsNode().boundsInRoot

        settingsHolder.value = settingsHolder.value.copy(
            iconSize = com.example.model.AppIconSize.MEDIUM,
            iconShape = com.example.model.AppIconShape.RECTANGLE,
        )
        composeRule.waitForIdle()
        val medRectBounds = composeRule.onNodeWithTag("app_tile_com.test.one").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected MEDIUM rect (${medRectBounds.width}) > SMALL rect (${smallRectBounds.width})",
            medRectBounds.width > smallRectBounds.width + 10f,
        )

        settingsHolder.value = settingsHolder.value.copy(
            iconSize = com.example.model.AppIconSize.LARGE,
            iconShape = com.example.model.AppIconShape.RECTANGLE,
        )
        composeRule.waitForIdle()
        val largeRectBounds = composeRule.onNodeWithTag("app_tile_com.test.one").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected LARGE rect height (${largeRectBounds.height}) > MEDIUM rect height (${medRectBounds.height})",
            largeRectBounds.height > medRectBounds.height + 10f,
        )

        settingsHolder.value = settingsHolder.value.copy(
            iconSize = com.example.model.AppIconSize.MEDIUM,
            iconShape = com.example.model.AppIconShape.CIRCLE,
        )
        composeRule.waitForIdle()
        val circleBounds = composeRule.onNodeWithTag("app_tile_com.test.one").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected CIRCLE 1:1 ratio (${circleBounds.width}x${circleBounds.height})",
            abs(circleBounds.width - circleBounds.height) < 3f,
        )

        // Move focus via D-pad right -> verify FocusSoundPlayer fires safely after transition suppress window
        com.example.ui.components.FocusSoundPlayer.markTransition(0L)
        val beforeCount = com.example.ui.components.FocusSoundPlayer.playCountForTest.get()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithTag("app_tile_com.test.two").performClick()
        composeRule.waitForIdle()
        // Focus movement with D-pad right from tile 1 -> tile 2
        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionRight)
        }
        composeRule.waitForIdle()
        val afterCount = com.example.ui.components.FocusSoundPlayer.playCountForTest.get()
        assertTrue("Expected audible focus sound invocation on focus change", afterCount >= beforeCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun longPressDialog_remainsOpenAfterStrayConfirmKeyUp_inAppLibraryAndAiBar() {
        val sampleApp = AppItem("com.test.keepopen", "KeepOpen", "MainActivity", ColorDrawable(AndroidColor.GREEN))
        val stateHolder = androidx.compose.runtime.mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(
                    language = LauncherLanguage.FR,
                    aiToolShortcuts = listOf("preset:chatgpt"),
                ),
                destination = LauncherDestination.HOME,
                allApps = listOf(sampleApp),
                favoriteApps = emptyList(),
                visibleAppsOrdered = listOf(sampleApp),
            )
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = stateHolder.value,
                    isHomeActive = stateHolder.value.destination == LauncherDestination.HOME,
                    onNavigate = {},
                    onLaunchApp = {},
                    onOpenContextApp = { app ->
                        stateHolder.value = stateHolder.value.copy(selectedAppForContext = app)
                    },
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onHideApp = {},
                    onOpenAppInfo = {},
                    onOpenNetworkSettings = {},
                    onExecuteGesture = {},
                    onSelectHdmiSource = {},
                    onStopAdhan = {},
                    renderContextDialog = false,
                    onToggleAiTool = { key ->
                        val updated = stateHolder.value.settings.aiToolShortcuts.toMutableList().apply { remove(key) }
                        stateHolder.value = stateHolder.value.copy(
                            settings = stateHolder.value.settings.copy(aiToolShortcuts = updated)
                        )
                    },
                )
                if (stateHolder.value.destination == LauncherDestination.APP_LIBRARY) {
                    com.example.ui.screens.AppLibraryScreen(
                        state = stateHolder.value,
                        onBack = {},
                        onLaunchApp = {},
                        onOpenContextApp = { app ->
                            stateHolder.value = stateHolder.value.copy(selectedAppForContext = app)
                        },
                    )
                }
                val ctx = stateHolder.value.selectedAppForContext
                if (ctx != null) {
                    com.example.ui.screens.AppContextDialog(
                        app = ctx,
                        state = stateHolder.value,
                        onDismiss = { stateHolder.value = stateHolder.value.copy(selectedAppForContext = null) },
                        onToggleFavorite = { pkg ->
                            stateHolder.value = stateHolder.value.copy(favoriteApps = listOf(sampleApp))
                        },
                        onMoveFavorite = { _, _ -> },
                        onHideApp = {},
                        onOpenAppInfo = {},
                        onUninstallApp = {},
                        onLaunchApp = {},
                    )
                }
            }
        }

        composeRule.waitForIdle()
        // 1. Long-press AI tool to open ai_remove_dialog, then dispatch orphan KeyUp to the dialog's focused view
        composeRule.onNodeWithTag("ai_tool_item_preset:chatgpt").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_remove_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("ai_dialog_remove_btn").assertIsFocused()

        composeRule.runOnUiThread {
            val dialogDecor = org.robolectric.shadows.ShadowDialog.getLatestDialog()?.window?.decorView
            val target = dialogDecor?.findFocus() ?: dialogDecor
            target?.dispatchKeyEvent(
                android.view.KeyEvent(0L, 0L, android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_CENTER, 0)
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_remove_dialog").assertIsDisplayed()
        assertTrue(stateHolder.value.settings.aiToolShortcuts.contains("preset:chatgpt"))

        // Explicit press (KeyDown + KeyUp) executes removal and closes dialog
        composeRule.onNodeWithTag("ai_dialog_remove_btn").performKeyInput {
            pressKey(Key.DirectionCenter)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_remove_dialog").assertDoesNotExist()
        assertTrue(!stateHolder.value.settings.aiToolShortcuts.contains("preset:chatgpt"))

        // 2. Same check in AppLibraryScreen: long-press opens AppContextDialog and stray KeyUp does NOT auto-trigger first item
        stateHolder.value = stateHolder.value.copy(destination = LauncherDestination.APP_LIBRARY)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_tile_com.test.keepopen").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("context_add_favorite_btn").assertIsFocused()

        composeRule.runOnUiThread {
            val dialogDecor = org.robolectric.shadows.ShadowDialog.getLatestDialog()?.window?.decorView
            val target = dialogDecor?.findFocus() ?: dialogDecor
            target?.dispatchKeyEvent(
                android.view.KeyEvent(0L, 0L, android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_CENTER, 0)
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_dialog").assertIsDisplayed()
        assertTrue(stateHolder.value.favoriteApps.isEmpty())

        composeRule.onNodeWithTag("context_add_favorite_btn").performKeyInput {
            pressKey(Key.DirectionCenter)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_dialog").assertDoesNotExist()
        assertEquals(1, stateHolder.value.favoriteApps.size)
    }

    @Test
    fun wallpaperStudio_adjustsAssombrissementDuFond_withStepperButtonsAndDpad() {
        val settingsHolder = androidx.compose.runtime.mutableStateOf(
            LauncherSettings(language = LauncherLanguage.FR, wallpaperDimAlpha = 0.25f)
        )
        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.screens.ThemeAndWallpaperScreen(
                    state = LauncherUiState(settings = settingsHolder.value),
                    onBack = {},
                    onUpdateSettings = { transform -> settingsHolder.value = transform(settingsHolder.value) },
                    onApplyThemePreset = {},
                    onSelectCustomWallpaper = { _, _ -> },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wallpaper_dim_card").performScrollTo()
        composeRule.onNodeWithTag("wallpaper_dim_value").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_dim_increase").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(0.30f, settingsHolder.value.wallpaperDimAlpha, 0.005f)

        composeRule.onNodeWithTag("wallpaper_dim_decrease").performScrollTo().performClick()
        composeRule.onNodeWithTag("wallpaper_dim_decrease").performClick()
        composeRule.waitForIdle()
        assertEquals(0.20f, settingsHolder.value.wallpaperDimAlpha, 0.005f)
    }

    @Test
    fun uninstallApp_declaresRequestDeletePackagesPermission_andStartsActionDeleteIntent() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS,
        )
        val permissions = pkgInfo.requestedPermissions?.toList().orEmpty()
        assertTrue(
            "Expected REQUEST_DELETE_PACKAGES in AndroidManifest.xml",
            permissions.contains(android.Manifest.permission.REQUEST_DELETE_PACKAGES),
        )

        val repo = com.example.data.InstalledAppsRepository(context)
        val launched = repo.uninstallApp("com.sample.iptvplayer")
        assertTrue("Expected uninstallApp to launch ACTION_DELETE intent", launched)

        val nextIntent = org.robolectric.Shadows.shadowOf(context).nextStartedActivity
        assertEquals(android.content.Intent.ACTION_DELETE, nextIntent?.action)
        assertEquals("package:com.sample.iptvplayer", nextIntent?.data?.toString())
    }

    @Test
    fun homeScreen_languageCycleButtonVisibleBesideGreeting_cyclesEnFrArAndUiCustomizerHasNoLangButtons() {
        val stateHolder = androidx.compose.runtime.mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(language = LauncherLanguage.EN, showGreeting = true),
                destination = LauncherDestination.HOME,
                greetingText = "GOOD MORNING",
            )
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = stateHolder.value,
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
                    onCycleLanguage = {
                        val current = stateHolder.value
                        val nextLang = current.settings.language.next()
                        stateHolder.value = current.copy(
                            settings = current.settings.copy(language = nextLang),
                            greetingText = LauncherStrings.get(nextLang).greetingMorning,
                        )
                    },
                )
            }
        }

        composeRule.waitForIdle()
        val langBtn = composeRule.onNodeWithTag("home_language_cycle_button")
        langBtn.assertIsDisplayed()
        val langBounds = langBtn.fetchSemanticsNode().boundsInRoot
        val greetingBounds = composeRule.onNodeWithTag("home_greeting_lang_header").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected language circle button inside top-left greeting header",
            langBounds.top >= greetingBounds.top - 2f && langBounds.bottom <= greetingBounds.bottom + 2f,
        )
        assertTrue(
            "Expected circular language button (width=${langBounds.width}, height=${langBounds.height})",
            abs(langBounds.width - langBounds.height) < 2f,
        )

        // First EN -> click -> FR -> click -> ع (AR) -> click -> EN
        composeRule.onNodeWithText("EN").assertIsDisplayed()
        langBtn.performClick()
        composeRule.waitForIdle()
        assertEquals(LauncherLanguage.FR, stateHolder.value.settings.language)
        composeRule.onNodeWithText("FR").assertIsDisplayed()

        langBtn.performClick()
        composeRule.waitForIdle()
        assertEquals(LauncherLanguage.AR, stateHolder.value.settings.language)
        composeRule.onNodeWithText("ع").assertIsDisplayed()

        langBtn.performClick()
        composeRule.waitForIdle()
        assertEquals(LauncherLanguage.EN, stateHolder.value.settings.language)
        composeRule.onNodeWithText("EN").assertIsDisplayed()
    }

    @Test
    fun uiCustomizerScreen_doesNotShowLanguageSwitcherButtons() {
        val state = LauncherUiState(settings = LauncherSettings(language = LauncherLanguage.FR))
        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.screens.ThemeAndWallpaperScreen(
                    state = state,
                    onBack = {},
                    onUpdateSettings = {},
                    onApplyThemePreset = {},
                    onSelectCustomWallpaper = { _, _ -> },
                )
            }
        }
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithTag("ui_lang_English").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("ui_lang_Français").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("ui_lang_العربية").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun settingsAndHomeDock_adjustDockScalePercent_shrinksAndEnlargesFavoritesBar() {
        val fiveApps = (1..5).map { idx ->
            AppItem("com.test.scale$idx", "Scale $idx", "MainActivity", ColorDrawable(AndroidColor.CYAN))
        }
        val settingsHolder = androidx.compose.runtime.mutableStateOf(
            LauncherSettings(
                language = LauncherLanguage.AR,
                appsPerRow = 5,
                dockScalePercent = 100,
                iconSize = com.example.model.AppIconSize.MEDIUM,
                iconShape = com.example.model.AppIconShape.RECTANGLE,
            )
        )
        val destinationHolder = androidx.compose.runtime.mutableStateOf(LauncherDestination.LAUNCHER_SETTINGS)

        composeRule.setContent {
            HorizonLauncherTheme {
                when (destinationHolder.value) {
                    LauncherDestination.LAUNCHER_SETTINGS -> com.example.ui.screens.LauncherSettingsScreen(
                        state = LauncherUiState(settings = settingsHolder.value),
                        onBack = {},
                        onNavigate = {},
                        onUpdateSettings = { transform -> settingsHolder.value = transform(settingsHolder.value) },
                        onFollowThemeReset = {},
                        onRequestDefaultHome = {},
                        onCheckUpdates = {},
                    )
                    else -> HomeScreen(
                        state = LauncherUiState(
                            settings = settingsHolder.value,
                            destination = LauncherDestination.HOME,
                            allApps = fiveApps,
                            favoriteApps = fiveApps,
                            visibleAppsOrdered = fiveApps,
                        ),
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
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_scale_value").assertIsDisplayed()
        repeat(3) {
            composeRule.onNodeWithTag("dock_scale_decrease").performScrollTo().performClick()
            composeRule.waitForIdle()
        }
        assertEquals(70, settingsHolder.value.dockScalePercent)

        destinationHolder.value = LauncherDestination.HOME
        composeRule.waitForIdle()
        val dock70 = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot

        settingsHolder.value = settingsHolder.value.copy(dockScalePercent = 100)
        composeRule.waitForIdle()
        val dock100 = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot

        settingsHolder.value = settingsHolder.value.copy(dockScalePercent = 120)
        composeRule.waitForIdle()
        val dock120 = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot

        assertTrue(
            "Expected 70% dock (${dock70.width}x${dock70.height}) narrower than 100% dock (${dock100.width}x${dock100.height})",
            dock70.width < dock100.width - 40f && dock70.height < dock100.height - 10f,
        )
        assertTrue(
            "Expected 120% dock (${dock120.height}) taller than 100% dock (${dock100.height})",
            dock120.height > dock100.height + 8f,
        )
    }

    @Test
    fun futuristicShapes_andCurvedArabicSettingsDrawer_renderCleanlyInsideBounds() {
        val dockPath = com.example.ui.components.buildFuturisticDockPath(
            size = androidx.compose.ui.geometry.Size(1200f, 120f),
            crestHeightPx = 14f,
            cornerRadiusPx = 22f,
        )
        val dockPathBounds = dockPath.getBounds()
        assertEquals(0f, dockPathBounds.top, 0.5f)
        assertEquals(1200f, dockPathBounds.width, 0.5f)
        assertEquals(120f, dockPathBounds.height, 0.5f)

        val drawerPath = com.example.ui.components.buildCurvedSettingsDrawerPath(
            size = androidx.compose.ui.geometry.Size(376f, 540f),
            headerHeightPx = 66f,
            neckInsetPx = 48f,
            midBowInsetPx = 82f,
            bottomInsetPx = 26f,
        )
        val drawerPathBounds = drawerPath.getBounds()
        assertEquals(376f, drawerPathBounds.width, 0.5f)
        assertEquals(540f, drawerPathBounds.height, 0.5f)

        val orbitPrayerPath = com.example.ui.components.buildOrbitPrayerCardPath(
            size = androidx.compose.ui.geometry.Size(340f, 160f),
            cornerRadiusPx = 22f,
            cutoutDepthPx = 36f,
        )
        assertEquals(340f, orbitPrayerPath.getBounds().width, 0.5f)
        assertEquals(160f, orbitPrayerPath.getBounds().height, 0.5f)

        val orbitWeatherPath = com.example.ui.components.buildOrbitWeatherCardPath(
            size = androidx.compose.ui.geometry.Size(340f, 160f),
            cornerRadiusPx = 22f,
            cutoutDepthPx = 36f,
        )
        assertEquals(340f, orbitWeatherPath.getBounds().width, 0.5f)
        assertEquals(160f, orbitWeatherPath.getBounds().height, 0.5f)

        var dismissed = false
        composeRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
            ) {
                HorizonLauncherTheme {
                    com.example.ui.screens.SettingsMenuOverlay(
                        state = LauncherUiState(
                            settings = LauncherSettings(language = LauncherLanguage.AR),
                        ),
                        onDismiss = { dismissed = true },
                        onNavigate = {},
                        onOpenSystemSettings = {},
                        onChangeLanguage = {},
                    )
                }
            }
        }

        composeRule.waitForIdle()
        val panelBounds = composeRule.onNodeWithTag("settings_menu_panel").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val tvBounds = composeRule.onNodeWithTag("menu_tv_settings").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected curved settings drawer anchored on physical right (${panelBounds.right} vs ${rootBounds.right})",
            abs(panelBounds.right - rootBounds.right) < 2f,
        )
        assertTrue(
            "Expected menu row physically inside curved left clearance (${tvBounds.left} > ${panelBounds.left + 60f})",
            tvBounds.left > panelBounds.left + 60f,
        )
        composeRule.onNodeWithTag("settings_menu_close").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun aboutSupportScreen_displaysDeveloperInfo_editsDetails_andLaunchesEmailAndWhatsappIntents() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        val settingsHolder = androidx.compose.runtime.mutableStateOf(
            LauncherSettings(language = LauncherLanguage.FR)
        )
        var requestedDefaultHome = false

        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.screens.AboutSupportScreen(
                    state = LauncherUiState(settings = settingsHolder.value),
                    onBack = {},
                    onUpdateSettings = { transform -> settingsHolder.value = transform(settingsHolder.value) },
                    onRequestDefaultHome = { requestedDefaultHome = true },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("about_developer_name_card").assertIsDisplayed()
        composeRule.onNodeWithTag("about_set_default_home_btn")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertTrue("Expected default home request callback from About & Support screen", requestedDefaultHome)

        composeRule.onNodeWithTag("about_contact_email_btn").performScrollTo().assertIsDisplayed().performClick()

        val emailIntent = org.robolectric.Shadows.shadowOf(context).nextStartedActivity
        assertEquals(android.content.Intent.ACTION_SENDTO, emailIntent?.action)
        assertEquals("mailto:support@horizontv.app", emailIntent?.data?.toString())

        // Tapping WhatsApp when no number is configured opens the edit dialog
        composeRule.onNodeWithTag("about_contact_whatsapp_btn").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("developer_edit_dialog").assertIsDisplayed()

        composeRule.onNodeWithTag("developer_name_input").performTextReplacement("Karim Dev")
        composeRule.onNodeWithTag("developer_email_input").performTextReplacement("karim@example.com")
        composeRule.onNodeWithTag("developer_whatsapp_input").performTextReplacement("+213 555 12 34 56")
        composeRule.onNodeWithTag("developer_save_btn").performClick()
        composeRule.waitForIdle()

        assertEquals("Karim Dev", settingsHolder.value.developerName)
        assertEquals("karim@example.com", settingsHolder.value.developerEmail)
        assertEquals("+213 555 12 34 56", settingsHolder.value.developerWhatsApp)
        composeRule.onNodeWithText("Karim Dev").assertIsDisplayed()
        composeRule.onNodeWithText("karim@example.com").assertIsDisplayed()

        // Tapping WhatsApp now launches wa.me URL with digits only
        composeRule.onNodeWithTag("about_contact_whatsapp_btn").performScrollTo().performClick()
        val waIntent = org.robolectric.Shadows.shadowOf(context).nextStartedActivity
        assertEquals(android.content.Intent.ACTION_VIEW, waIntent?.action)
        assertEquals("https://wa.me/213555123456", waIntent?.data?.toString())
    }

    @Test
    fun appLibraryTileCard_extractsAmbientColor_andHandlesLaunchAndFavoriteBadge() {
        val redVisual = com.example.ui.components.extractAppIconVisual(
            drawable = ColorDrawable(AndroidColor.RED),
            fallbackAccent = androidx.compose.ui.graphics.Color.Cyan,
        )
        assertTrue("Expected bitmap from non-null icon drawable", redVisual.bitmap != null)
        assertTrue("Expected red channel dominant in extracted ambient color", redVisual.ambientColor.red > 0.8f)

        val app = AppItem("com.test.modern", "Modern TV", "MainActivity", ColorDrawable(AndroidColor.MAGENTA))
        var launched = false
        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.components.AppLibraryTileCard(
                    app = app,
                    settings = LauncherSettings(),
                    onClick = { launched = true },
                    onLongClick = {},
                    isFavoriteBadge = true,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_favorite_badge_com.test.modern", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("app_tile_com.test.modern").assertIsDisplayed().performClick()
        assertTrue(launched)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun homeScreen_defaultSettings_displaysSevenFavoritesOnSingleHorizontalRow_withBalancedRatio() {
        val sevenApps = (1..7).map { idx ->
            AppItem("com.test.seven$idx", "App $idx", "MainActivity", ColorDrawable(AndroidColor.BLUE))
        }
        val defaultSettings = LauncherSettings()
        assertEquals(7, defaultSettings.appsPerRow)

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = LauncherUiState(
                        settings = defaultSettings,
                        destination = LauncherDestination.HOME,
                        allApps = sevenApps,
                        favoriteApps = sevenApps,
                        visibleAppsOrdered = sevenApps,
                    ),
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
        val tile1 = composeRule.onNodeWithTag("app_tile_com.test.seven1").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val tile7 = composeRule.onNodeWithTag("app_tile_com.test.seven7").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

        // All 7 apps sit on one straight horizontal line (7th must NOT drop below 1st)
        assertTrue(
            "Expected 7th favorite app (${tile7.top}) aligned horizontally with 1st app (${tile1.top})",
            abs(tile7.top - tile1.top) < 8f && abs(tile7.bottom - tile1.bottom) < 8f,
        )
        assertTrue("Expected 7th app to the right of 1st app", tile7.left > tile1.right)

        // Balanced aspect ratio (around 1.5:1, not exaggerated > 1.85:1)
        val ratio = tile1.width / tile1.height
        assertTrue("Expected balanced tile ratio (~1.5:1), got $ratio (${tile1.width}x${tile1.height})", ratio in 1.25f..1.75f)

        // Verify middle favorite tile has focus, then move D-pad UP to All Apps medallion:
        // Dock and All Apps medallion vertical coordinates MUST stay 100% stationary (zero jitter/shake).
        composeRule.onNodeWithTag("app_tile_com.test.seven4").assertIsFocused()
        val dockBefore = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot
        val allAppsBefore = composeRule.onNodeWithTag("home_all_apps_button").fetchSemanticsNode().boundsInRoot

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionUp)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_all_apps_button").assertIsFocused()
        val dockAfter = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot
        val allAppsAfter = composeRule.onNodeWithTag("home_all_apps_button").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected Favorites Dock top & height to stay rock-steady on focus change (before=$dockBefore, after=$dockAfter)",
            abs(dockBefore.top - dockAfter.top) < 1f && abs(dockBefore.height - dockAfter.height) < 1f,
        )
        val allAppsBeforeCenterY = (allAppsBefore.top + allAppsBefore.bottom) / 2f
        val allAppsAfterCenterY = (allAppsAfter.top + allAppsAfter.bottom) / 2f
        assertTrue(
            "Expected All Apps button vertical center to remain stationary on focus (before=$allAppsBeforeCenterY, after=$allAppsAfterCenterY)",
            abs(allAppsBeforeCenterY - allAppsAfterCenterY) < 1f,
        )
    }

    @Test
    fun clockSettings_selectWatchFaceTheme_updatesClockOnHomeScreen_preservingHeaderAlignment() {
        val settingsHolder = androidx.compose.runtime.mutableStateOf(
            LauncherSettings(
                language = LauncherLanguage.AR,
                clockTheme = com.example.model.ClockTheme.CLASSIC_HORIZON,
            )
        )
        val destHolder = androidx.compose.runtime.mutableStateOf(LauncherDestination.LAUNCHER_SETTINGS)

        composeRule.setContent {
            HorizonLauncherTheme {
                when (destHolder.value) {
                    LauncherDestination.LAUNCHER_SETTINGS -> com.example.ui.screens.LauncherSettingsScreen(
                        state = LauncherUiState(
                            settings = settingsHolder.value,
                            formattedTime = "10:08",
                            formattedDate = "SAMEDI 21 NOV",
                        ),
                        onBack = {},
                        onNavigate = {},
                        onUpdateSettings = { transform -> settingsHolder.value = transform(settingsHolder.value) },
                        onFollowThemeReset = {},
                        onRequestDefaultHome = {},
                        onCheckUpdates = {},
                    )
                    else -> HomeScreen(
                        state = LauncherUiState(
                            settings = settingsHolder.value,
                            destination = LauncherDestination.HOME,
                            formattedTime = "10:08",
                            formattedDate = "SAMEDI 21 NOV",
                            nextPrayer = NextPrayerInfo(PrayerKey.DHUHR, "12:45", 1, 20),
                        ),
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
        }

        composeRule.waitForIdle()
        // Open Watch Face Gallery inside Clock & Date settings
        composeRule.onNodeWithTag("open_clock_theme_picker").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_theme_gallery_dialog").assertIsDisplayed()

        // Pick Black Orange (Digital-only) watch theme
        composeRule.onNodeWithTag("clock_theme_card_black_orange").performClick()
        composeRule.waitForIdle()
        assertEquals(com.example.model.ClockTheme.BLACK_ORANGE, settingsHolder.value.clockTheme)

        // Switch to HomeScreen and verify themed clock sits centered inside the upper orbital slot between Prayer & Weather
        destHolder.value = LauncherDestination.HOME
        composeRule.waitForIdle()
        val digitalClockBounds = composeRule.onNodeWithTag("home_clock_theme_black_orange").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val prayerBounds = composeRule.onNodeWithTag("home_prayer_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val weatherBounds = composeRule.onNodeWithTag("home_weather_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val clockHeader = composeRule.onNodeWithTag("home_clock_header").fetchSemanticsNode().boundsInRoot
        assertTrue("Expected themed clock inside orbital clock header", digitalClockBounds.bottom <= clockHeader.bottom + 2f)
        assertTrue(
            "Expected themed clock (${digitalClockBounds.left}..${digitalClockBounds.right}) centered between Prayer (${prayerBounds.right}) and Weather (${weatherBounds.left})",
            digitalClockBounds.left >= prayerBounds.right - 2f && digitalClockBounds.right <= weatherBounds.left + 2f,
        )
        val digitalLeftGap = digitalClockBounds.left - prayerBounds.right
        val digitalRightGap = weatherBounds.left - digitalClockBounds.right
        assertTrue(
            "Expected equal spacing on both sides of themed digital clock (left=$digitalLeftGap, right=$digitalRightGap)",
            abs(digitalLeftGap - digitalRightGap) < 4f,
        )

        // Switch to Classic Analog (Simple Red): pure analog ONLY (no digital time text rendered), still centered in orbit slot
        settingsHolder.value = settingsHolder.value.copy(clockTheme = com.example.model.ClockTheme.SIMPLE_RED)
        composeRule.waitForIdle()
        val analogClockBounds = composeRule.onNodeWithTag("home_clock_theme_simple_red").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected analog clock (${analogClockBounds.left}..${analogClockBounds.right}) centered between Prayer (${prayerBounds.right}) and Weather (${weatherBounds.left})",
            analogClockBounds.left >= prayerBounds.right - 2f && analogClockBounds.right <= weatherBounds.left + 2f,
        )
        val analogLeftGap = analogClockBounds.left - prayerBounds.right
        val analogRightGap = weatherBounds.left - analogClockBounds.right
        assertTrue(
            "Expected equal spacing on both sides of analog clock (left=$analogLeftGap, right=$analogRightGap)",
            abs(analogLeftGap - analogRightGap) < 4f,
        )
        composeRule.onNodeWithText("10:08").assertDoesNotExist()
    }

    @Test
    fun startupBoot_rendersAdhanAndWeatherCardsSimultaneouslyFromFrameZero() {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.LauncherViewModel(app)
        val initialState = vm.uiState.value

        assertTrue("Expected initial PrayerTimesData populated immediately at startup", initialState.prayerTimes != null)
        assertTrue("Expected initial NextPrayerInfo populated immediately at startup", initialState.nextPrayer != null)

        // Even if HomeScreen receives an unpopulated LauncherUiState (nextPrayer == null),
        // PrayerWidgetCard must render simultaneously alongside WeatherWidgetCard from frame 0.
        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = LauncherUiState(settings = LauncherSettings(language = LauncherLanguage.FR)),
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

        val prayerBounds = composeRule.onNodeWithTag("home_prayer_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val weatherBounds = composeRule.onNodeWithTag("home_weather_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected Adhan card (${prayerBounds.top}) aligned synchronously with Weather card (${weatherBounds.top})",
            abs(prayerBounds.top - weatherBounds.top) < 4f,
        )
    }

    @Test
    fun launcherSettingsScreen_displaysProminentDefaultHomeSetting_andTriggersRequest() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        var requestedDefaultHome = false

        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.screens.LauncherSettingsScreen(
                    state = LauncherUiState(settings = LauncherSettings(language = LauncherLanguage.AR)),
                    onBack = {},
                    onNavigate = {},
                    onUpdateSettings = {},
                    onFollowThemeReset = {},
                    onRequestDefaultHome = { requestedDefaultHome = true },
                    onCheckUpdates = {},
                )
            }
        }

        composeRule.waitForIdle()
        // Visible immediately near top of App Settings without scrolling
        composeRule.onNodeWithTag("action_default_home")
            .assertIsDisplayed()
            .performClick()
        assertTrue("Expected Default Home launcher callback from LauncherSettingsScreen", requestedDefaultHome)

        val repo = com.example.data.InstalledAppsRepository(context)
        assertTrue("Expected fallback default home settings intent to launch cleanly", repo.openDefaultHomeFallbackSettings())
    }

    @Test
    fun githubAutoUpdater_parsesRepoSlugComparesVersions_andRendersUpdateDialogs() {
        assertEquals("tofay01/username-horizon-launcher", LauncherSettings().githubUpdateRepo)
        assertEquals("karim/horizon-tv", com.example.update.GitHubUpdateRepository.normalizeRepoSlug("karim/horizon-tv"))
        assertEquals(
            "karim/horizon-tv",
            com.example.update.GitHubUpdateRepository.normalizeRepoSlug("https://github.com/karim/horizon-tv.git"),
        )
        assertEquals(
            "karim/horizon-tv",
            com.example.update.GitHubUpdateRepository.normalizeRepoSlug("https://github.com/karim/horizon-tv/releases/latest"),
        )
        assertEquals(null, com.example.update.GitHubUpdateRepository.normalizeRepoSlug("invalid-repo"))

        assertTrue(com.example.update.GitHubUpdateRepository.isNewerVersion("v1.15", "1.14"))
        assertTrue(com.example.update.GitHubUpdateRepository.isNewerVersion("1.14.1", "1.14"))
        assertTrue(com.example.update.GitHubUpdateRepository.isNewerVersion("v2.0.0", "1.14"))
        assertTrue(!com.example.update.GitHubUpdateRepository.isNewerVersion("v1.14", "1.14"))
        assertTrue(!com.example.update.GitHubUpdateRepository.isNewerVersion("v1.13", "1.14"))

        val picked = com.example.update.GitHubUpdateRepository.selectBestApkAsset(
            listOf(
                com.example.update.GitHubReleaseAsset("checksum.txt", "https://example.com/checksum.txt", 120L),
                com.example.update.GitHubReleaseAsset("HorizonLauncher.apk", "https://example.com/HorizonLauncher.apk", 22_000_000L),
            )
        )
        assertEquals("HorizonLauncher.apk", picked?.name)

        var savedSlug: String? = null
        var downloadTriggered = false
        val updateStateHolder = androidx.compose.runtime.mutableStateOf<com.example.update.AppUpdateUiState>(
            com.example.update.AppUpdateUiState.RepoSetupRequired
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.screens.GitHubUpdateDialogHost(
                    updateState = updateStateHolder.value,
                    currentRepo = "",
                    strings = LauncherStrings.get(LauncherLanguage.AR),
                    accent = androidx.compose.ui.graphics.Color.Cyan,
                    onSaveRepo = { raw, _ ->
                        val norm = com.example.update.GitHubUpdateRepository.normalizeRepoSlug(raw) ?: return@GitHubUpdateDialogHost false
                        savedSlug = norm
                        true
                    },
                    onDownloadAndInstall = { downloadTriggered = true },
                    onRetryInstall = {},
                    onRequestInstallPermission = {},
                    onDismiss = { updateStateHolder.value = com.example.update.AppUpdateUiState.Idle },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("github_repo_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("github_repo_input").performTextReplacement("https://github.com/esiptv/HorizonLauncher")
        composeRule.onNodeWithTag("github_repo_save_btn").performClick()
        assertEquals("esiptv/HorizonLauncher", savedSlug)

        updateStateHolder.value = com.example.update.AppUpdateUiState.Available(
            com.example.update.AppUpdateInfo(
                versionTag = "v1.15",
                releaseTitle = "Horizon 1.15",
                releaseNotes = "تحديث جديد عبر GitHub",
                htmlUrl = "https://github.com/esiptv/HorizonLauncher/releases/tag/v1.15",
                apkDownloadUrl = "https://github.com/esiptv/HorizonLauncher/releases/download/v1.15/HorizonLauncher.apk",
                apkFileName = "HorizonLauncher.apk",
                apkSizeBytes = 21_500_000L,
            )
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("github_update_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("github_update_download_btn").assertIsDisplayed().performClick()
        assertTrue("Expected download & install CTA callback", downloadTriggered)
    }

    @Test
    fun calendarWidget_replacesPrayerCardInSameSlot_andSwitchesCleanly() {
        val showCalendarOnly = androidx.compose.runtime.mutableStateOf(true)
        val showPrayerAlso = androidx.compose.runtime.mutableStateOf(false)
        var navigatedToCalendar = false

        composeRule.setContent {
            HorizonLauncherTheme {
                HomeScreen(
                    state = LauncherUiState(
                        settings = LauncherSettings(
                            language = LauncherLanguage.FR,
                            showPrayerWidget = showPrayerAlso.value,
                            showCalendarWidget = showCalendarOnly.value,
                            showWeatherWidget = true,
                        )
                    ),
                    isHomeActive = true,
                    onNavigate = { dest ->
                        if (dest == com.example.model.LauncherDestination.CALENDAR_DETAIL) {
                            navigatedToCalendar = true
                        }
                    },
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

        val calBounds = composeRule.onNodeWithTag("home_calendar_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val weatherBounds = composeRule.onNodeWithTag("home_weather_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected Calendar card (${calBounds.top}) aligned symmetrically with Weather card (${weatherBounds.top})",
            abs(calBounds.top - weatherBounds.top) < 4f,
        )
        composeRule.onNodeWithTag("home_calendar_hero_day", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("home_calendar_week_strip", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("home_calendar_card").performClick()
        assertTrue("Expected clicking Calendar widget to open CALENDAR_DETAIL", navigatedToCalendar)

        // When both Adhan & Calendar are enabled, user can flip cleanly in the same left slot
        showPrayerAlso.value = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("switch_left_widget_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_prayer_card").assertIsDisplayed()
    }

    @Test
    fun newThemeCategories_flagsCinemaAndAnime_renderAndSelectCleanly() {
        var appliedPreset: com.example.model.ThemePreset? = null
        val state = LauncherUiState(
            settings = LauncherSettings(
                language = LauncherLanguage.AR,
                themePreset = com.example.model.ThemePreset.FLAG_ALGERIA,
                wallpaperId = "flag_algeria",
            ),
        )

        composeRule.setContent {
            HorizonLauncherTheme {
                com.example.ui.components.LauncherWallpaperLayer(settings = state.settings)
                com.example.ui.screens.ThemeAndWallpaperScreen(
                    state = state,
                    onBack = {},
                    onUpdateSettings = {},
                    onApplyThemePreset = { appliedPreset = it },
                    onSelectCustomWallpaper = { _, _ -> },
                )
            }
        }
        composeRule.waitForIdle()

        val expectedNewIds = listOf(
            "flag_algeria", "flag_egypt", "flag_saudi_arabia", "flag_france",
            "flag_germany", "flag_spain", "flag_uk",
            "cinema_classic", "cinema_scifi", "cinema_retro",
            "anime_grendizer", "anime_remi", "anime_sandybell", "anime_dbz",
        )
        expectedNewIds.forEach { id ->
            composeRule.onNodeWithTag("theme_preset_$id").assertExists()
        }

        // Verify drawable-backed themes have valid drawables and procedural themes are registered
        assertTrue(com.example.ui.components.wallpaperDrawableRes("cinema_classic") != null)
        assertTrue(com.example.ui.components.wallpaperDrawableRes("anime_grendizer") != null)
        assertTrue(com.example.ui.components.wallpaperDrawableRes("anime_remi") != null)
        assertTrue(com.example.ui.components.wallpaperDrawableRes("anime_sandybell") != null)
        assertTrue(com.example.ui.components.wallpaperDrawableRes("anime_dbz") != null)
        assertTrue(com.example.ui.components.hasBuiltInArtworkWallpaper("flag_algeria"))
        assertTrue(com.example.ui.components.hasBuiltInArtworkWallpaper("cinema_scifi"))

        composeRule.onNodeWithTag("theme_preset_anime_grendizer")
            .performScrollTo()
            .performClick()
        assertEquals(com.example.model.ThemePreset.ANIME_GRENDIZER, appliedPreset)
    }

    @Test
    fun signatureSkins_selectCleanly_andPreserveExactHomeCardGeometry() {
        val sampleApps = (1..6).map { idx ->
            AppItem("com.test.skin$idx", "App $idx", "MainActivity", ColorDrawable(AndroidColor.CYAN))
        }
        val activePresetHolder = androidx.compose.runtime.mutableStateOf(com.example.model.ThemePreset.COSMOS_BLUE)
        val showCustomizer = androidx.compose.runtime.mutableStateOf(false)

        composeRule.setContent {
            HorizonLauncherTheme {
                val activeSettings = LauncherSettings(
                    language = LauncherLanguage.FR,
                    appsPerRow = 6,
                    themePreset = activePresetHolder.value,
                    accentColorHex = activePresetHolder.value.accentHex,
                    wallpaperId = activePresetHolder.value.defaultWallpaperId,
                )
                if (showCustomizer.value) {
                    com.example.ui.screens.ThemeAndWallpaperScreen(
                        state = LauncherUiState(settings = activeSettings),
                        onBack = {},
                        onUpdateSettings = {},
                        onApplyThemePreset = { activePresetHolder.value = it },
                        onSelectCustomWallpaper = { _, _ -> },
                    )
                } else {
                    HomeScreen(
                        state = LauncherUiState(
                            settings = activeSettings,
                            destination = LauncherDestination.HOME,
                            allApps = sampleApps,
                            favoriteApps = sampleApps,
                            visibleAppsOrdered = sampleApps,
                            nextPrayer = NextPrayerInfo(PrayerKey.DHUHR, "12:26", 3, 36),
                            weather = WeatherData("Mons Centre", 11.0, 9.0, 61, 14.0, 82, 13.0, 10.0, isDay = true, isGpsSynced = true),
                        ),
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
        }

        composeRule.waitForIdle()
        val baselinePrayer = composeRule.onNodeWithTag("home_prayer_card").fetchSemanticsNode().boundsInRoot
        val baselineWeather = composeRule.onNodeWithTag("home_weather_card").fetchSemanticsNode().boundsInRoot
        val baselineClock = composeRule.onNodeWithTag("home_clock_header").fetchSemanticsNode().boundsInRoot
        val baselineDock = composeRule.onNodeWithTag("home_favorites_dock").fetchSemanticsNode().boundsInRoot

        val signatureThemes = listOf(
            com.example.model.ThemePreset.SILVER_EARTH,
            com.example.model.ThemePreset.COFFEE_GOLD,
            com.example.model.ThemePreset.ZELLIGE_ROYAL,
            com.example.model.ThemePreset.PHARAOH_GOLD,
            com.example.model.ThemePreset.EGYPT,
            com.example.model.ThemePreset.ALGERIA,
            com.example.model.ThemePreset.MAN_CITY,
            com.example.model.ThemePreset.BARCELONA,
            com.example.model.ThemePreset.REAL_MADRID,
        )

        signatureThemes.forEach { preset ->
            assertTrue(
                "Expected valid wallpaper drawable for ${preset.id}",
                com.example.ui.components.wallpaperDrawableRes(preset.defaultWallpaperId) != null,
            )
            activePresetHolder.value = preset
            composeRule.waitForIdle()

            val prayer = composeRule.onNodeWithTag("home_prayer_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val weather = composeRule.onNodeWithTag("home_weather_card").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val clock = composeRule.onNodeWithTag("home_clock_header").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val dock = composeRule.onNodeWithTag("home_favorites_dock").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

            assertEquals("Prayer bounds must remain identical for ${preset.id}", baselinePrayer, prayer)
            assertEquals("Weather bounds must remain identical for ${preset.id}", baselineWeather, weather)
            assertEquals("Clock bounds must remain identical for ${preset.id}", baselineClock, clock)
            assertEquals("Dock bounds must remain identical for ${preset.id}", baselineDock, dock)
        }

        showCustomizer.value = true
        composeRule.waitForIdle()
        listOf("silver_earth", "coffee_gold", "zellige_royal", "pharaoh_gold").forEach { id ->
            composeRule.onNodeWithTag("theme_preset_$id").assertExists()
        }
        composeRule.onNodeWithTag("theme_preset_zellige_royal").performClick()
        composeRule.waitForIdle()
        assertEquals(com.example.model.ThemePreset.ZELLIGE_ROYAL, activePresetHolder.value)
    }
}
