package app.loobby.feature.gameplays.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.SectionTitle
import app.loobby.feature.gameplays.domain.model.GameplayComment
import app.loobby.feature.gameplays.presentation.detail.components.CommentComposer
import app.loobby.feature.gameplays.presentation.detail.components.CommentItem
import app.loobby.theme.Spacing

/**
 * Os comentários como itens da lista da tela: o título, a caixa de escrever, os
 * comentários e o "Carregar mais". Ficam na mesma LazyColumn da gameplay, para
 * rolar junto com ela.
 */
fun LazyListScope.commentsSection(
    comments: CommentsUiState,
    canComment: Boolean,
    isOnline: Boolean,
    currentUserId: String?,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onLoadMore: () -> Unit,
    onDelete: (GameplayComment) -> Unit,
    onReport: (GameplayComment) -> Unit,
) {
    item(key = "comments-title") { SectionTitle(text = "Comentários") }
    if (canComment) {
        item(key = "comments-composer") {
            CommentComposer(
                draft = comments.draft,
                isPosting = comments.isPosting,
                enabled = isOnline,
                onDraftChange = onDraftChange,
                onSend = onSend,
            )
        }
    }
    val visible = comments.visible
    if (visible.isEmpty() && !comments.isLoading) {
        item(key = "comments-empty") {
            Text(
                text = "Ninguém comentou ainda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    items(visible, key = { "comment-${it.id}" }) { comment ->
        CommentItem(
            comment = comment,
            onDelete = if (comment.canDelete && isOnline) ({ onDelete(comment) }) else null,
            onReport = if (comment.author.id != currentUserId && isOnline) ({ onReport(comment) }) else null,
        )
    }
    if (comments.mayHaveMore) {
        item(key = "comments-more") {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs), contentAlignment = Alignment.Center) {
                if (comments.isLoadingMore) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = onLoadMore, enabled = isOnline) { Text("Carregar mais") }
                }
            }
        }
    }
}
