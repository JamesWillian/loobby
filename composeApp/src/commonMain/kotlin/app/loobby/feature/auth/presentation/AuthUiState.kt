package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.model.AuthSession

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isAnonymous: Boolean = true,
    val emailVerified: Boolean = true,
    val errorMessage: String? = null,

    val profile: UserMeResponse? = null,

    // Login fields
    val loginEmail: String = "",
    val loginPassword: String = "",

    // Register fields
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",

    // Email verification
    val isResendingVerification: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val verificationMessage: String? = null,

    // Forgot password
    val showForgotPassword: Boolean = false,
    val forgotPasswordEmail: String = "",
    val isSendingResetEmail: Boolean = false,
    val forgotPasswordMessage: String? = null,

    // Navigation
    val showRegisterScreen: Boolean = false,
    val shouldDismiss: Boolean = false,

    val welcomeName: String? = null
) {
    /**
     * O usuário tem restrições se é anônimo OU se registrou mas não verificou o email.
     * Usar esta propriedade em vez de checar isAnonymous diretamente
     * para determinar se pode criar grupos, eventos, etc.
     */
    val hasFullAccess: Boolean
        get() = isLoggedIn && !isAnonymous && emailVerified

    /**
     * Registrado mas ainda não verificou — mostra banner de verificação.
     */
    val needsEmailVerification: Boolean
        get() = isLoggedIn && !isAnonymous && !emailVerified

    /**
     * Pode reenviar email (cooldown zerou e não está enviando).
     */
    val canResendVerification: Boolean
        get() = !isResendingVerification && resendCooldownSeconds <= 0
}