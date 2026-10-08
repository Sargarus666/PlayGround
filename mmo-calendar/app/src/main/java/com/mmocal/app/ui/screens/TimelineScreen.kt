package com.mmocal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mmocal.app.data.GamesRepository
import com.mmocal.app.ui.components.GameCard
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    onDeleteRelease: (String) -> Unit
) {
    val repo by GamesRepository.state.collectAsState()
    val today = remember { LocalDate.now() }
    val scope = rememberCoroutineScope()
    val upcoming = remember(today, repo) {
        GamesRepository.upcoming(today).filter { it.confirmed }
    }
    val recent = remember(today, repo) { GamesRepository.recent(today) }
    val undated = remember(repo) { GamesRepository.undated() }

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
            item { SectionTitle("Дальше по датам", "${upcoming.size} событий") }
            items(upcoming, key = { "up-${it.id}" }) { event ->
                GameCard(
                    event = event,
                    isFavorite = event.id in favorites,
                    onClick = { onOpenEvent(event.id) },
                    onToggleFavorite = { onToggleFavorite(event.id) },
                    onDelete = if (event.custom) ({ onDeleteRelease(event.id) }) else null
                )
            }

            item { SectionTitle("Недавно вышло", "за последние 45 дней") }
            if (recent.isEmpty()) {
                item {
                    Text(
                        text = "Свежих релизов пока нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(recent, key = { "past-${it.id}" }) { event ->
                    GameCard(
                        event = event,
                        isFavorite = event.id in favorites,
                        onClick = { onOpenEvent(event.id) },
                        onToggleFavorite = { onToggleFavorite(event.id) },
                        onDelete = if (event.custom) ({ onDeleteRelease(event.id) }) else null
                    )
                }
            }

            item { SectionTitle("Целевые окна", "даты ещё не объявлены") }
            items(undated, key = { "win-${it.id}" }) { event ->
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

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
