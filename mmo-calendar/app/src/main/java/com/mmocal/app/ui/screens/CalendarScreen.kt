package com.mmocal.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mmocal.app.data.EventType
import com.mmocal.app.data.GameEvent
import com.mmocal.app.data.GamesRepository
import com.mmocal.app.data.formatDate
import com.mmocal.app.data.formatMonthYear
import com.mmocal.app.ui.components.CalendarGrid
import com.mmocal.app.ui.components.GameCard
import com.mmocal.app.ui.components.MonthHeader
import com.mmocal.app.ui.components.colorFor
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private data class FilterOption(
    val label: String,
    val types: Set<EventType>?,
    val aaaOnly: Boolean = false
)

private val filterOptions = listOf(
    FilterOption("Все", null),
    FilterOption("Релизы", setOf(EventType.LAUNCH)),
    FilterOption("Ранний доступ", setOf(EventType.EARLY_ACCESS)),
    FilterOption("ЗБТ / ОБТ", setOf(EventType.BETA)),
    FilterOption("AAA", null, aaaOnly = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    onAddRelease: (String, LocalDate, EventType) -> Unit,
    onDeleteRelease: (String) -> Unit
) {
    val repo by GamesRepository.state.collectAsState()
    val today = remember { LocalDate.now() }
    val scope = rememberCoroutineScope()
    var monthDate by remember { mutableStateOf(today.withDayOfMonth(1)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var filterIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val monthEvents = remember(monthDate, repo) {
        GamesRepository.inMonth(monthDate.year, monthDate.monthValue)
    }
    val filter = filterOptions[filterIndex]
    val filteredMonth = remember(monthEvents, filter) {
        var list = if (filter.types == null) monthEvents else monthEvents.filter { it.type in filter.types }
        if (filter.aaaOnly) list = list.filter { it.aaa }
        list
    }
    val dayEvents = remember(filteredMonth, selectedDay) {
        if (selectedDay == null) filteredMonth
        else filteredMonth.filter { it.date?.dayOfMonth == selectedDay }
    }
    val nextLaunch = remember(today, repo) { GamesRepository.nextBigLaunch(today) }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = repo.loading,
            onRefresh = { scope.launch { GamesRepository.refresh() } },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    HeroCountdownCard(
                        today = today,
                        event = nextLaunch,
                        onClick = { nextLaunch?.let { onOpenEvent(it.id) } }
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                            MonthHeader(
                                monthDate = monthDate,
                                onPrev = {
                                    monthDate = monthDate.minusMonths(1)
                                    selectedDay = null
                                },
                                onNext = {
                                    monthDate = monthDate.plusMonths(1)
                                    selectedDay = null
                                }
                            )
                            Spacer(Modifier.height(4.dp))
                            CalendarGrid(
                                monthDate = monthDate,
                                eventsInMonth = monthEvents,
                                selectedDay = selectedDay,
                                today = today,
                                onDayClick = { day ->
                                    selectedDay = if (selectedDay == day) null else day
                                }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val selected = selectedDay
                        Text(
                            text = if (selected != null) {
                                formatDate(monthDate.withDayOfMonth(selected))
                            } else formatMonthYear(monthDate),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${dayEvents.size} событ.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filterOptions.forEachIndexed { index, option ->
                            FilterChip(
                                selected = filterIndex == index,
                                onClick = { filterIndex = index },
                                label = { Text(option.label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                if (dayEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "В этот день событий нет",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(dayEvents, key = { it.id }) { event ->
                        GameCard(
                            event = event,
                            isFavorite = event.id in favorites,
                            onClick = { onOpenEvent(event.id) },
                            onToggleFavorite = { onToggleFavorite(event.id) },
                            onDelete = if (event.custom) ({ onDeleteRelease(event.id) }) else null
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить свой релиз")
        }
    }

    if (showAddDialog) {
        AddReleaseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, date, type ->
                showAddDialog = false
                onAddRelease(title, date, type)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReleaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, EventType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var typeIndex by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickedMillis by remember {
        mutableLongStateOf(LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    }
    val date = remember(pickedMillis) {
        Instant.ofEpochMilli(pickedMillis).atZone(ZoneOffset.UTC).toLocalDate()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить свой релиз") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventType.entries.forEachIndexed { index, type ->
                        FilterChip(
                            selected = typeIndex == index,
                            onClick = { typeIndex = index },
                            label = { Text(type.label) }
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Дата",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDate(date),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    TextButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Изменить")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, date, EventType.entries[typeIndex]) },
                enabled = title.isNotBlank()
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = pickedMillis,
            initialDisplayedMonthMillis = pickedMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { pickedMillis = it }
                    showDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = state, modifier = Modifier.heightIn(max = 420.dp))
        }
    }
}

@Composable
private fun HeroCountdownCard(
    today: LocalDate,
    event: GameEvent?,
    onClick: () -> Unit
) {
    if (event == null || event.date == null) return
    val days = ChronoUnit.DAYS.between(today, event.date)
    val accent = colorFor(event.type)
    val scheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            scheme.primary.copy(alpha = 0.85f),
                            scheme.primary.copy(alpha = 0.55f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "БЛИЖАЙШИЙ РЕЛИЗ",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimary.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(event.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (days <= 0) "СЕГОДНЯ" else "$days",
                        style = MaterialTheme.typography.displaySmall,
                        color = scheme.onPrimary
                    )
                    if (days > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (days == 1L) "день до запуска" else "дней до запуска",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onPrimary
                )
                Spacer(Modifier.height(2.dp))
                AnimatedVisibility(visible = event.note != null, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = event.note.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
