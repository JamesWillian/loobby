package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.UserAvatar
import app.loobby.feature.gameplays.domain.model.GameplayUser
import app.loobby.theme.Spacing

/** Quem vai e tem [gameName] na biblioteca, numa plataforma da gameplay. */
@Composable
fun OwnersDialog(gameName: String, owners: List<GameplayUser>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(gameName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                if (owners.isEmpty()) {
                    Text("Ninguém que vai tem o jogo na biblioteca, numa plataforma desta gameplay.")
                } else {
                    Text(
                        text = "Têm o jogo na biblioteca, numa plataforma desta gameplay:",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    owners.forEach { owner ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            UserAvatar(url = owner.avatarUrl, size = 28.dp)
                            Text(text = owner.shownName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}
