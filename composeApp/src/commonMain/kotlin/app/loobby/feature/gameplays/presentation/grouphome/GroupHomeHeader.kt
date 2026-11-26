package app.loobby.feature.gameplays.presentation.grouphome

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.ScreenHeader
import app.loobby.feature.home.presentation.components.RailToggleButton

/**
 * O topo do Início do grupo. Tocar no nome abre os detalhes do grupo, e o botão ao
 * lado, as Finalizadas. A gameplay nova fica no botão flutuante ou no placeholder
 * da lista, não aqui.
 */
@Composable
fun GroupHomeHeader(
    groupName: String,
    memberCount: Int?,
    isRailOpen: Boolean,
    onToggleRail: () -> Unit,
    onOpenGroupDetail: () -> Unit,
    onOpenEnded: () -> Unit,
) {
    ScreenHeader(
        title = groupName.ifBlank { "Grupo" },
        subtitle = memberCount?.let { if (it == 1) "1 membro" else "$it membros" },
        onTitleClick = onOpenGroupDetail,
        leading = { RailToggleButton(isRailOpen = isRailOpen, onToggle = onToggleRail) },
        titleAdornment = {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Detalhes do grupo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
        actions = {
            TextButton(onClick = onOpenEnded) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Finalizadas")
            }
        },
    )
}
