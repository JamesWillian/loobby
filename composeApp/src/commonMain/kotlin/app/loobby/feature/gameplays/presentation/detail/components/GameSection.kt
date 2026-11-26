package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.SectionTitle
import app.loobby.feature.games.presentation.GameCoverImage
import app.loobby.feature.gameplays.domain.model.GameInfo
import app.loobby.feature.gameplays.domain.model.GameplayUser
import app.loobby.theme.Spacing

/**
 * Jogo definido: o jogo, com a capa, e quem vai e o tem na biblioteca ([owners], só
 * no jogo do catálogo). Livre ([game] null): só o aviso.
 */
@Composable
fun GameSection(
    game: GameInfo?,
    modifier: Modifier = Modifier,
    owners: List<GameplayUser> = emptyList(),
    goingCount: Int = 0,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle(text = "Jogo")
        if (game == null) {
            Text(
                text = "Jogo livre: decidam na hora.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                GameCoverImage(url = game.coverUrl, size = 56.dp)
                Text(text = game.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            if (game.catalogId != null && goingCount > 0) {
                GameOwnersRow(owners = owners)
            }
        }
    }
}
