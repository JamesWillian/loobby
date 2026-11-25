package app.loobby.feature.home.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** O botão que abre e fecha a barra lateral do Início. */
@Composable
fun RailToggleButton(isRailOpen: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isRailOpen) Icons.AutoMirrored.Outlined.MenuOpen else Icons.Outlined.Menu,
            contentDescription = if (isRailOpen) "Fechar a barra lateral" else "Abrir a barra lateral",
        )
    }
}
