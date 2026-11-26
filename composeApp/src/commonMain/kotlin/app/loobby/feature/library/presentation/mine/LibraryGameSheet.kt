package app.loobby.feature.library.presentation.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.core.ui.components.AppSheet
import app.loobby.core.ui.components.ConfirmDialog
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.games.presentation.GameCoverImage
import app.loobby.feature.library.domain.model.LibraryGame
import app.loobby.feature.library.presentation.components.OwnedPlatformsSelector
import app.loobby.feature.library.presentation.components.catalogPlatformsLine
import app.loobby.theme.Spacing

/**
 * Um jogo da biblioteca: o "tenho o jogo" e o tirar da biblioteca. As plataformas do
 * catálogo vêm primeiro; as outras também valem, porque o catálogo às vezes falha.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryGameSheet(
    game: LibraryGame,
    canEdit: Boolean,
    onToggle: (Platform) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    TrackScreen(AppScreen.LIBRARY_GAME)
    var confirmRemove by remember { mutableStateOf(false) }

    AppSheet(onDismiss = onDismiss, title = game.name) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                GameCoverImage(url = game.coverUrl, size = 64.dp)
                Text(
                    text = catalogPlatformsLine(game.availablePlatforms),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(text = "Tenho o jogo em", style = MaterialTheme.typography.titleSmall)
                OwnedPlatformsSelector(
                    choices = game.platformChoices,
                    selected = game.ownedPlatforms,
                    enabled = canEdit,
                    onToggle = onToggle,
                )
                Text(
                    text = "Nas gameplays, o grupo vê quem vai e tem o jogo na plataforma da gameplay. " +
                        "A sua biblioteca, só você vê.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = { confirmRemove = true },
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Tirar da biblioteca")
            }
        }
    }

    if (confirmRemove) {
        ConfirmDialog(
            title = "Tirar da biblioteca?",
            message = "${game.name} sai da sua biblioteca, com o \"tenho o jogo\".",
            confirmLabel = "Tirar",
            destructive = true,
            onConfirm = { confirmRemove = false; onRemove() },
            onDismiss = { confirmRemove = false },
        )
    }
}
