package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.feature.games.presentation.GameCoverImage
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.GameplayRules
import app.loobby.theme.Spacing

/** Os jogos que já entram na votação da gameplay nova. Dá para criar sem nenhum. */
@Composable
fun VotingGamesEditor(
    games: List<GameChoice>,
    onAdd: () -> Unit,
    onRemove: (GameChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = "Jogos na votação", style = MaterialTheme.typography.labelLarge)
        if (games.isEmpty()) {
            Text(
                text = "Adicione agora ou depois, na gameplay. Qualquer um do grupo também pode adicionar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        games.forEach { game ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                GameCoverImage(url = game.coverUrl, size = 36.dp)
                Text(text = game.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemove(game) }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Tirar ${game.name}", modifier = Modifier.size(18.dp))
                }
            }
        }
        if (games.size < GameplayRules.MAX_GAMES) {
            OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Adicionar jogo")
            }
        }
    }
}
