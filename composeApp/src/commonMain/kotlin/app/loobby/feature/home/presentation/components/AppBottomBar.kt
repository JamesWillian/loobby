package app.loobby.feature.home.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.loobby.core.navigation.AppTab
import app.loobby.core.ui.components.UserAvatar

/** A barra de baixo: Início, Histórico e Perfil. O ícone do Perfil é a foto da pessoa. */
@Composable
fun AppBottomBar(
    currentTab: AppTab,
    avatarUrl: String?,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        IconTab(
            tab = AppTab.HOME,
            label = "Início",
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            currentTab = currentTab,
            onSelect = onSelect,
        )
        IconTab(
            tab = AppTab.HISTORY,
            label = "Histórico",
            icon = Icons.Outlined.History,
            selectedIcon = Icons.Filled.History,
            currentTab = currentTab,
            onSelect = onSelect,
        )
        val profileSelected = currentTab == AppTab.PROFILE
        NavigationBarItem(
            selected = profileSelected,
            onClick = { onSelect(AppTab.PROFILE) },
            icon = {
                // Do tamanho dos ícones (24 dp), para o rótulo ficar alinhado com os outros.
                UserAvatar(
                    url = avatarUrl,
                    size = 24.dp,
                    ringColor = if (profileSelected) MaterialTheme.colorScheme.primary else null,
                )
            },
            label = { Text("Perfil") },
        )
    }
}

@Composable
private fun RowScope.IconTab(
    tab: AppTab,
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    currentTab: AppTab,
    onSelect: (AppTab) -> Unit,
) {
    val selected = currentTab == tab
    NavigationBarItem(
        selected = selected,
        onClick = { onSelect(tab) },
        icon = { Icon(if (selected) selectedIcon else icon, contentDescription = null) },
        label = { Text(label) },
    )
}
