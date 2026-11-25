package app.loobby.feature.home.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.runtime.Composable
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.ui.components.AppSheet
import app.loobby.core.ui.components.SheetOptionRow
import app.loobby.theme.Spacing

/** O que o "+" da barra lateral oferece. As três ações precisam de rede. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrJoinSheet(
    onDismiss: () -> Unit,
    onNewGameplay: () -> Unit,
    onCreateGroup: () -> Unit,
    onJoinInvite: () -> Unit,
) {
    val isOnline = LocalIsOnline.current
    AppSheet(onDismiss = onDismiss, title = "O que você quer fazer?") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            SheetOptionRow(
                icon = Icons.Outlined.SportsEsports,
                title = "Nova gameplay",
                subtitle = "Num grupo ou única, chamando a galera por convite",
                enabled = isOnline,
                onClick = onNewGameplay,
            )
            SheetOptionRow(
                icon = Icons.Outlined.GroupAdd,
                title = "Criar grupo",
                subtitle = "Junte quem sempre joga junto",
                enabled = isOnline,
                onClick = onCreateGroup,
            )
            SheetOptionRow(
                icon = Icons.Outlined.Link,
                title = "Entrar com convite",
                subtitle = "O código de um grupo ou de uma gameplay",
                enabled = isOnline,
                onClick = onJoinInvite,
            )
        }
    }
}
