package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.model.AuthSession

data class AuthUiState(
    val isLoading: Boolean = false,
    val session: AuthSession? = null,
    val profile: UserProfileResponse? = null,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,

    // Login fields
    val loginEmail: String = "",
    val loginPassword: String = "",

    // Register fields
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",

    // Navigation
    val showRegisterScreen: Boolean = false,
    val shouldDismiss: Boolean = false
)