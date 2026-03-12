package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.domain.usecase.GetProfileUseCase
import app.loobby.feature.auth.domain.usecase.UpdateProfileUseCase
import app.loobby.feature.auth.domain.usecase.UploadAvatarUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    // ─── Load ───────────────────────────────────────

    fun loadProfile() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profile = getProfileUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = profile,
                        editUsername = profile.username,
                        editDisplayname = profile.displayname ?: ""
                    )
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Erro ao carregar perfil"
                    )
                }
            }
        }
    }

    // ─── Edit fields ────────────────────────────────

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(editUsername = value, errorMessage = null, successMessage = null) }
    }

    fun onDisplaynameChanged(value: String) {
        _uiState.update { it.copy(editDisplayname = value, errorMessage = null, successMessage = null) }
    }

    fun startEditing() {
        val profile = _uiState.value.profile ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                editUsername = profile.username,
                editDisplayname = profile.displayname ?: "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, errorMessage = null, successMessage = null) }
    }

    // ─── Save profile ───────────────────────────────

    fun saveProfile() {
        val state = _uiState.value
        val username = state.editUsername.trim()
        val displayname = state.editDisplayname.trim()

        if (username.length < 3) {
            _uiState.update { it.copy(errorMessage = "Username deve ter pelo menos 3 caracteres.") }
            return
        }

        // Envia apenas o que mudou
        val profile = state.profile
        val usernameToSend = if (username != profile?.username) username else null
        val displaynameToSend = if (displayname != (profile?.displayname ?: "")) displayname else null

        if (usernameToSend == null && displaynameToSend == null) {
            _uiState.update { it.copy(isEditing = false) }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                updateProfileUseCase(
                    username = usernameToSend,
                    displayname = displaynameToSend
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        successMessage = "Perfil atualizado!"
                    )
                }
                // Recarregar com dados atualizados
                loadProfile()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = mapError(t)
                    )
                }
            }
        }
    }

    // ─── Avatar ─────────────────────────────────────

    fun uploadAvatar(imageBytes: ByteArray, fileName: String) {
        scope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, errorMessage = null) }
            try {
                uploadAvatarUseCase(imageBytes, fileName)
                _uiState.update {
                    it.copy(
                        isUploadingAvatar = false,
                        successMessage = "Foto atualizada!"
                    )
                }
                loadProfile()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isUploadingAvatar = false,
                        errorMessage = t.message ?: "Erro ao enviar foto"
                    )
                }
            }
        }
    }

    // ─── Navigation ─────────────────────────────────

    fun dismiss() {
        _uiState.update { it.copy(shouldDismiss = true) }
    }

    fun resetDismiss() {
        _uiState.update { it.copy(shouldDismiss = false) }
    }

    // ─── Helpers ────────────────────────────────────

    private fun mapError(t: Throwable): String {
        val msg = t.message ?: "Erro desconhecido"
        return when {
            "409" in msg || "Conflict" in msg -> "Esse username já está em uso."
            "400" in msg || "Bad Request" in msg -> "Dados inválidos. Verifique os campos."
            else -> msg
        }
    }
}