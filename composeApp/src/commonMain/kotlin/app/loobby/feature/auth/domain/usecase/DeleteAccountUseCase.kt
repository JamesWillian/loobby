package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.repository.AuthRepository

class DeleteAccountUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(password: String) {
        repository.deleteAccount(password)
    }
}