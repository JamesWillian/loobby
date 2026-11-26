package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.UserAvatar
import app.loobby.feature.gameplays.domain.model.GameplayComment
import app.loobby.feature.gameplays.presentation.common.ScheduleFormatter
import app.loobby.theme.Spacing

/**
 * Um comentário: foto, nome, quando e o texto. O menu traz Apagar ([onDelete], para
 * o autor e a moderação) e Denunciar ([onReport], só nos dos outros).
 */
@Composable
fun CommentItem(
    comment: GameplayComment,
    onDelete: (() -> Unit)?,
    onReport: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        UserAvatar(url = comment.author.avatarUrl, size = 32.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = comment.author.shownName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = ScheduleFormatter.relative(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text = comment.body, style = MaterialTheme.typography.bodyMedium)
        }
        CommentMenu(onDelete = onDelete, onReport = onReport)
    }
}

@Composable
private fun CommentMenu(onDelete: (() -> Unit)?, onReport: (() -> Unit)?) {
    if (onDelete == null && onReport == null) return
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Opções do comentário", modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            onDelete?.let {
                DropdownMenuItem(text = { Text("Apagar") }, onClick = { open = false; it() })
            }
            onReport?.let {
                DropdownMenuItem(text = { Text("Denunciar") }, onClick = { open = false; it() })
            }
        }
    }
}
