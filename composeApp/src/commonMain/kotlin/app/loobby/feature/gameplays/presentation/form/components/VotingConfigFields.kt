package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.loobby.feature.gameplays.domain.model.GameplayRules
import app.loobby.theme.Spacing

/**
 * Quantos finalistas e quantos votos por pessoa. Os dois travam no primeiro voto:
 * aí [enabled] é false e o formulário só mostra.
 */
@Composable
fun VotingConfigFields(
    finalists: Int,
    multiVote: Boolean,
    enabled: Boolean,
    onFinalistsChange: (Int) -> Unit,
    onMultiVoteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Finalistas", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "Os mais votados que vão para o resultado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(onClick = { onFinalistsChange(finalists - 1) }, enabled = enabled && finalists > 1) {
                Icon(Icons.Outlined.Remove, contentDescription = "Menos finalistas")
            }
            Text(text = finalists.toString(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            FilledTonalIconButton(
                onClick = { onFinalistsChange(finalists + 1) },
                enabled = enabled && finalists < GameplayRules.MAX_FINALISTS,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Mais finalistas")
            }
        }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(false to "1 voto por pessoa", true to "Vários votos").forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = multiVote == value,
                    onClick = { onMultiVoteChange(value) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                ) { Text(label) }
            }
        }
        if (!enabled) {
            Text(
                text = "A votação já tem voto: finalistas e votos por pessoa não mudam mais.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
