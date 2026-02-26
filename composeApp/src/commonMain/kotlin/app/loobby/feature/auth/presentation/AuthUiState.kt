package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.model.AuthSession

data class AuthUiState(
    val isLoading: Boolean = false,
    val session: AuthSession? = null,
    val profile: UserProfileResponse? = null,
    val errorMessage: String? = null
) {
    val isLoggedIn: Boolean get() = session != null && !session.isAnonymous
    val isAnonymous: Boolean get() = session?.isAnonymous == true
}