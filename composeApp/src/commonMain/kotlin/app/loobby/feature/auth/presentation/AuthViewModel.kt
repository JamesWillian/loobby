package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.auth.domain.usecase.ForgotPasswordUseCase
import app.loobby.feature.auth.domain.usecase.GetProfileUseCase
import app.loobby.feature.auth.domain.usecase.InitializeAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.IsAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.LoginUseCase
import app.loobby.feature.auth.domain.usecase.LoginWithGoogleUseCase
import app.loobby.feature.auth.domain.usecase.RegisterUseCase
import app.loobby.feature.auth.domain.usecase.ResendVerificationUseCase
import app.loobby.feature.auth.domain.usecase.UpdateProfileUseCase
import app.loobby.feature.auth.domain.usecase.UploadAvatarUseCase
import app.loobby.feature.notifications.domain.usecase.RegisterDeviceTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val initializeAnonymousUseCase: InitializeAnonymousUseCase,
    private val isAnonymousUseCase: IsAnonymousUseCase,
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val authRepository: AuthRepository,
    private val resendVerificationUseCase: ResendVerificationUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
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
                    // Registra token de push sempre que houver sessão (inclusive anônima,
                    // porque o próprio backend aceita qualquer JWT válido). Falhas são
                    // silenciosas — notificações são best-effort e não devem bloquear login.
                    scope.launch {
                        registerDeviceTokenUseCase()
                    }
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
     * Atualiza isAnonymous + emailVerified + profile a partir das fontes reais.
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
                        isAnonymous = profile.isAnonymous,
                        emailVerified = profile.emailVerified
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

    /**
     * Limpa todos os campos de formulário do sheet de autenticação
     * (login, registro, esqueci senha) e o estado de navegação interno.
     *
     * Chamado quando o AuthBottomSheet é removido da composição (onDispose),
     * para que ao reabrir o sheet os campos comecem sempre vazios.
     *
     * IMPORTANTE: não toca em `profile`, `isLoggedIn`, `isAnonymous`, etc. —
     * apenas em estado efêmero da UI do bottom sheet.
     */
    fun clearAuthFields() {
        _uiState.update {
            it.copy(
                loginEmail = "",
                loginPassword = "",
                registerEmail = "",
                registerPassword = "",
                registerConfirmPassword = "",
                forgotPasswordEmail = "",
                forgotPasswordMessage = null,
                verificationMessage = null,
                errorMessage = null,
                showRegisterScreen = false,
                showForgotPassword = false
            )
        }
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
                        emailVerified = false,
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

    // ─── Email Verification ─────────────────────────────

    /**
     * Reenvia o email de verificação e inicia o countdown de cooldown.
     */
    fun resendVerificationEmail() {
        if (!_uiState.value.canResendVerification) return

        scope.launch {
            _uiState.update { it.copy(isResendingVerification = true, verificationMessage = null) }
            try {
                resendVerificationUseCase()
                _uiState.update {
                    it.copy(
                        isResendingVerification = false,
                        verificationMessage = "Email reenviado! Verifique sua caixa de entrada.",
                        resendCooldownSeconds = RESEND_COOLDOWN_SECONDS
                    )
                }
                startCooldownTimer()
            } catch (t: Throwable) {
                val msg = when {
                    "429" in (t.message ?: "") || "Too Many" in (t.message ?: "") ->
                        "Aguarde alguns minutos antes de solicitar novamente."
                    else -> t.message ?: "Erro ao reenviar email"
                }
                _uiState.update {
                    it.copy(
                        isResendingVerification = false,
                        verificationMessage = msg
                    )
                }
            }
        }
    }

    /**
     * Recarrega o perfil para checar se o email foi verificado.
     * Chamado quando o app volta ao foreground ou o usuário puxa pra atualizar.
     */
    fun checkEmailVerification() {
        loadProfile()
    }

    fun clearVerificationMessage() {
        _uiState.update { it.copy(verificationMessage = null) }
    }

    private fun startCooldownTimer() {
        scope.launch {
            while (_uiState.value.resendCooldownSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(resendCooldownSeconds = it.resendCooldownSeconds - 1) }
            }
        }
    }

    // ─── Forgot Password ────────────────────────────────

    fun showForgotPassword() {
        _uiState.update {
            it.copy(
                showForgotPassword = true,
                forgotPasswordEmail = it.loginEmail,  // pré-preenche com o email do login
                forgotPasswordMessage = null,
                errorMessage = null
            )
        }
    }

    fun hideForgotPassword() {
        _uiState.update {
            it.copy(
                showForgotPassword = false,
                forgotPasswordMessage = null
            )
        }
    }

    fun onForgotPasswordEmailChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordEmail = value, forgotPasswordMessage = null) }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.forgotPasswordEmail.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(forgotPasswordMessage = "Digite seu email.") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isSendingResetEmail = true, forgotPasswordMessage = null) }
            try {
                forgotPasswordUseCase(email)
                _uiState.update {
                    it.copy(
                        isSendingResetEmail = false,
                        forgotPasswordMessage = "Se o email estiver cadastrado, você receberá um link para redefinir a senha."
                    )
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isSendingResetEmail = false,
                        forgotPasswordMessage = t.message ?: "Erro ao enviar email."
                    )
                }
            }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                loginWithGoogleUseCase(idToken)
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
                    it.copy(isLoading = false, errorMessage = mapError(t))
                }
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun mapError(t: Throwable): String {
        val msg = t.message ?: "Erro desconhecido"
        return when {
            // Verificações específicas pelo conteúdo do body (vêm do
            // ApiErrorResponse.message do backend) — precedem o mapeamento
            // por status para dar mensagens mais úteis ao usuário.
            "Email already registered" in msg -> "Este e-mail já está cadastrado. Faça login ou use outro e-mail."
            "User already registered" in msg -> "Esta conta já foi registrada."
            "Invalid credentials" in msg -> "E-mail ou senha incorretos."

            "401" in msg || "Unauthorized" in msg -> "E-mail ou senha incorretos."
            "409" in msg || "Conflict" in msg -> "Esse e-mail ou username já está em uso."
            "400" in msg || "Bad Request" in msg -> "Dados inválidos. Verifique os campos."
            else -> msg
        }
    }

    companion object {
        private const val RESEND_COOLDOWN_SECONDS = 180 // 3 minutos
    }
}