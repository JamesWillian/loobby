package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.repository.GroupsRepository

class GetGroupByIdUseCase(
    private val repo: GroupsRepository
) {
    suspend operator fun invoke(groupId: String) = repo.getGroupById(groupId)
}