package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.SheetOptionRow
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.presentation.common.explanation
import app.loobby.feature.gameplays.presentation.common.icon
import app.loobby.feature.gameplays.presentation.common.label
import app.loobby.theme.Spacing

/** O primeiro passo da gameplay nova: como o jogo vai ser decidido. */
@Composable
fun ModeStep(onSelect: (GameplayMode) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = "Como vocês vão escolher o jogo?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GameplayMode.entries.forEach { mode ->
            SheetOptionRow(
                icon = mode.icon,
                title = mode.label,
                subtitle = mode.explanation,
                onClick = { onSelect(mode) },
            )
        }
    }
}
