package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.SectionTitle
import app.loobby.feature.gameplays.domain.model.GameplayPermissions
import app.loobby.feature.gameplays.domain.model.GameplayRules
import app.loobby.feature.gameplays.domain.model.Voting
import app.loobby.feature.gameplays.presentation.components.BallotGameRow
import app.loobby.feature.gameplays.presentation.model.BallotEntry
import app.loobby.feature.gameplays.presentation.model.ballot
import app.loobby.feature.gameplays.presentation.model.frozenRounds
import app.loobby.theme.Spacing

/**
 * A votação inteira: a configuração, a cédula da rodada aberta (com quem votou), o
 * botão de adicionar jogo, o resultado com o Desempatar e as rodadas que congelaram.
 */
@Composable
fun VotingSection(
    voting: Voting,
    permissions: GameplayPermissions,
    isEnded: Boolean,
    isBusy: Boolean,
    onVote: (votingGameId: String) -> Unit,
    onRemoveGame: (BallotEntry) -> Unit,
    onAddGame: () -> Unit,
    onTiebreak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = voting.ballot()
    val canEditGames = permissions.editGames && !voting.isTiebreak && !isBusy
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle(
            text = if (voting.isTiebreak) "Desempate" else "Votação",
            detail = configLine(voting),
            trailing = if (voting.locked) {
                { Icon(Icons.Outlined.Lock, contentDescription = "Configuração travada", modifier = Modifier.size(18.dp)) }
            } else null,
        )
        if (entries.isEmpty()) {
            Text(
                text = "Nenhum jogo na votação ainda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        entries.forEach { entry ->
            BallotGameRow(
                entry = entry,
                showVoters = true,
                onVote = if (permissions.vote && !isBusy) ({ onVote(entry.votingGameId) }) else null,
                onRemove = if (canEditGames) ({ onRemoveGame(entry) }) else null,
            )
        }
        if (canEditGames && voting.games.size < GameplayRules.MAX_GAMES) {
            OutlinedButton(onClick = onAddGame, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Adicionar jogo")
            }
        }
        VotingResultBanner(
            voting = voting,
            isEnded = isEnded,
            canTiebreak = permissions.tiebreak && !isBusy,
            onTiebreak = onTiebreak,
        )
        FrozenRoundsSection(rounds = voting.frozenRounds())
    }
}

private fun configLine(voting: Voting): String {
    val perPerson = if (voting.allowsMultipleVotes) "vários votos por pessoa" else "1 voto por pessoa"
    if (voting.isTiebreak) return "Rodada ${voting.round} · $perPerson"
    val finalists = if (voting.finalists == 1) "1 finalista" else "${voting.finalists} finalistas"
    return "$finalists · $perPerson"
}
