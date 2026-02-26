package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository

class InitializeAnonymousUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): AuthSession = repo.initializeAnonymousIfNeeded()
}