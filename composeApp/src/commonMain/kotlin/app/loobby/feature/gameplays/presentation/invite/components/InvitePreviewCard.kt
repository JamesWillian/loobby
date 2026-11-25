package app.loobby.feature.gameplays.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.feature.gameplays.domain.model.GameplayInvitePreview
import app.loobby.feature.gameplays.presentation.common.ScheduleFormatter
import app.loobby.feature.gameplays.presentation.common.goingLabel
import app.loobby.feature.gameplays.presentation.components.GameplayCover
import app.loobby.feature.gameplays.presentation.components.GameplayTags
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.groupImagePlaceholder
import app.loobby.theme.Radius
import app.loobby.theme.Spacing
import coil3.compose.AsyncImage

/** A prévia de um grupo encontrado pelo convite. */
@Composable
fun GroupInvitePreviewCard(group: GroupDomain, modifier: Modifier = Modifier) {
    PreviewCard(modifier) {
        AsyncImage(
            model = group.imageUrl ?: groupImagePlaceholder(group.name),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        PreviewTexts(title = group.name, subtitle = "Grupo")
    }
}

/** A prévia de uma gameplay única: quem criou, quando, onde e quantos vão. */
@Composable
fun GameplayInvitePreviewCard(preview: GameplayInvitePreview, modifier: Modifier = Modifier) {
    PreviewCard(modifier) {
        GameplayCover(coverUrl = null, mode = preview.mode, size = 56.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            PreviewTexts(
                title = preview.name,
                subtitle = "${ScheduleFormatter.schedule(preview.scheduledAt)} · criada por ${preview.creator.shownName}",
            )
            GameplayTags(platform = preview.platform, mode = preview.mode)
            Text(
                text = when {
                    preview.isEnded -> "Já foi encerrada"
                    else -> goingLabel(preview.goingCount)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PreviewCard(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun PreviewTexts(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
