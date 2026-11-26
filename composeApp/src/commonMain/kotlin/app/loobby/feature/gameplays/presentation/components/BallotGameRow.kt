package app.loobby.feature.gameplays.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.AvatarStack
import app.loobby.feature.games.presentation.GameCoverImage
import app.loobby.feature.gameplays.presentation.model.BallotEntry
import app.loobby.theme.Radius
import app.loobby.theme.Spacing

/**
 * Um jogo da cédula: capa, nome, votos e o meu voto. Com [onVote], tocar na linha
 * vota ou tira o voto; com [onRemove], aparece o × de tirar o jogo. [showVoters]
 * mostra quem votou, na tela da gameplay; [below] vem debaixo disso (o match).
 */
@Composable
fun BallotGameRow(
    entry: BallotEntry,
    modifier: Modifier = Modifier,
    onVote: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    showVoters: Boolean = false,
    below: @Composable (() -> Unit)? = null,
) {
    val highlight = entry.isMyVote
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(
                if (highlight) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            )
            .then(if (onVote != null) Modifier.clickable(onClick = onVote) else Modifier)
            .padding(start = Spacing.sm, end = if (onRemove != null) Spacing.xxs else Spacing.md)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        GameCoverImage(url = entry.coverUrl, size = 36.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (entry.isFinalist) FontWeight.Bold else FontWeight.Medium,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showVoters && entry.votes > 0) {
                AvatarStack(avatarUrls = entry.voterAvatars, total = entry.votes, size = 20.dp)
            }
            below?.invoke()
        }
        VoteCount(votes = entry.votes)
        VoteMark(isMyVote = entry.isMyVote, canVote = onVote != null)
        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Close, contentDescription = "Tirar ${entry.name} da votação", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun VoteCount(votes: Int) {
    Text(
        text = if (votes == 1) "1 voto" else "$votes votos",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun VoteMark(isMyVote: Boolean, canVote: Boolean) {
    when {
        isMyVote -> Icon(
            Icons.Filled.CheckCircle,
            contentDescription = "Seu voto",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        canVote -> Icon(
            Icons.Outlined.RadioButtonUnchecked,
            contentDescription = "Votar",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(22.dp),
        )
    }
}
