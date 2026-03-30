package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.repository.GroupsRepository

class UploadGroupImageUseCase(
    private val repo: GroupsRepository
) {
    suspend operator fun invoke(groupId: String, imageBytes: ByteArray, fileName: String): GroupDomain =
        repo.uploadGroupImage(groupId, imageBytes, fileName)
}