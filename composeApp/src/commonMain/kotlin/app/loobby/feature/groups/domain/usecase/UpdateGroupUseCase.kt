package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.repository.GroupsRepository

class UpdateGroupUseCase(
    private val repo: GroupsRepository
) {
    suspend operator fun invoke(groupId: String, name: String): GroupDomain =
        repo.updateGroupName(groupId, name)
}