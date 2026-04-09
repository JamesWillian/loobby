package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.repository.AuthRepository

class ResendVerificationUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.resendVerification()
    }
}