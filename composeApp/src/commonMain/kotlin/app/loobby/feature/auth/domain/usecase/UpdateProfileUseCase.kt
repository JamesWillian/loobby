package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.repository.AuthRepository

class UpdateProfileUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        username: String? = null,
        displayname: String? = null
    ): UserProfileResponse = repository.updateProfile(username, displayname)
}