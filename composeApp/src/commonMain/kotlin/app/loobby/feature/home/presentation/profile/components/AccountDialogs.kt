package app.loobby.feature.home.presentation.profile.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import app.loobby.core.media.CropAvatarSheet
import app.loobby.core.media.CroppedResult
import app.loobby.feature.auth.presentation.ChangePasswordSheet
import app.loobby.feature.auth.presentation.DeleteAccountDialog
import app.loobby.feature.auth.presentation.ProfileUiState

/**
 * Os diálogos da conta, como no sheet de perfil de antes: sair, excluir conta (com a
 * senha), recortar a foto escolhida e alterar a senha.
 */
@Composable
fun AccountDialogs(
    state: ProfileUiState,
    pendingAvatar: ByteArray?,
    showChangePassword: Boolean,
    onConfirmLogout: () -> Unit,
    onCancelLogout: () -> Unit,
    onDeletePasswordChange: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onAvatarCropped: (CroppedResult) -> Unit,
    onDismissCrop: () -> Unit,
    onDismissChangePassword: () -> Unit,
) {
    if (state.showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelLogout,
            title = { Text("Sair da conta") },
            text = { Text("Tem certeza que deseja sair? Você voltará como usuário anônimo.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmLogout,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Sair") }
            },
            dismissButton = { TextButton(onClick = onCancelLogout) { Text("Cancelar") } },
        )
    }
    if (state.showDeleteAccountDialog) {
        DeleteAccountDialog(
            password = state.deleteAccountPassword,
            onPasswordChange = onDeletePasswordChange,
            error = state.deleteAccountError,
            loading = state.isDeletingAccount,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
    if (pendingAvatar != null) {
        CropAvatarSheet(imageBytes = pendingAvatar, onConfirm = onAvatarCropped, onDismiss = onDismissCrop)
    }
    if (showChangePassword) {
        ChangePasswordSheet(onDismiss = onDismissChangePassword)
    }
}
