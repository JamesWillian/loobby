package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.auth.domain.usecase.GetProfileUseCase
import app.loobby.feature.auth.domain.usecase.InitializeAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.IsAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.LoginUseCase
import app.loobby.feature.auth.domain.usecase.RegisterUseCase
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

class AuthViewModel(
    private val initializeAnonymousUseCase: InitializeAnonymousUseCase,
    private val isAnonymousUseCase: IsAnonymousUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val authRepository: AuthRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(AuthUiState(isLoading = true))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            authRepository.sessionFlow.collect { session ->
                if (session != null) {
                    _uiState.update {
                        it.copy(
                            isAnonymous = session.isAnonymous,
                            isLoggedIn = !session.isAnonymous
                        )
                    }
                    loadProfile()
                } else {
                    // Tokens foram limpos (logout em andamento)
                    _uiState.update {
                        it.copy(
                            isAnonymous = true,
                            isLoggedIn = false,
                            profile = null
                        )
                    }
                }
            }
        }

        scope.launch {
            try {
                initializeAnonymousUseCase()
                refreshAuthStatus()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * Atualiza isAnonymous + profile a partir das fontes reais.
     * Chamado após init, login, register.
     */
    private fun refreshAuthStatus() {
        scope.launch {
            try {
                val anonymous = isAnonymousUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAnonymous = anonymous,
                        isLoggedIn = !anonymous
                    )
                }
                // Carrega perfil (funciona tanto pra anônimo quanto registrado)
                loadProfile()
            } catch (_: Throwable) {
                // Se falhar, pelo menos atualiza o loading
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadProfile() {
        scope.launch {
            try {
                val profile = getProfileUseCase()
                _uiState.update {
                    it.copy(
                        profile = profile,
                        isAnonymous = profile.isAnonymous
                    )
                }
            } catch (_: Throwable) {
                // Profile load failure não bloqueia o app
            }
        }
    }

    // ─── Field updates ──────────────────────────────────

    fun onLoginEmailChanged(value: String) {
        _uiState.update { it.copy(loginEmail = value, errorMessage = null) }
    }

    fun onLoginPasswordChanged(value: String) {
        _uiState.update { it.copy(loginPassword = value, errorMessage = null) }
    }

    fun onRegisterEmailChanged(value: String) {
        _uiState.update { it.copy(registerEmail = value, errorMessage = null) }
    }

    fun onRegisterPasswordChanged(value: String) {
        _uiState.update { it.copy(registerPassword = value, errorMessage = null) }
    }

    fun onRegisterConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(registerConfirmPassword = value, errorMessage = null) }
    }

    // ─── Navigation ─────────────────────────────────────

    fun navigateToRegister() {
        _uiState.update { it.copy(showRegisterScreen = true, errorMessage = null) }
    }

    fun navigateBackToLogin() {
        _uiState.update { it.copy(showRegisterScreen = false, errorMessage = null) }
    }

    fun dismiss() {
        _uiState.update { it.copy(shouldDismiss = true) }
    }

    fun resetDismiss() {
        _uiState.update { it.copy(shouldDismiss = false) }
    }

    // ─── Actions ────────────────────────────────────────

    fun login() {
        val state = _uiState.value
        val email = state.loginEmail.trim()
        val password = state.loginPassword

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha e-mail e senha.") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                loginUseCase(email, password)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isAnonymous = false,
                        shouldDismiss = true
                    )
                }
                loadProfile()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = mapError(t)
                    )
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        val email = state.registerEmail.trim()
        val password = state.registerPassword
        val confirm = state.registerConfirmPassword

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha e-mail e senha.") }
            return
        }
        if (password != confirm) {
            _uiState.update { it.copy(errorMessage = "As senhas não coincidem.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "A senha deve ter pelo menos 6 caracteres.") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                registerUseCase(email, password)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isAnonymous = false,
                        shouldDismiss = true
                    )
                }
                loadProfile()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = mapError(t)
                    )
                }
            }
        }
    }

    private fun mapError(t: Throwable): String {
        val msg = t.message ?: "Erro desconhecido"
        return when {
            "401" in msg || "Unauthorized" in msg -> "E-mail ou senha incorretos."
            "409" in msg || "Conflict" in msg -> "Esse e-mail ou username já está em uso."
            "400" in msg || "Bad Request" in msg -> "Dados inválidos. Verifique os campos."
            else -> msg
        }
    }
}