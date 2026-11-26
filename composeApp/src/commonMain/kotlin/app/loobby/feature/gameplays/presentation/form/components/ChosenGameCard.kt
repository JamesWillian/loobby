package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.feature.games.presentation.GameCoverImage
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.theme.Spacing

/** O jogo definido da gameplay, com o botão de escolher ou trocar. */
@Composable
fun ChosenGameCard(game: GameChoice?, onChoose: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            GameCoverImage(url = game?.coverUrl, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Jogo *",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = game?.name ?: "Nenhum jogo escolhido",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                )
            }
            TextButton(onClick = onChoose) { Text(if (game == null) "Escolher" else "Trocar") }
        }
    }
}
