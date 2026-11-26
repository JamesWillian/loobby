package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.feature.gameplays.domain.model.Voting

/**
 * O resultado da rodada aberta. No empate no 1º lugar, quem criou vê o Desempatar;
 * os outros ficam sabendo que é com quem criou.
 */
@Composable
fun VotingResultBanner(
    voting: Voting,
    isEnded: Boolean,
    canTiebreak: Boolean,
    onTiebreak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val finalists = voting.games.filter(voting::isFinalist).map { it.name }
    if (finalists.isEmpty()) return

    if (voting.result.tie) {
        NoticeCard(
            icon = Icons.Outlined.Balance,
            text = "Empate no 1º lugar: ${finalists.joinNames()}",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = modifier,
        ) {
            when {
                isEnded -> Unit
                canTiebreak -> Button(onClick = onTiebreak) { Text("Desempatar") }
                else -> Text("Quem criou a gameplay pode abrir o desempate.", style = MaterialTheme.typography.bodySmall)
            }
        }
        return
    }

    val text = when {
        isEnded && finalists.size == 1 -> "Escolhido: ${finalists.first()}"
        isEnded -> "Escolhidos: ${finalists.joinNames()}"
        finalists.size == 1 -> "Na frente: ${finalists.first()}"
        else -> "Finalistas: ${finalists.joinNames()}"
    }
    NoticeCard(icon = Icons.Outlined.EmojiEvents, text = text, modifier = modifier)
}

private fun List<String>.joinNames(): String =
    if (size <= 1) joinToString() else dropLast(1).joinToString(", ") + " e " + last()
