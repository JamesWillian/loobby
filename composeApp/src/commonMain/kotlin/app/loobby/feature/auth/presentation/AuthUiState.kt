package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.model.AuthSession

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isAnonymous: Boolean = true,
    val errorMessage: String? = null,

    val profile: UserMeResponse? = null,

    // Login fields
    val loginEmail: String = "",
    val loginPassword: String = "",

    // Register fields
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",

    // Navigation
    val showRegisterScreen: Boolean = false,
    val shouldDismiss: Boolean = false,

    val welcomeName: String? = null
)