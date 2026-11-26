package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import app.loobby.core.ui.components.AvatarStack
import app.loobby.feature.gameplays.domain.model.GameplayUser
import app.loobby.theme.Spacing

/** O match do jogo definido: os avatares e os nomes de quem vai e tem o jogo. */
@Composable
fun GameOwnersRow(owners: List<GameplayUser>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (owners.isNotEmpty()) {
            AvatarStack(avatarUrls = owners.map { it.avatarUrl }, total = owners.size)
        }
        Text(
            text = if (owners.isEmpty()) "Ninguém que vai tem o jogo na biblioteca ainda." else ownersNamesLabel(owners),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
