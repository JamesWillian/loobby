package app.loobby.feature.home.presentation.profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler

/**
 * O menu ⋮ da conta. Alterar senha só aparece para quem tem e-mail confirmado. As
 * ações que dependem da API ficam desligadas sem rede; sair funciona sem ela.
 */
@Composable
fun AccountMenu(
    expanded: Boolean,
    hasFullAccess: Boolean,
    isOnline: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Box {
        IconButton(onClick = onExpand) {
            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            if (hasFullAccess) {
                DropdownMenuItem(
                    text = { Text("Alterar senha") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    enabled = isOnline,
                    onClick = { onDismiss(); onChangePassword() },
                )
            }
            DropdownMenuItem(
                text = { Text("Denunciar conteúdo inapropriado") },
                leadingIcon = { Icon(Icons.Outlined.Report, contentDescription = null) },
                onClick = {
                    onDismiss()
                    uriHandler.openUri(
                        "mailto:jammeswm@gmail.com" +
                            "?subject=Den%C3%BAncia%20de%20Conte%C3%BAdo%20Inapropriado%20no%204Players"
                    )
                },
            )
            DropdownMenuItem(
                text = { Text("Sair da conta") },
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                onClick = { onDismiss(); onLogout() },
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Excluir conta", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                enabled = isOnline,
                onClick = onDeleteAccount,
            )
        }
    }
}
