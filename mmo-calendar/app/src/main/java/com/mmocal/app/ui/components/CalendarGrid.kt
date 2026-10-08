package com.mmocal.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmocal.app.data.EventType
import com.mmocal.app.data.GameEvent
import com.mmocal.app.data.formatMonthYear
import java.time.LocalDate

private val dayHeaders = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

@Composable
fun MonthHeader(
    monthDate: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Предыдущий месяц",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedContent(
            targetState = formatMonthYear(monthDate),
            transitionSpec = {
                (slideInHorizontally { it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally { -it / 3 } + fadeOut(tween(160)))
            },
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f),
            label = "monthTitle"
        ) { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Следующий месяц",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarGrid(
    monthDate: LocalDate,
    eventsInMonth: List<GameEvent>,
    selectedDay: Int?,
    today: LocalDate,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val year = monthDate.year
    val month = monthDate.monthValue
    val firstDay = LocalDate.of(year, month, 1)
    val lengthOfMonth = firstDay.lengthOfMonth()
    val offset = (firstDay.dayOfWeek.value + 6) % 7
    val daysInWeek = 7

    val eventsByDay = eventsInMonth.groupBy { it.date!!.dayOfMonth }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dayHeaders.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        val cells = offset + lengthOfMonth
        val rows = (cells + daysInWeek - 1) / daysInWeek
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until daysInWeek) {
                    val dayIndex = row * daysInWeek + col - offset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayIndex in 1..lengthOfMonth) {
                            DayCell(
                                day = dayIndex,
                                isToday = today.year == year && today.monthValue == month && today.dayOfMonth == dayIndex,
                                isSelected = selectedDay == dayIndex,
                                events = eventsByDay[dayIndex].orEmpty(),
                                onClick = { onDayClick(dayIndex) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<GameEvent>,
    onClick: () -> Unit
) {
    val dotColor = when {
        events.isEmpty() -> Color.Transparent
        events.any { it.type == EventType.LAUNCH } -> colorFor(EventType.LAUNCH)
        else -> colorFor(events.first().type)
    }

    val background = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        events.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isToday || isSelected || events.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(width = 5.dp, height = 5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}
