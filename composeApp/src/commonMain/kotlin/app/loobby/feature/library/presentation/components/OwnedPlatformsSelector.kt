package app.loobby.feature.library.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.gameplays.presentation.common.icon
import app.loobby.feature.gameplays.presentation.common.label
import app.loobby.theme.Spacing

/** O "tenho o jogo": as plataformas de [choices], marcadas as de [selected]. Toque marca ou desmarca. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnedPlatformsSelector(
    choices: List<Platform>,
    selected: Set<Platform>,
    enabled: Boolean,
    onToggle: (Platform) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        choices.forEach { platform ->
            FilterChip(
                selected = platform in selected,
                onClick = { onToggle(platform) },
                enabled = enabled,
                label = { Text(platform.label) },
                leadingIcon = { Icon(platform.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )
        }
    }
}
