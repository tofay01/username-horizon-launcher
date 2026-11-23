package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherLanguage
import com.example.model.LauncherSettings
import com.example.model.LauncherStrings
import com.example.ui.LauncherUiState
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CalendarWeekDayItem(
    val shortLabel: String,
    val dayOfMonth: Int,
    val isToday: Boolean,
)

data class DailyCalendarSnapshot(
    val dayOfMonth: Int,
    val weekdayName: String,
    val monthYearLabel: String,
    val weekOfYear: Int,
    val dayOfYear: Int,
    val totalDaysInYear: Int,
    val remainingDaysInYear: Int,
    val weekDays: List<CalendarWeekDayItem>,
)

object HorizonCalendarHelper {

    fun localeFor(language: LauncherLanguage): Locale = when (language) {
        LauncherLanguage.FR -> Locale.FRENCH
        LauncherLanguage.AR -> Locale("ar")
        LauncherLanguage.EN -> Locale.ENGLISH
    }

    fun computeDailySnapshot(
        nowMillis: Long,
        language: LauncherLanguage,
        timezoneId: String,
    ): DailyCalendarSnapshot {
        val locale = localeFor(language)
        val tz = runCatching { TimeZone.getTimeZone(timezoneId) }.getOrElse { TimeZone.getDefault() }
        val cal = Calendar.getInstance(tz, locale).apply {
            firstDayOfWeek = Calendar.MONDAY
            timeInMillis = nowMillis
        }

        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
        val remainingDays = (totalDays - dayOfYear).coerceAtLeast(0)

        val weekdayName = runCatching {
            SimpleDateFormat("EEEE", locale).apply { timeZone = tz }.format(Date(nowMillis))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }.getOrDefault("")

        val monthYearLabel = runCatching {
            SimpleDateFormat("MMMM yyyy", locale).apply { timeZone = tz }.format(Date(nowMillis))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }.getOrDefault("")

        val weekCursor = (cal.clone() as Calendar).apply {
            val dow = get(Calendar.DAY_OF_WEEK)
            val diffFromMonday = (dow - Calendar.MONDAY + 7) % 7
            add(Calendar.DAY_OF_MONTH, -diffFromMonday)
        }

        val dayFormatter = SimpleDateFormat("EEEEE", locale).apply { timeZone = tz }
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)

        val weekDays = (0 until 7).map {
            val shortLabel = when (language) {
                LauncherLanguage.AR -> when (weekCursor.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "ن"
                    Calendar.TUESDAY -> "ث"
                    Calendar.WEDNESDAY -> "ر"
                    Calendar.THURSDAY -> "خ"
                    Calendar.FRIDAY -> "ج"
                    Calendar.SATURDAY -> "س"
                    else -> "ح"
                }
                else -> runCatching { dayFormatter.format(weekCursor.time).uppercase(locale).take(2) }.getOrDefault("·")
            }
            val item = CalendarWeekDayItem(
                shortLabel = shortLabel,
                dayOfMonth = weekCursor.get(Calendar.DAY_OF_MONTH),
                isToday = weekCursor.get(Calendar.YEAR) == todayYear &&
                    weekCursor.get(Calendar.DAY_OF_YEAR) == todayDay,
            )
            weekCursor.add(Calendar.DAY_OF_MONTH, 1)
            item
        }

        return DailyCalendarSnapshot(
            dayOfMonth = dayOfMonth,
            weekdayName = weekdayName,
            monthYearLabel = monthYearLabel,
            weekOfYear = weekOfYear,
            dayOfYear = dayOfYear,
            totalDaysInYear = totalDays,
            remainingDaysInYear = remainingDays,
            weekDays = weekDays,
        )
    }

    fun weekdayHeaders(language: LauncherLanguage): List<String> = when (language) {
        LauncherLanguage.FR -> listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        LauncherLanguage.AR -> listOf("إث", "ثلا", "أرب", "خمي", "جمع", "سبت", "أحد")
        LauncherLanguage.EN -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
}

@Composable
fun CalendarDetailScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
) {
    BackHandler { onBack() }

    val settings = state.settings
    val strings = LauncherStrings.get(settings.language)
    val accent = settings.accentColor
    val surfaceColor = Color(settings.themePreset.surfaceHex)

    val nowMillis = remember { System.currentTimeMillis() }
    val todaySnapshot = remember(nowMillis, settings.language, settings.timezoneId) {
        HorizonCalendarHelper.computeDailySnapshot(
            nowMillis = nowMillis,
            language = settings.language,
            timezoneId = settings.timezoneId,
        )
    }

    val tz = remember(settings.timezoneId) {
        runCatching { TimeZone.getTimeZone(settings.timezoneId) }.getOrElse { TimeZone.getDefault() }
    }
    val initialCal = remember(nowMillis, tz) {
        Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
    }
    val todayYear = initialCal.get(Calendar.YEAR)
    val todayMonth = initialCal.get(Calendar.MONTH)
    val todayDay = initialCal.get(Calendar.DAY_OF_MONTH)

    var displayYear by remember { mutableIntStateOf(todayYear) }
    var displayMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedDay by remember { mutableIntStateOf(todayDay) }

    val locale = HorizonCalendarHelper.localeFor(settings.language)
    val displayedMonthTitle = remember(displayYear, displayMonth, settings.language) {
        val cal = Calendar.getInstance(tz, locale).apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        SimpleDateFormat("MMMM yyyy", locale).apply { timeZone = tz }.format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    val selectedFullDateLabel = remember(displayYear, displayMonth, selectedDay, settings.language) {
        val cal = Calendar.getInstance(tz, locale).apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            val maxD = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, selectedDay.coerceIn(1, maxD))
        }
        SimpleDateFormat("EEEE d MMMM yyyy", locale).apply { timeZone = tz }.format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    val monthCells = remember(displayYear, displayMonth) {
        val firstDayCal = Calendar.getInstance(tz).apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDow = firstDayCal.get(Calendar.DAY_OF_WEEK)
        val leadingEmpty = (firstDow - Calendar.MONDAY + 7) % 7
        val totalSlots = ((leadingEmpty + daysInMonth + 6) / 7) * 7
        (0 until totalSlots).map { idx ->
            val dayNum = idx - leadingEmpty + 1
            if (dayNum in 1..daysInMonth) dayNum else null
        }
    }

    val backFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { backFocus.requestFocus() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xE8071638), Color(0xF5030B1D))))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 1040.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            val isWide = maxWidth >= 720.dp
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TvFocusableSurface(
                            onClick = onBack,
                            accentColor = accent,
                            shape = RoundedCornerShape(12.dp),
                            baseColor = Color.White.copy(alpha = 0.08f),
                            focusRequester = backFocus,
                            modifier = Modifier.size(48.dp).testTag("calendar_back_btn"),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = strings.backLabel,
                                    tint = PureWhite,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.calendarScreenTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite,
                        )
                    }

                    TvFocusableSurface(
                        onClick = {
                            displayYear = todayYear
                            displayMonth = todayMonth
                            selectedDay = todayDay
                        },
                        accentColor = accent,
                        shape = RoundedCornerShape(12.dp),
                        baseColor = accent.copy(alpha = 0.20f),
                        modifier = Modifier.testTag("calendar_today_btn"),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Rounded.DateRange, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            Text(
                                text = strings.calendarTodayBtn,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite,
                            )
                        }
                    }
                }

                val dailyHeroCard: @Composable (Modifier) -> Unit = { mod ->
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = surfaceColor.copy(alpha = 0.72f),
                        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.48f)),
                        modifier = mod,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(20.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = accent.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.6f)),
                            ) {
                                Text(
                                    text = "${strings.calendarWeekPrefix} ${todaySnapshot.weekOfYear}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                )
                            }

                            Text(
                                text = todaySnapshot.dayOfMonth.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                                color = PureWhite,
                            )

                            Text(
                                text = todaySnapshot.weekdayName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = accent,
                            )

                            Text(
                                text = todaySnapshot.monthYearLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MutedSlate,
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.14f))
                            )

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = strings.calendarDayOfYearLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate,
                                    )
                                    Text(
                                        text = "${todaySnapshot.dayOfYear} / ${todaySnapshot.totalDaysInYear}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PureWhite,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = strings.calendarDaysLeftLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate,
                                    )
                                    Text(
                                        text = "${todaySnapshot.remainingDaysInYear}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = accent,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            TvFocusableSurface(
                                onClick = {
                                    onUpdateSettings {
                                        it.copy(showPrayerWidget = false, showCalendarWidget = true)
                                    }
                                },
                                accentColor = accent,
                                shape = RoundedCornerShape(12.dp),
                                baseColor = Color.White.copy(alpha = 0.08f),
                                isSelected = !settings.showPrayerWidget && settings.showCalendarWidget,
                                modifier = Modifier.fillMaxWidth().testTag("calendar_set_home_primary_btn"),
                            ) {
                                Text(
                                    text = if (!settings.showPrayerWidget && settings.showCalendarWidget) {
                                        "✓ ${strings.leftWidgetModeCalendar}"
                                    } else {
                                        strings.calendarUseAsHomeCardBtn
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = PureWhite,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }

                val monthlyGridCard: @Composable (Modifier) -> Unit = { mod ->
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = surfaceColor.copy(alpha = 0.62f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                        modifier = mod,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(18.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                TvFocusableSurface(
                                    onClick = {
                                        if (displayMonth == Calendar.JANUARY) {
                                            displayYear -= 1
                                            displayMonth = Calendar.DECEMBER
                                        } else {
                                            displayMonth -= 1
                                        }
                                        selectedDay = 1
                                    },
                                    accentColor = accent,
                                    shape = RoundedCornerShape(10.dp),
                                    baseColor = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.size(44.dp).testTag("calendar_prev_month_btn"),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                            contentDescription = "Previous month",
                                            tint = PureWhite,
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = displayedMonthTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = PureWhite,
                                    )
                                    Text(
                                        text = selectedFullDateLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                TvFocusableSurface(
                                    onClick = {
                                        if (displayMonth == Calendar.DECEMBER) {
                                            displayYear += 1
                                            displayMonth = Calendar.JANUARY
                                        } else {
                                            displayMonth += 1
                                        }
                                        selectedDay = 1
                                    },
                                    accentColor = accent,
                                    shape = RoundedCornerShape(10.dp),
                                    baseColor = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.size(44.dp).testTag("calendar_next_month_btn"),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = "Next month",
                                            tint = PureWhite,
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                HorizonCalendarHelper.weekdayHeaders(settings.language).forEach { header ->
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MutedSlate,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            val rows = monthCells.chunked(7)
                            rows.forEach { weekRow ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    weekRow.forEach { dayVal ->
                                        if (dayVal == null) {
                                            Spacer(modifier = Modifier.weight(1f).height(42.dp))
                                        } else {
                                            val isToday = displayYear == todayYear &&
                                                displayMonth == todayMonth &&
                                                dayVal == todayDay
                                            val isPicked = dayVal == selectedDay
                                            TvFocusableSurface(
                                                onClick = { selectedDay = dayVal },
                                                accentColor = accent,
                                                shape = RoundedCornerShape(10.dp),
                                                baseColor = if (isToday) accent.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.06f),
                                                isSelected = isToday || isPicked,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(42.dp)
                                                    .testTag("calendar_day_$dayVal"),
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize(),
                                                ) {
                                                    Text(
                                                        text = dayVal.toString(),
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = if (isToday || isPicked) FontWeight.Bold else FontWeight.Medium,
                                                        ),
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

                if (isWide) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        dailyHeroCard(Modifier.weight(0.38f))
                        monthlyGridCard(Modifier.weight(0.62f))
                    }
                } else {
                    dailyHeroCard(Modifier.fillMaxWidth())
                    monthlyGridCard(Modifier.fillMaxWidth())
                }
            }
        }
    }
}
