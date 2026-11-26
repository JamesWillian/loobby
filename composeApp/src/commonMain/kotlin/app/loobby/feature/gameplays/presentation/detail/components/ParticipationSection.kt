package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.SectionTitle
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.ParticipationCounts
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.presentation.common.goingLabel
import app.loobby.feature.gameplays.presentation.components.GoingLine
import app.loobby.feature.gameplays.presentation.components.ParticipationButtons
import app.loobby.theme.Spacing

/**
 * Vou / Não vou / Pendente e o resumo de quem vai. Não há a lista de resposta de
 * cada pessoa: só as contagens e os avatares de quem vai.
 */
@Composable
fun ParticipationSection(
    gameplay: Gameplay,
    isBusy: Boolean,
    onRespond: (ParticipationStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle(text = "Participação")
        if (gameplay.permissions.respond) {
            ParticipationButtons(
                selected = gameplay.myStatus,
                onSelect = onRespond,
                options = ParticipationStatus.entries,
                enabled = !isBusy,
            )
        }
        Text(
            text = countsLine(gameplay.counts),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GoingLine(goingCount = gameplay.counts.going, avatars = gameplay.goingAvatars)
    }
}

private fun countsLine(counts: ParticipationCounts): String = listOf(
    goingLabel(counts.going),
    if (counts.notGoing == 1) "1 não vai" else "${counts.notGoing} não vão",
    "${counts.pending} sem resposta",
).joinToString(" · ")
