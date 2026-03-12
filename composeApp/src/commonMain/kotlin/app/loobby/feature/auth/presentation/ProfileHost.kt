package app.loobby.feature.auth.presentation

import androidx.compose.runtime.*
import org.koin.compose.koinInject

/**
 * Host do perfil — conecta o ProfileViewModel ao ProfileScreen.
 *
 * @param onDismiss chamado quando o usuário quer fechar a tela
 * @param onPickImage chamado quando o usuário clica no avatar para selecionar foto.
 *        A plataforma (Android/iOS) deve abrir o picker e retornar bytes + nome.
 */
@Composable
fun ProfileHost(
    onDismiss: () -> Unit,
    onPickImage: (onResult: (ByteArray, String) -> Unit) -> Unit,
    vm: ProfileViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) {
            vm.resetDismiss()
            onDismiss()
        }
    }

    ProfileScreen(
        state = state,
        onUsernameChanged = vm::onUsernameChanged,
        onDisplaynameChanged = vm::onDisplaynameChanged,
        onStartEditing = vm::startEditing,
        onCancelEditing = vm::cancelEditing,
        onSaveProfile = vm::saveProfile,
        onAvatarClick = {
            onPickImage { bytes, fileName ->
                vm.uploadAvatar(bytes, fileName)
            }
        },
        onBackClick = onDismiss
    )
}