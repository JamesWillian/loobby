package app.loobby.feature.gameplays.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.theme.FourPlayersColors

/** A cor de cada resposta: Vou verde, Não vou vermelho, Pendente neutro. */
@Composable
fun ParticipationStatus.color(): Color = when (this) {
    ParticipationStatus.GOING -> FourPlayersColors.ConfirmedLight
    ParticipationStatus.NOT_GOING -> FourPlayersColors.Declined
    ParticipationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
}
