package app.loobby.feature.gameplays.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.presentation.common.label
import app.loobby.theme.Radius
import app.loobby.theme.Spacing

/**
 * Botões de resposta lado a lado. No card do grupo são só Vou e Não vou; na tela
 * da gameplay entra também o Pendente. Tocar na resposta marcada não faz nada.
 */
@Composable
fun ParticipationButtons(
    selected: ParticipationStatus,
    onSelect: (ParticipationStatus) -> Unit,
    modifier: Modifier = Modifier,
    options: List<ParticipationStatus> = listOf(ParticipationStatus.GOING, ParticipationStatus.NOT_GOING),
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        options.forEach { status ->
            ParticipationButton(
                status = status,
                isSelected = status == selected,
                enabled = enabled,
                onClick = { if (status != selected) onSelect(status) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ParticipationButton(
    status: ParticipationStatus,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = status.color()
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier.height(40.dp),
    ) {
        Text(
            text = status.label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}
