package com.mmocalendar.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmocalendar.app.data.GamesRepository
import com.mmocalendar.app.data.MmoGame
import com.mmocalendar.app.data.MmoStatus
import com.mmocalendar.app.ui.theme.MMOTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MMOTheme {
                MMOApp()
            }
        }
    }
}

private const val PREFS = "mmo_favs"

private fun loadFavs(ctx: Context): Set<String> =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet("favs", emptySet()) ?: emptySet()

private fun saveFavs(ctx: Context, favs: Set<String>) {
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet("favs", favs).apply()
}

enum class Tab(val title: String, val icon: ImageVector) {
    CALENDAR("Календарь", Icons.Filled.CalendarMonth),
    CATALOG("Все игры", Icons.Filled.VideogameAsset),
    FAVS("Хочу играть", Icons.Filled.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MMOApp() {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(Tab.CALENDAR) }
    var favs by remember { mutableStateOf(loadFavs(ctx)) }
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<MmoStatus?>(null) }
    var platformFilter by remember { mutableStateOf<String?>(null) }
    var selected: MmoGame? by remember { mutableStateOf(null) }

    fun toggleFav(id: String) {
        favs = if (favs.contains(id)) favs - id else favs + id
        saveFavs(ctx, favs)
    }

    val today = remember { LocalDate.now() }
    val nextRelease = remember {
        GamesRepository.games
            .filter { it.sortDate != null && (it.status == MmoStatus.SOON || it.status == MmoStatus.TARGET) }
            .minByOrNull { it.sortDate!! }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MMO Календарь AAA", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            "Релизы MMORPG • 2025–2028",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0E1A))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF141927)) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        label = { Text(t.title, fontSize = 11.sp) },
                        icon = { Icon(t.icon, contentDescription = t.title) }
                    )
                }
            }
        },
        containerColor = Color(0xFF0B0E1A)
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // Баннер ближайшего релиза
            if (tab == Tab.CALENDAR && nextRelease != null) {
                val diff = GamesRepository.daysUntil(nextRelease, today)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFF06B6D4))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selected = nextRelease }
                        .padding(16.dp)
                ) {
                    Column {
                        Text("🔥 Ближайший AAA-релиз", fontSize = 12.sp, color = Color.White.copy(0.85f))
                        Text(nextRelease.title, fontWeight = FontWeight.Black, fontSize = 19.sp, color = Color.White)
                        Text(
                            "${nextRelease.dateLabel} • " + if (diff != null && diff >= 0) "осталось $diff дн." else nextRelease.status.label,
                            color = Color.White.copy(0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Поиск + фильтры (каталог и календарь)
            if (tab != Tab.FAVS) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Поиск: Aion, Chrono, WoW…") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("Все") }
                    )
                    MmoStatus.entries.forEach { s ->
                        FilterChip(
                            selected = statusFilter == s,
                            onClick = { statusFilter = if (statusFilter == s) null else s },
                            label = { Text(s.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C3AED)
                            )
                        )
                    }
                }
                FlowRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PC", "PS5", "Xbox", "Mobile").forEach { p ->
                        FilterChip(
                            selected = platformFilter == p,
                            onClick = { platformFilter = if (platformFilter == p) null else p },
                            label = { Text(p) }
                        )
                    }
                }
            }

            val base = GamesRepository.games.sortedWith(
                compareBy({ it.sortDate == null }, { it.sortDate })
            )
            fun filtered(list: List<MmoGame>): List<MmoGame> {
                var r = list
                if (tab == Tab.FAVS) r = r.filter { favs.contains(it.id) }
                if (query.isNotBlank()) {
                    val q = query.lowercase()
                    r = r.filter {
                        it.title.lowercase().contains(q) || it.developer.lowercase()
                            .contains(q) || it.genre.lowercase().contains(q)
                    }
                }
                statusFilter?.let { s -> r = r.filter { it.status == s } }
                platformFilter?.let { p -> r = r.filter { it.platforms.contains(p) } }
                return r
            }

            val list = filtered(base)

            if (tab == Tab.FAVS && favs.isEmpty()) {
                EmptyState("Нажми ♡ на игре, чтобы собрать свой вишлист релизов")
            } else if (list.isEmpty()) {
                EmptyState("Ничего не найдено. Попробуй снять фильтры.")
            } else {
                if (tab == Tab.CALENDAR) {
                    Text(
                        "Хронология релизов — от свежих к будущим (${list.size})",
                        Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                } else {
                    Text(
                        "Найдено игр: ${list.size}",
                        Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { g ->
                        GameCard(
                            game = g,
                            isFav = favs.contains(g.id),
                            onFav = { toggleFav(g.id) },
                            onOpen = { selected = g }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }

        // Детали игры
        if (selected != null) {
            GameSheet(
                game = selected!!,
                isFav = favs.contains(selected!!.id),
                onFav = { toggleFav(selected!!.id) },
                onClose = { selected = null }
            )
        }
    }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Color(0xFF9CA3AF), fontSize = 15.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameCard(game: MmoGame, isFav: Boolean, onFav: () -> Unit, onOpen: () -> Unit) {
    val today = remember { LocalDate.now() }
    val countdown = GamesRepository.countdownLabel(game, today)
    val statusColor = when (game.status) {
        MmoStatus.RELEASED -> Color(0xFF22C55E)
        MmoStatus.EARLY_ACCESS -> Color(0xFFF59E0B)
        MmoStatus.SOON -> Color(0xFF8B5CF6)
        MmoStatus.TARGET -> Color(0xFF38BDF8)
        MmoStatus.TBA -> Color(0xFF6B7280)
        MmoStatus.CANCELLED -> Color(0xFFEF4444)
    }
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141927)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(game.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${game.developer} • ${game.genre}",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                IconButton(onClick = onFav) {
                    Icon(
                        if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "В избранное",
                        tint = if (isFav) Color(0xFFF43F5E) else Color(0xFF9CA3AF)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("📅 ${game.dateLabel}", fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1D2440))
                )
                AssistChip(
                    onClick = {},
                    label = { Text(countdown, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = statusColor.copy(alpha = 0.22f))
                )
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, label = { Text(game.status.label, fontSize = 11.sp) })
                AssistChip(onClick = {}, label = { Text(game.monetization, fontSize = 11.sp) })
                game.platforms.forEach { p ->
                    AssistChip(onClick = {}, label = { Text(p, fontSize = 11.sp) })
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i < game.hype) Color(0xFFFBBF24) else Color(0xFF374151),
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("Подробнее →", color = Color(0xFF22D3EE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameSheet(game: MmoGame, isFav: Boolean, onFav: () -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Color(0xFF141927)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(game.title, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("${game.developer} • изд. ${game.publisher}", color = Color(0xFF9CA3AF), fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, label = { Text("📅 ${game.dateLabel}") })
                AssistChip(onClick = {}, label = { Text(game.status.label) })
                AssistChip(onClick = {}, label = { Text(game.monetization) })
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                game.platforms.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
            Spacer(Modifier.height(12.dp))
            Text("Об игре", fontWeight = FontWeight.Bold, color = Color(0xFF22D3EE))
            Spacer(Modifier.height(4.dp))
            Text(game.description, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(12.dp))
            Text("Жанр: ${game.genre}", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("Сайт: ${game.site}", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Хайп: ", fontWeight = FontWeight.Bold)
                repeat(5) { i ->
                    Icon(
                        Icons.Filled.Star, null,
                        tint = if (i < game.hype) Color(0xFFFBBF24) else Color(0xFF374151)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(
                Modifier.fillMaxWidth().clickable(onClick = onFav),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFav) Color(0xFFF43F5E) else Color(0xFF7C3AED)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (isFav) "♥ В вишлисте — убрать" else "♡ Хочу играть — в вишлист",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
