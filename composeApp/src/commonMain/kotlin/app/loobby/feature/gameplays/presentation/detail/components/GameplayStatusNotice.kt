package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.presentation.common.ScheduleFormatter

/**
 * Encerrada: por quem e quando, ou que encerrou sozinha. Ativa: quando ela vai
 * encerrar sozinha, numa linha discreta.
 */
@Composable
fun GameplayStatusNotice(gameplay: Gameplay, modifier: Modifier = Modifier) {
    if (gameplay.isEnded) {
        val endedAt = gameplay.endedAt?.let(ScheduleFormatter::dayAndTime)
        val text = when {
            gameplay.endedBy != null && endedAt != null -> "Encerrada por ${gameplay.endedBy.shownName} em $endedAt"
            gameplay.endedBy != null -> "Encerrada por ${gameplay.endedBy.shownName}"
            else -> "Encerrada sozinha, 24 h depois do horário marcado"
        }
        NoticeCard(icon = Icons.Outlined.EventBusy, text = "$text. Agora é só para ver.", modifier = modifier)
    } else {
        Text(
            text = "Encerra sozinha em ${ScheduleFormatter.dayAndTime(gameplay.autoEndAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    }
}
