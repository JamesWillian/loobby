package app.loobby.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Avatares sobrepostos, com "+N" quando há mais gente do que [maxShown]. [total]
 * é o número real de pessoas; a lista pode trazer só as primeiras.
 */
@Composable
fun AvatarStack(
    avatarUrls: List<String?>,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    maxShown: Int = 5,
) {
    val shown = avatarUrls.take(maxShown)
    val extra = total - shown.size
    val ring = MaterialTheme.colorScheme.surface
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-(size / 3)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        shown.forEach { url -> UserAvatar(url = url, size = size, ringColor = ring) }
        if (extra > 0) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, ring, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$extra",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
