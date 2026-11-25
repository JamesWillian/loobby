package app.loobby.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage

/** Foto redonda de uma pessoa. Sem foto, mostra o avatar padrão. */
@Composable
fun UserAvatar(
    url: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    ringColor: Color? = null,
) {
    val base = modifier
        .size(size)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = url ?: userAvatarPlaceholder(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = if (ringColor != null) base.border(2.dp, ringColor, CircleShape) else base,
    )
}
