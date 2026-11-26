package app.loobby.feature.gameplays.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.InfoChip
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.gameplays.presentation.common.icon
import app.loobby.feature.gameplays.presentation.common.label
import app.loobby.theme.Spacing

/**
 * As etiquetas de plataforma e modo. Quando não cabem lado a lado, a segunda desce
 * para a linha de baixo, em vez de o texto ser espremido letra por letra.
 */
@Composable
fun GameplayTags(platform: Platform?, mode: GameplayMode, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        platform?.let { InfoChip(label = it.label, icon = it.icon) }
        InfoChip(label = mode.label, icon = mode.icon)
    }
}
