package com.example

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.RadioBrowserStationDto
import com.example.data.RadioRepository
import com.example.model.LauncherDestination
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.RadioPlaybackStatus
import com.example.model.RadioStation
import com.example.ui.LauncherUiState
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.HorizonLauncherTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h720dp-land")
class RadioFeatureTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun radioRepository_filtersHttpsPlayableStations_andRejectsCleartextOrBroken() {
        val raw = listOf(
            RadioBrowserStationDto(
                stationUuid = "u1",
                name = "France Inter",
                url = "http://direct.franceinter.fr/live/franceinter-midfi.mp3",
                urlResolved = "https://icecast.radiofrance.fr/franceinter-midfi.mp3",
                country = "France",
                countryCode = "fr",
                codec = "mp3",
                bitrate = 128,
                votes = 500,
                lastCheckOk = 1,
            ),
            RadioBrowserStationDto(
                stationUuid = "u2",
                name = "Cleartext Only Radio",
                url = "http://stream.example.com/live.mp3",
                urlResolved = "http://stream.example.com/live.mp3",
                country = "Algeria",
                countryCode = "DZ",
                lastCheckOk = 1,
            ),
            RadioBrowserStationDto(
                stationUuid = "u3",
                name = "Broken Radio",
                urlResolved = "https://broken.example.com/live.mp3",
                lastCheckOk = 0,
            ),
            RadioBrowserStationDto(
                stationUuid = "u4",
                name = "Playlist Wrapper",
                urlResolved = "https://stream.example.com/listen.pls",
                lastCheckOk = 1,
            ),
            RadioBrowserStationDto(
                stationUuid = "u5",
                name = "HLS Live Station",
                urlResolved = "https://stream.example.com/radio/master.m3u8?token=abc",
                country = "Algeria",
                countryCode = "dz",
                codec = "AAC",
                bitrate = 96,
                lastCheckOk = 1,
            ),
            RadioBrowserStationDto(
                stationUuid = "u1",
                name = "Duplicate UUID",
                urlResolved = "https://other.example.com/live.mp3",
                lastCheckOk = 1,
            ),
        )

        val filtered = RadioRepository.filterAndMapStations(raw)
        assertEquals(2, filtered.size)
        assertEquals("u1", filtered[0].stationUuid)
        assertEquals("FR", filtered[0].countryCode)
        assertEquals("MP3", filtered[0].codec)
        assertEquals("u5", filtered[1].stationUuid)
        assertEquals("DZ", filtered[1].countryCode)
    }

    @Test
    fun radioRepository_resolvesCountryFromCityAndTimezone() = runBlocking {
        val repo = RadioRepository(ApplicationProvider.getApplicationContext())

        assertEquals(
            "DZ",
            repo.resolveCountryCode(
                LauncherSettings(cityName = "Alger, Algérie", timezoneId = "Africa/Algiers")
            ),
        )
        assertEquals(
            "EG",
            repo.resolveCountryCode(
                LauncherSettings(cityName = "القاهرة، مصر", timezoneId = "Africa/Cairo")
            ),
        )
        assertEquals(
            "MA",
            repo.resolveCountryCode(
                LauncherSettings(cityName = "Casablanca", timezoneId = "Africa/Casablanca")
            ),
        )
        assertEquals(
            "FR",
            repo.resolveCountryCode(
                LauncherSettings(cityName = "Lyon, France", timezoneId = "Europe/Paris")
            ),
        )
    }

    @Test
    fun homeScreen_topBarRadioButton_opensDialog_selectsStation_andShowsQuickStopToggle() {
        val stations = listOf(
            RadioStation(
                stationUuid = "dz-chaabi",
                name = "Radio El Bahdja",
                streamUrl = "https://stream.example.dz/bahdja.mp3",
                country = "Algeria",
                countryCode = "DZ",
                state = "Alger",
                codec = "MP3",
                bitrate = 128,
            ),
            RadioStation(
                stationUuid = "dz-ch1",
                name = "Chaine 1",
                streamUrl = "https://stream.example.dz/ch1.mp3",
                country = "Algeria",
                countryCode = "DZ",
                codec = "AAC",
                bitrate = 96,
            ),
        )
        var openRadioCalls = 0
        var toggleCalls = 0
        var lastPlayed: RadioStation? = null

        val stateHolder = mutableStateOf(
            LauncherUiState(
                settings = LauncherSettings(
                    language = LauncherLanguage.AR,
                    shortCityName = "الجزائر",
                    cityName = "Alger, Algérie",
                    timezoneId = "Africa/Algiers",
                ),
                destination = LauncherDestination.HOME,
                radioStations = stations,
                radioDetectedCountryCode = "DZ",
                radioFilterCountryCode = "DZ",
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
                    onOpenRadio = { openRadioCalls++ },
                    onPlayRadioStation = { station ->
                        lastPlayed = station
                        stateHolder.value = stateHolder.value.copy(
                            radioStatus = RadioPlaybackStatus.PLAYING,
                            activeRadioStation = station,
                        )
                    },
                    onToggleRadioPlayPause = {
                        toggleCalls++
                        val nextStatus = if (stateHolder.value.radioStatus == RadioPlaybackStatus.PLAYING) {
                            RadioPlaybackStatus.IDLE
                        } else {
                            RadioPlaybackStatus.PLAYING
                        }
                        stateHolder.value = stateHolder.value.copy(radioStatus = nextStatus)
                    },
                    onToggleFavoriteRadioStation = { uuid ->
                        val favs = stateHolder.value.settings.favoriteRadioStationIds.toMutableSet()
                        if (favs.contains(uuid)) favs.remove(uuid) else favs.add(uuid)
                        stateHolder.value = stateHolder.value.copy(
                            settings = stateHolder.value.settings.copy(favoriteRadioStationIds = favs)
                        )
                    },
                )
            }
        }

        composeRule.waitForIdle()
        val barBounds = composeRule.onNodeWithTag("home_ai_tools_bar").fetchSemanticsNode().boundsInRoot
        val radioBtnBounds = composeRule.onNodeWithTag("top_radio_button").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected top_radio_button inside top AI tools bar",
            radioBtnBounds.left >= barBounds.left - 1f && radioBtnBounds.right <= barBounds.right + 1f,
        )
        assertTrue(composeRule.onAllNodesWithTag("top_radio_play_toggle").fetchSemanticsNodes().isEmpty())

        // Click Radio button in top AI bar -> opens dialog
        composeRule.onNodeWithTag("top_radio_button").performClick()
        composeRule.waitForIdle()
        assertEquals(1, openRadioCalls)
        composeRule.onNodeWithTag("radio_stations_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("radio_station_item_dz-chaabi").assertIsDisplayed()

        // Favorite toggle + Play station
        composeRule.onNodeWithTag("radio_fav_btn_dz-chaabi").performClick()
        composeRule.waitForIdle()
        assertTrue(stateHolder.value.settings.favoriteRadioStationIds.contains("dz-chaabi"))

        composeRule.onNodeWithTag("radio_station_item_dz-chaabi").performClick()
        composeRule.waitForIdle()
        assertEquals("dz-chaabi", lastPlayed?.stationUuid)
        assertEquals(RadioPlaybackStatus.PLAYING, stateHolder.value.radioStatus)

        // Close dialog and check top bar shows active station name and quick stop toggle
        composeRule.onNodeWithTag("radio_close_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Radio El Bahdja").assertIsDisplayed()
        composeRule.onNodeWithTag("top_radio_play_toggle").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertEquals(1, toggleCalls)
        assertEquals(RadioPlaybackStatus.IDLE, stateHolder.value.radioStatus)
    }
}
