package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.logout()
}