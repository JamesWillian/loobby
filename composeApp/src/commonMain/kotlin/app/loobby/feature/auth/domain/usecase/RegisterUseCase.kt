package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResponse =
        repository.register(email, password)
}