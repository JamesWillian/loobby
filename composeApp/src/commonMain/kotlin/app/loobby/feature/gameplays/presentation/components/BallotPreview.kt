package app.loobby.feature.gameplays.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.feature.gameplays.domain.model.Voting
import app.loobby.feature.gameplays.presentation.common.gamesLabel
import app.loobby.feature.gameplays.presentation.model.topOfBallot
import app.loobby.theme.Spacing

/**
 * A votação dentro do card do grupo: os mais votados, com o voto num toque, e o
 * link para a gameplay quando há mais jogos do que cabem.
 */
@Composable
fun BallotPreview(
    voting: Voting,
    canVote: Boolean,
    onVote: (votingGameId: String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    maxRows: Int = 5,
) {
    val candidates = voting.candidates
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (candidates.isEmpty()) {
            Text(
                text = "Nenhum jogo na votação ainda. Abra a gameplay para adicionar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        voting.topOfBallot(maxRows).forEach { entry ->
            BallotGameRow(
                entry = entry,
                onVote = if (canVote) ({ onVote(entry.votingGameId) }) else null,
            )
        }
        if (candidates.size > maxRows) {
            TextButton(onClick = onSeeAll) { Text("Ver os ${gamesLabel(candidates.size)}") }
        }
    }
}
