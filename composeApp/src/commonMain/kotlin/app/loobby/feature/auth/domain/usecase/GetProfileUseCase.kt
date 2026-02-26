package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.repository.AuthRepository

class GetProfileUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): UserProfileResponse = repo.getProfile()
}