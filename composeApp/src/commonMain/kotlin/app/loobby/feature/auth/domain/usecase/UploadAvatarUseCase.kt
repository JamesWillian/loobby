package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.repository.AuthRepository

class UploadAvatarUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        fileName: String
    ): UserProfileResponse = repository.uploadAvatar(imageBytes, fileName)
}