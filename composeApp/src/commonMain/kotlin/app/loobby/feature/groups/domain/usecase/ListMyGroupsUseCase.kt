package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.repository.GroupsRepository

class ListMyGroupsUseCase(
    private val repo: GroupsRepository
) {
    suspend operator fun invoke() = repo.listMyGroups()
}