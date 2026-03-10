package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.repository.AuthRepository

class IsAnonymousUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Boolean = repository.isAnonymous()
}