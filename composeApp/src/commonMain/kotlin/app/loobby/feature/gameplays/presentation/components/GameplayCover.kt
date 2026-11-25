package app.loobby.feature.gameplays.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.presentation.common.icon
import app.loobby.theme.Radius
import coil3.compose.AsyncImage

/** A imagem de uma gameplay: a capa do jogo, ou o ícone do modo quando não há capa. */
@Composable
fun GameplayCover(
    coverUrl: String?,
    mode: GameplayMode,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(Radius.md))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            Icon(
                imageVector = mode.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(size / 4),
            )
        }
    }
}
