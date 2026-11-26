package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.presentation.common.ScheduleFormatter
import app.loobby.feature.gameplays.presentation.components.GameplayCover
import app.loobby.feature.gameplays.presentation.components.GameplayTags
import app.loobby.feature.gameplays.presentation.model.toCardModel
import app.loobby.theme.Spacing

/** O topo da gameplay: capa, nome, onde, quando, plataforma, modo e quem criou. */
@Composable
fun GameplayHero(gameplay: Gameplay, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            GameplayCover(coverUrl = gameplay.toCardModel().coverUrl, mode = gameplay.mode, size = 72.dp)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = gameplay.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = gameplay.groupName ?: "Gameplay única",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GameplayTags(platform = gameplay.platform, mode = gameplay.mode)
            }
        }
        HeroLine(icon = Icons.Outlined.Schedule, text = ScheduleFormatter.schedule(gameplay.scheduledAt))
        HeroLine(icon = Icons.Outlined.Person, text = "Criada por ${gameplay.creator.shownName}")
        gameplay.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun HeroLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
