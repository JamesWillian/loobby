package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResponse {
        // Se for anônimo, guardar o id antes de sobrescrever
        if (repository.isAnonymous()) {
            repository.currentUserId()?.let { anonId ->
                repository.saveAnonymousId(anonId)
            }
        }
        return repository.login(email, password)
    }
}