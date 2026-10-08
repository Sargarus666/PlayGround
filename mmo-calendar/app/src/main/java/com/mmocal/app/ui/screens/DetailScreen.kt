package com.mmocal.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mmocal.app.data.GameEvent
import com.mmocal.app.data.GamesRepository
import com.mmocal.app.data.Platform
import com.mmocal.app.data.formatDate
import com.mmocal.app.ui.components.GameIcon
import com.mmocal.app.ui.components.TypeBadge
import com.mmocal.app.ui.components.colorFor
import coil.compose.AsyncImage
import com.mmocal.app.ui.components.shareEvent
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime

@Composable
fun DetailScreen(
    eventId: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onDeleteEvent: (() -> Unit)? = null
) {
    val repo by GamesRepository.state.collectAsState()
    val event = remember(eventId, repo) { GamesRepository.byId(eventId) } ?: return
    val context = LocalContext.current
    val accent = colorFor(event.type)
    val scheme = MaterialTheme.colorScheme

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.id, event.date) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.32f), scheme.surface, scheme.background)
                    )
                )
                .padding(bottom = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = scheme.onSurface
                        )
                    }
                    Row {
                        if (event.custom && onDeleteEvent != null) {
                            IconButton(onClick = onDeleteEvent) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Удалить событие",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(onClick = { shareEvent(context, event) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Поделиться",
                                tint = scheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Избранное",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary
                                else scheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GameIcon(event = event, size = 68.dp, cornerRadius = 18.dp)
                        Column {
                            TypeBadge(event.type)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = scheme.onSurface
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = event.developer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    CountdownBlock(
                        event = event,
                        nowMillis = now,
                        accent = accent
                    )
                }
            }
        }

        if (event.screenshots.isNotEmpty()) {
            ScreenshotsGallery(screenshots = event.screenshots)
        }
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))
            InfoGrid(event)
            Spacer(Modifier.height(20.dp))
            Text("О игре", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant
            )
            if (event.note != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                        Text(
                            text = event.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = accent
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CountdownBlock(event: GameEvent, nowMillis: Long, accent: Color) {
    val scheme = MaterialTheme.colorScheme
    val date = event.date
    if (date == null) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (scheme.background.luminanceCompat() < 0.5f)
                    Color.White.copy(alpha = 0.08f)
                else Color.Black.copy(alpha = 0.06f)
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "ОЖИДАЕМОЕ ОКНО",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = event.windowText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = accent,
                    fontWeight = FontWeight.ExtraBold
                )
                if (!event.confirmed) {
                    Text(
                        text = "Дата не подтверждена издателем",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val target = date.atTime(LocalTime.of(18, 0)).atZone(java.time.ZoneId.systemDefault())
    val remaining = Duration.between(java.time.Instant.ofEpochMilli(nowMillis), target.toInstant())
    val days = remaining.toDays()
    val hours = remaining.minusDays(days).toHours()
    val minutes = remaining.minusHours(hours).toMinutes()
    val seconds = remaining.minusMinutes(minutes).seconds

    val pulse = rememberInfiniteTransition(label = "pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alpha"
    )

    if (remaining.isNegative) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "УЖЕ ВЫШЛО",
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.onSurface
        )
        val daysAgo = -days
        Text(
            text = if (daysAgo == 0L) "Сегодня" else "Прошло $daysAgo дн. назад",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    } else {
        Text(
            text = if (days == 0L) "ОСТАЛОСЬ МЕНЬШЕ СУТОК" else "ДО РЕЛИЗА",
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = alpha),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TimeUnit(value = days, label = if (days % 10 == 1L && days % 100 != 11L) "день" else "дней")
            if (hours > 0 || days < 7) {
                TimeUnit(value = hours, label = "час")
                TimeUnit(value = minutes, label = "мин")
                TimeUnit(value = seconds, label = "сек")
            } else {
                TimeUnit(value = hours, label = "час")
                TimeUnit(value = minutes, label = "мин")
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

private fun Color.luminanceCompat(): Float =
    (0.299f * red + 0.587f * green + 0.114f * blue)

@Composable
private fun TimeUnit(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoGrid(event: GameEvent) {
    val rows = buildList {
        add("Дата" to event.windowText)
        add("Статус" to if (event.confirmed) "Подтверждено" else "Цель / слух")
        if (event.genre.isNotBlank()) add("Жанр" to event.genre)
        if (event.model != com.mmocal.app.data.AccessModel.UNKNOWN) add("Модель" to event.model.label)
        if (event.publisher.isNotBlank()) add("Издатель" to event.publisher)
        val plats = event.platforms.joinToString(", ") { it.label }
        if (plats.isNotBlank()) add("Платформы" to plats)
        if (event.region.isNotBlank()) add("Регион" to event.region)
        if (event.platforms.contains(Platform.MOBILE)) add("Мобильные" to "Android, iOS")
        if (event.rating100 != null) add("Рейтинг" to "★ ${event.rating100} / 100")
        if (event.aaa) add("Класс" to "AAA-проект")
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
                if (index != rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotsGallery(screenshots: List<String>) {
    val shots = screenshots.take(3)
    if (shots.isEmpty()) return
    var opened by remember { mutableStateOf<Int?>(null) }
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
        Text(
            text = "Скриншоты",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            shots.forEachIndexed { index, url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Скриншот ${index + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { opened = index }
                )
            }
        }
    }
    val start = opened
    if (start != null) {
        FullscreenShots(shots = shots, startIndex = start, onClose = { opened = null })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullscreenShots(shots: List<String>, startIndex: Int, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val pager = rememberPagerState(initialPage = startIndex) { shots.size }
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = shots[page],
                    contentDescription = "Скриншот ${page + 1} из ${shots.size}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pager.currentPage + 1} / ${shots.size}",
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
