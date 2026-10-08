package com.mmocal.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mmocal.app.data.GameEvent
import kotlin.math.abs

private fun hashColor(seed: String): Color {
    var hash = 0
    for (ch in seed) hash = 31 * hash + ch.code
    val hue = (abs(hash) % 360).toFloat()
    return Color.hsv(hue, 0.55f, 0.75f)
}

@Composable
fun GameIcon(
    event: GameEvent,
    size: Dp = 46.dp,
    cornerRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
    val base = hashColor(event.title)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(
                    Brush.linearGradient(listOf(base, base.copy(alpha = 0.65f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = event.title.trim().first().uppercaseChar().toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / 2.2f).sp
            )
        }
        when {
            event.iconUrl != null -> AsyncImage(
                model = event.iconUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
            event.iconRes != null -> Image(
                painter = painterResource(id = event.iconRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        }
    }
}
