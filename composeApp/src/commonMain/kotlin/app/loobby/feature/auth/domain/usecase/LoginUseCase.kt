package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResponse {
        // Se for anônimo, guardar o refreshToken antes de sobrescrever
        if (repository.isAnonymous()) {
            repository.storedTokensFlow.first()?.let { tokens ->
                repository.saveAnonymousToken(tokens.refreshToken)
            }
        }
        return repository.login(email, password)
    }
}