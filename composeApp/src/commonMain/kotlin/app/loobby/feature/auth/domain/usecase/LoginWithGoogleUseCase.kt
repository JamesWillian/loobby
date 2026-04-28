package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

class LoginWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): AuthResponse {
        // Se for anônimo, guardar o refreshToken antes de sobrescrever — assim,
        // se o usuário fizer logout depois, conseguimos restaurar a sessão anônima.
        if (repository.isAnonymous()) {
            repository.storedTokensFlow.first()?.let { tokens ->
                repository.saveAnonymousToken(tokens.refreshToken)
            }
        }
        return repository.loginWithGoogle(idToken)
    }
}
