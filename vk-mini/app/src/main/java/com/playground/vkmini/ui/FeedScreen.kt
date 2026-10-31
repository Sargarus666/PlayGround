package com.playground.vkmini.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.playground.vkmini.AppViewModel
import com.playground.vkmini.data.FeedPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.ensureFeed() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Лента",
            subtitle = if (vm.demo) "тестовые данные" else "только посты, без рекламы",
            demo = vm.demo,
            actions = {
                CompactIconButton(
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Обновить",
                    onClick = { vm.loadFeed(true) },
                )
            },
        )

        PullToRefreshBox(
            isRefreshing = vm.feedRefreshing,
            onRefresh = { vm.loadFeed(true) },
            modifier = Modifier.weight(1f),
        ) {
            when {
                vm.feedError != null && vm.feed.isEmpty() -> Column(Modifier.padding(12.dp)) {
                    InlineError(text = vm.feedError!!, onRetry = { vm.loadFeed() })
                }

                vm.feed.isEmpty() && vm.feedLoading -> LoadingState()

                vm.feed.isEmpty() -> EmptyState(
                    icon = MiniIcons.Comment,
                    title = "Лента пуста",
                    text = "Постов нет или у токена нет доступа к newsfeed.",
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(vm.feed, key = { it.id }) { post ->
                        PostCard(post)
                    }
                    if (vm.feedLoading) {
                        item { LoadingState() }
                    }
                    if (vm.feedHasMore && !vm.feedLoading) {
                        item {
                            TextButton(
                                onClick = { vm.loadFeed() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                            ) {
                                Text("Показать ещё", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: FeedPost) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(post.avatar, 30.dp, post.author)
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                ) {
                    Text(
                        text = post.author,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = relativeTime(post.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            post.image?.let { image ->
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(190.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Metric(MiniIcons.Heart, post.likes)
                Metric(MiniIcons.Comment, post.comments)
                Metric(MiniIcons.Repeat, post.reposts)
            }
        }
    }
}

@Composable
private fun Metric(icon: ImageVector, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
