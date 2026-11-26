package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.gameplays.presentation.common.icon
import app.loobby.feature.gameplays.presentation.common.label
import app.loobby.theme.Spacing

/** A plataforma da gameplay. Uma só, por enquanto. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlatformSelector(selected: Platform?, onSelect: (Platform) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(text = "Plataforma *", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Platform.entries.forEach { platform ->
                FilterChip(
                    selected = platform == selected,
                    onClick = { onSelect(platform) },
                    label = { Text(platform.label) },
                    leadingIcon = { Icon(platform.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }
    }
}
