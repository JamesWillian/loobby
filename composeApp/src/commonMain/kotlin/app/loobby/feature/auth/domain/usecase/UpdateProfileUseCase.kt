package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.repository.AuthRepository

class UpdateProfileUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        username: String?,
        displayname: String?
    ): UserProfileResponse = repo.updateProfile(username, displayname)
}