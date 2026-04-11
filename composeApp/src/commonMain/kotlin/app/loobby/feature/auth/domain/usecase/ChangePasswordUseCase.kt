package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.repository.AuthRepository

class ChangePasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String, confirmPassword: String) {
        repository.changePassword(currentPassword, newPassword, confirmPassword)
    }
}