package app.loobby.feature.groups.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.core.network.LocalIsOnline

enum class ActionSheetOption {
    CREATE_GROUP,
    JOIN_BY_INVITE,
    INSTANT_EVENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSheet(
    onDismiss: () -> Unit,
    onOptionSelected: (ActionSheetOption) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Todas as 3 ações precisam de rede (POST para criar / fetch do invite +
    // POST pra entrar). Quando offline, os itens entram em disabled.
    val isOnline = LocalIsOnline.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "O que deseja fazer?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ActionSheetItem(
                icon = { Icon(Icons.Outlined.Group, contentDescription = null) },
                title = "Criar Grupo",
                subtitle = "Crie um novo grupo e convide seus amigos",
                enabled = isOnline,
                onClick = { onOptionSelected(ActionSheetOption.CREATE_GROUP) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ActionSheetItem(
                icon = { Icon(Icons.Outlined.Link, contentDescription = null) },
                title = "Entrar com Convite",
                subtitle = "Use um código de convite para entrar em um grupo ou evento",
                enabled = isOnline,
                onClick = { onOptionSelected(ActionSheetOption.JOIN_BY_INVITE) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ActionSheetItem(
                icon = { Icon(Icons.Outlined.FlashOn, contentDescription = null) },
                title = "Evento Instantâneo",
                subtitle = "Crie um evento rápido sem grupo",
                enabled = isOnline,
                onClick = { onOptionSelected(ActionSheetOption.INSTANT_EVENT) }
            )
        }
    }
}

@Composable
private fun ActionSheetItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                icon()
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}