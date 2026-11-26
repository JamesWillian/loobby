package app.loobby.feature.home.presentation.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.UserAvatar

/**
 * A foto do perfil com o botão da câmera. Com [canEdit] (e-mail confirmado e rede),
 * tocar na foto ou na câmera escolhe uma nova.
 */
@Composable
fun EditableAvatar(
    url: String?,
    canEdit: Boolean,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickable = if (canEdit) Modifier.clickable(onClick = onClick) else Modifier
    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        UserAvatar(url = url, size = 96.dp, modifier = Modifier.clip(CircleShape).then(clickable))
        Surface(
            modifier = Modifier.size(32.dp).clip(CircleShape).then(clickable),
            color = if (canEdit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Trocar foto",
                        modifier = Modifier.size(16.dp),
                        tint = if (canEdit) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
