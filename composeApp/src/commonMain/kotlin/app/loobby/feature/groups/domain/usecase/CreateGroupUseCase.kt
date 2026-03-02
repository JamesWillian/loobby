package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.repository.GroupsRepository

class CreateGroupUseCase(
    private val repo: GroupsRepository
) {
    suspend operator fun invoke(name: String, imageUrl: String?) =
        repo.createGroup(name, imageUrl)
}