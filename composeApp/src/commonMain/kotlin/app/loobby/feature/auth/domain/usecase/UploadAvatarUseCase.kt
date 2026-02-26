package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.repository.AuthRepository

class UploadAvatarUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        fileName: String,
        bytes: ByteArray,
        contentType: String
    ): UserProfileResponse = repo.uploadAvatar(fileName, bytes, contentType)
}