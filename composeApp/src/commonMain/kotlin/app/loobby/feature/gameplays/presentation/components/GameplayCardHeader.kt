package app.loobby.feature.gameplays.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.loobby.feature.gameplays.presentation.common.ScheduleFormatter
import app.loobby.feature.gameplays.presentation.model.GameplayCardModel
import app.loobby.theme.Spacing

/**
 * O topo de todo card de gameplay: a capa, o nome, onde e quando, e as etiquetas
 * de plataforma e modo. [showGroup] some no Início do grupo, onde ele é óbvio.
 */
@Composable
fun GameplayCardHeader(
    model: GameplayCardModel,
    modifier: Modifier = Modifier,
    showGroup: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        GameplayCover(coverUrl = model.coverUrl, mode = model.mode)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            TitleRow(name = model.name, trailing = trailing)
            Text(
                text = metaLine(model, showGroup),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            GameplayTags(platform = model.platform, mode = model.mode)
        }
    }
}

/**
 * O nome com o [trailing] (o status, no Lobby) ao lado. O status fica só nesta
 * linha, para as de baixo usarem a largura toda.
 */
@Composable
private fun TitleRow(name: String, trailing: @Composable (() -> Unit)?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

private fun metaLine(model: GameplayCardModel, showGroup: Boolean): String {
    val place = if (showGroup) model.groupName ?: "Única" else null
    return listOfNotNull(place, ScheduleFormatter.schedule(model.scheduledAt)).joinToString(" · ")
}
