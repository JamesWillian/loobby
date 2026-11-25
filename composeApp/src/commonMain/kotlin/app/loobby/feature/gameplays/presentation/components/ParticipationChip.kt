package app.loobby.feature.gameplays.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.InfoChip
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.presentation.common.label

/** A resposta da pessoa, como etiqueta: Vou, Não vou ou Pendente. */
@Composable
fun ParticipationChip(status: ParticipationStatus, modifier: Modifier = Modifier) {
    val color = status.color()
    InfoChip(
        label = status.label,
        containerColor = color.copy(alpha = 0.14f),
        contentColor = color,
        modifier = modifier,
    )
}
