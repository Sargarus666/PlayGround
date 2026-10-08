package com.mmocal.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mmocal.app.data.FavoritesStore
import com.mmocal.app.data.GamesRepository
import com.mmocal.app.data.GohaNewsSource
import com.mmocal.app.data.SettingsStore
import com.mmocal.app.data.UserEventsStore
import com.mmocal.app.notifications.Reminders
import com.mmocal.app.ui.screens.CalendarScreen
import com.mmocal.app.ui.screens.DetailScreen
import com.mmocal.app.ui.screens.FavoritesScreen
import com.mmocal.app.ui.screens.NewsArticleScreen
import com.mmocal.app.ui.screens.NewsScreen
import com.mmocal.app.ui.screens.SettingsScreen
import com.mmocal.app.ui.screens.TimelineScreen
import com.mmocal.app.ui.theme.MMOCalendarTheme

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("calendar", "Календарь", Icons.Filled.CalendarMonth),
    Tab("timeline", "Список", Icons.Filled.FormatListBulleted),
    Tab("news", "Новости", Icons.Filled.Newspaper),
    Tab("favorites", "Избранное", Icons.Filled.Favorite),
    Tab("settings", "Настройки", Icons.Filled.Settings)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val favoritesStore = FavoritesStore(applicationContext)
        val settingsStore = SettingsStore(applicationContext)
        val userEventsStore = UserEventsStore(applicationContext)
        setContent {
            val themeMode by settingsStore.themeMode.collectAsState()
            val style by settingsStore.style.collectAsState()
            val colorIndex by settingsStore.colorIndex.collectAsState()
            MMOCalendarTheme(
                themeMode = themeMode,
                style = style,
                accentIndex = colorIndex
            ) {
                App(favoritesStore, settingsStore, userEventsStore)
            }
        }
    }
}

@Composable
private fun App(
    favoritesStore: FavoritesStore,
    settingsStore: SettingsStore,
    userEventsStore: UserEventsStore
) {
    val navController = rememberNavController()
    val favorites by favoritesStore.favorites.collectAsState()
    val notifications by settingsStore.notifications.collectAsState()
    val autoUpdate by settingsStore.autoUpdate.collectAsState()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in tabs.map { it.route }
    val context = androidx.compose.ui.platform.LocalContext.current

    val userEvents by userEventsStore.events.collectAsState()
    LaunchedEffect(userEvents) {
        GamesRepository.setUserEvents(userEvents)
    }

    LaunchedEffect(autoUpdate) {
        if (autoUpdate) GamesRepository.refresh()
    }

    LaunchedEffect(notifications) {
        if (notifications) Reminders.schedule(context, favoritesStore.favorites.value)
    }

    fun onAccountChanged() {
        settingsStore.reload()
        favoritesStore.reload()
        userEventsStore.reload()
        val enabled = settingsStore.notifications.value
        if (enabled) Reminders.schedule(context, favoritesStore.favorites.value)
    }

    fun deleteRelease(id: String) {
        userEventsStore.remove(id)
    }

    fun toggleFavorite(id: String) {
        favoritesStore.toggle(id)
        if (notifications) {
            Reminders.schedule(context, favoritesStore.favorites.value)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = {
                                Text(
                                    text = tab.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "calendar",
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(260)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(260)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(260)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(260)
                    )
                }
            ) {
                composable("calendar") {
                    CalendarScreen(
                        favorites = favorites,
                        onToggleFavorite = { toggleFavorite(it) },
                        onOpenEvent = { navController.navigate("event/$it") },
                        onAddRelease = { title, date, type -> userEventsStore.add(title, date, type) },
                        onDeleteRelease = { deleteRelease(it) }
                    )
                }
                composable("timeline") {
                    TimelineScreen(
                        favorites = favorites,
                        onToggleFavorite = { toggleFavorite(it) },
                        onOpenEvent = { navController.navigate("event/$it") },
                        onDeleteRelease = { deleteRelease(it) }
                    )
                }
                composable("news") {
                    NewsScreen(
                        onOpenArticle = { item ->
                            navController.navigate("newsview/" + Uri.encode(item.url))
                        }
                    )
                }
                composable("newsview/{u}") { entry ->
                    val u = Uri.decode(entry.arguments?.getString("u").orEmpty())
                    val item = GohaNewsSource.news.value.find { it.url == u }
                    if (item != null && u.isNotBlank()) {
                        NewsArticleScreen(
                            url = u,
                            title = item.title,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        LaunchedEffect(u) {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)))
                            }
                            navController.popBackStack()
                        }
                    }
                }
                composable("favorites") {
                    FavoritesScreen(
                        favorites = favorites,
                        onToggleFavorite = { toggleFavorite(it) },
                        onOpenEvent = { navController.navigate("event/$it") },
                        onDeleteRelease = { deleteRelease(it) }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        settingsStore = settingsStore,
                        onNotificationsToggled = { enabled ->
                            if (enabled) {
                                Reminders.schedule(context, favoritesStore.favorites.value)
                            } else {
                                Reminders.cancel(context)
                            }
                        },
                        onAccountChanged = { onAccountChanged() }
                    )
                }
                composable("event/{id}") { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    var isFav by remember(id, favorites) { mutableStateOf(id in favorites) }
                    isFav = id in favorites
                    DetailScreen(
                        eventId = id,
                        isFavorite = isFav,
                        onToggleFavorite = { toggleFavorite(id) },
                        onBack = { navController.popBackStack() },
                        onDeleteEvent = {
                            deleteRelease(id)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
