package app.loobby.feature.home.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.theme.Spacing

/** O topo do Perfil de quem tem conta: a foto, o nome, o @, o e-mail e o aviso da última edição. */
@Composable
fun AccountCard(
    profile: UserMeResponse,
    canChangeAvatar: Boolean,
    isUploadingAvatar: Boolean,
    message: String?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        EditableAvatar(
            url = profile.avatarUrl,
            canEdit = canChangeAvatar,
            isUploading = isUploadingAvatar,
            onClick = onAvatarClick,
            modifier = Modifier.padding(bottom = Spacing.sm),
        )
        Text(
            text = profile.displayname?.takeIf { it.isNotBlank() } ?: profile.username,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "@${profile.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        profile.email?.let { email ->
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
