package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.foundation.layout.Box
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
import app.loobby.feature.gameplays.domain.model.Gameplay

/**
 * O ⋮ da gameplay: Editar e Encerrar para quem gerencia; Excluir para os mesmos,
 * também na encerrada; Sair na única. Some quando a pessoa não tem nenhum deles.
 */
@Composable
fun GameplayActionsMenu(
    gameplay: Gameplay,
    enabled: Boolean,
    onEdit: () -> Unit,
    onEnd: () -> Unit,
    onDelete: () -> Unit,
    onLeave: () -> Unit,
) {
    val canManage = gameplay.permissions.manage && !gameplay.isEnded
    val canDelete = gameplay.permissions.delete
    val canLeave = gameplay.permissions.leave
    if (!canManage && !canDelete && !canLeave) return

    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, enabled = enabled) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Mais opções")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            if (canManage) {
                DropdownMenuItem(text = { Text("Editar") }, onClick = { open = false; onEdit() })
                DropdownMenuItem(text = { Text("Encerrar") }, onClick = { open = false; onEnd() })
            }
            if (canLeave) {
                DropdownMenuItem(
                    text = { Text("Sair da gameplay", color = MaterialTheme.colorScheme.error) },
                    onClick = { open = false; onLeave() },
                )
            }
            if (canDelete) {
                DropdownMenuItem(
                    text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                    onClick = { open = false; onDelete() },
                )
            }
        }
    }
}
