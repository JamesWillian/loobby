package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.repository.GroupsRepository

class RemoveGroupMemberUseCase(
    private val repo: GroupsRepository
) {
    suspend operator fun invoke(groupId: String, memberId: String) =
        repo.removeMember(groupId, memberId)
}