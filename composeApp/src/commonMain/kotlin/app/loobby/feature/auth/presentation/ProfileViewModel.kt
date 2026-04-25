package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.auth.domain.usecase.ChangePasswordUseCase
import app.loobby.feature.auth.domain.usecase.DeleteAccountUseCase
import app.loobby.feature.auth.domain.usecase.GetProfileUseCase
import app.loobby.feature.auth.domain.usecase.InitializeAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.LogoutUseCase
import app.loobby.feature.auth.domain.usecase.RecoverAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.UpdateProfileUseCase
import app.loobby.feature.auth.domain.usecase.UploadAvatarUseCase
import app.loobby.feature.notifications.domain.usecase.UnregisterDeviceTokenUseCase
import app.loobby.feature.notifications.platform.PushTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val recoverAnonymousUseCase: RecoverAnonymousUseCase,
    private val initializeAnonymousUseCase: InitializeAnonymousUseCase,
    private val authRepository: AuthRepository,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val unregisterDeviceTokenUseCase: UnregisterDeviceTokenUseCase,
    private val pushTokenProvider: PushTokenProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        scope.launch {
            authRepository.sessionFlow
                .map { it?.userId }
                .distinctUntilChanged()
                .collect {
                    _uiState.update {
                        it.copy(
                            profile = null,
                            isEditing = false,
                            editUsername = "",
                            editDisplayname = "",
                            successMessage = null,
                            errorMessage = null
                        )
                    }
                    loadProfile()
                }
        }
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

    // ─── Change Password ────────────────────────────

    fun hideChangePassword() {
        _uiState.update {
            it.copy(
                showChangePassword = false,
                changePasswordMessage = null,
                changePasswordSuccess = false,
                // Limpa também os campos de senha — caso contrário o usuário
                // veria os valores digitados anteriormente ao reabrir o sheet.
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = ""
            )
        }
    }

    fun onCurrentPasswordChanged(value: String) {
        _uiState.update { it.copy(currentPassword = value, changePasswordMessage = null) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, changePasswordMessage = null) }
    }

    fun onConfirmNewPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmNewPassword = value, changePasswordMessage = null) }
    }

    fun changePassword() {
        val state = _uiState.value

        if (state.currentPassword.isBlank()) {
            _uiState.update { it.copy(changePasswordMessage = "Digite a senha atual.") }
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.update { it.copy(changePasswordMessage = "A nova senha deve ter pelo menos 6 caracteres.") }
            return
        }
        if (state.newPassword != state.confirmNewPassword) {
            _uiState.update { it.copy(changePasswordMessage = "As senhas não coincidem.") }
            return
        }
        if (state.currentPassword == state.newPassword) {
            _uiState.update { it.copy(changePasswordMessage = "A nova senha deve ser diferente da atual.") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isChangingPassword = true, changePasswordMessage = null) }
            try {
                changePasswordUseCase(state.currentPassword, state.newPassword, state.confirmNewPassword)
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        changePasswordSuccess = true,
                        changePasswordMessage = "Senha alterada com sucesso!"
                    )
                }
            } catch (t: Throwable) {
                val msg = when {
                    "401" in (t.message ?: "") || "Unauthorized" in (t.message ?: "") ->
                        "Senha atual incorreta."
                    "400" in (t.message ?: "") || "Bad Request" in (t.message ?: "") ->
                        t.message?.substringAfter("\"message\":\"")?.substringBefore("\"")
                            ?: "Dados inválidos."
                    else -> t.message ?: "Erro ao alterar senha."
                }
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        changePasswordMessage = msg
                    )
                }
            }
        }
    }

    // ─── More Options Menu ───────────────────────────

    fun onMoreOptionsClick() {
        _uiState.update { it.copy(showMoreOptionsMenu = true) }
    }

    fun onMoreOptionsDismiss() {
        _uiState.update { it.copy(showMoreOptionsMenu = false) }
    }

    // ─── Delete Account ──────────────────────────────

    fun onDeleteAccountClick() {
        _uiState.update {
            it.copy(
                showMoreOptionsMenu = false,
                showDeleteAccountDialog = true,
                deleteAccountPassword = "",
                deleteAccountError = null
            )
        }
    }

    fun onDeleteAccountDialogDismiss() {
        _uiState.update {
            it.copy(
                showDeleteAccountDialog = false,
                deleteAccountPassword = "",
                deleteAccountError = null
            )
        }
    }

    fun onDeleteAccountPasswordChange(value: String) {
        _uiState.update { it.copy(deleteAccountPassword = value, deleteAccountError = null) }
    }

    fun onDeleteAccountConfirm() {
        val password = _uiState.value.deleteAccountPassword
        if (password.isBlank()) {
            _uiState.update { it.copy(deleteAccountError = "Informe sua senha.") }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, deleteAccountError = null) }
            try {
                deleteAccountUseCase(password)
                // Após excluir, limpa a sessão e inicia fluxo anônimo
                val anonymousToken = authRepository.getSavedAnonymousToken()
                logoutUseCase()
                if (!anonymousToken.isNullOrBlank()) {
                    recoverAnonymousUseCase(anonymousToken)
                } else {
                    initializeAnonymousUseCase()
                }
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        showDeleteAccountDialog = false,
                        shouldDismiss = true
                    )
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        deleteAccountError = "Senha incorreta ou erro ao excluir conta."
                    )
                }
            }
        }
    }

    // ─── Logout ─────────────────────────────────

    fun requestLogout() {
        _uiState.update { it.copy(showLogoutConfirmation = true) }
    }

    fun cancelLogout() {
        _uiState.update { it.copy(showLogoutConfirmation = false) }
    }

    fun confirmLogout() {
        _uiState.update { it.copy(showLogoutConfirmation = false) }
        logout()
    }

    fun logout() {
        scope.launch {
            val anonymousToken = authRepository.getSavedAnonymousToken()
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Desregistrar o token FCM ANTES de limpar o Bearer (o endpoint é autenticado).
                // Falha silenciosa: se der erro, seguimos com o logout normal.
                runCatching {
                    pushTokenProvider.getToken()?.let { token ->
                        unregisterDeviceTokenUseCase(token)
                    }
                }

                logoutUseCase()
                if (!anonymousToken.isNullOrBlank()) {
                    // Tenta recuperar a sessão anônima anterior
                    recoverAnonymousUseCase(anonymousToken)
                } else {
                    // Sem token anônimo salvo → cria nova sessão anônima
                    initializeAnonymousUseCase()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        shouldDismiss = true
                    )
                }
            } catch(t: Throwable) {
                try {
                    initializeAnonymousUseCase()
                    _uiState.update {
                        it.copy(isLoading = false, shouldDismiss = true)
                    }
                } catch (t2: Throwable) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = t2.message ?: "Erro ao restaurar sessão"
                        )
                    }
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

    /**
     * Reseta o estado efêmero do ProfileBottomSheet para o estado de
     * "tela inicial de Perfil". Chamado quando o sheet sai de composição —
     * assim, ao reabrir, o usuário sempre cai na visualização do perfil
     * (e não no modo Editar Perfil que ele tinha deixado aberto).
     *
     * Mantém o profile carregado para evitar flash de loading na reabertura.
     */
    fun resetSheetState() {
        _uiState.update {
            it.copy(
                isEditing = false,
                errorMessage = null,
                successMessage = null,
                showMoreOptionsMenu = false,
                showLogoutConfirmation = false,
                // Restaura os campos de edição aos valores do perfil atual
                editUsername = it.profile?.username ?: "",
                editDisplayname = it.profile?.displayname ?: ""
            )
        }
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