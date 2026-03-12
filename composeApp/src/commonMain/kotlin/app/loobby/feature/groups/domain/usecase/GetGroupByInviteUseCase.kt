package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.repository.GroupsRepository

class GetGroupByInviteUseCase(
    private val repository: GroupsRepository
) {
    suspend operator fun invoke(inviteCode: String): GroupDomain {
        return repository.getGroupByInvite(inviteCode)
    }
}