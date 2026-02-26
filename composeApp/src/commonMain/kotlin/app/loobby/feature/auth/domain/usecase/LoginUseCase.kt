package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthSession =
        repo.login(email, password)
}