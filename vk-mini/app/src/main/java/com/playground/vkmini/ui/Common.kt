package com.playground.vkmini.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Avatar(url: String?, size: Dp, name: String = "") {
    if (url.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.trim().take(1).uppercase().ifBlank { "?" },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Medium,
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}

@Composable
fun CompactIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = LocalContentColor.current,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun DemoBadge() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = "демо",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    demo: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (demo) {
                Spacer(Modifier.size(7.dp))
                DemoBadge()
            }
            Spacer(Modifier.weight(1f))
            actions()
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}

@Composable
fun InlineError(
    text: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)) {
            Text(
                text = "Не получилось",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (onRetry != null) {
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                ) {
                    Text("Повторить", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp,
        )
    }
}

fun relativeTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val diff = (System.currentTimeMillis() / 1000 - timestamp).coerceAtLeast(0)
    return when {
        diff < 60 -> "сейчас"
        diff < 3600 -> "${diff / 60} мин"
        diff < 86_400 -> "${diff / 3600} ч"
        diff < 172_800 -> "вчера"
        else -> SimpleDateFormat("d MMM", Locale("ru")).format(Date(timestamp * 1000))
    }
}

fun clockTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp * 1000))

fun formatDuration(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "%d:%02d".format(safe / 60, safe % 60)
}
