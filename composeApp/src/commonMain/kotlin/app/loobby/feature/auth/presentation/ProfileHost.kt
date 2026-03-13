package app.loobby.feature.auth.presentation

import androidx.compose.runtime.*
import app.loobby.core.media.rememberImagePicker
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Host do perfil — conecta o ProfileViewModel ao ProfileScreen.
 * Usa rememberImagePicker() (expect/actual) para selecionar foto.
 */
@Composable
fun ProfileHost(
    onDismiss: () -> Unit,
    vm: ProfileViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val imagePicker = rememberImagePicker()
    val coroutineScope = rememberCoroutineScope()

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
            coroutineScope.launch {
                val picked = imagePicker.pickImage() ?: return@launch
                vm.uploadAvatar(picked.bytes, picked.fileName)
            }
        },
        onBackClick = onDismiss
    )
}