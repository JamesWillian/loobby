package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.repository.GroupsRepository

class DeleteGroupUseCase(
    private val repo: GroupsRepository
) {
    suspend operator fun invoke(groupId: String) =
        repo.deleteGroup(groupId)
}