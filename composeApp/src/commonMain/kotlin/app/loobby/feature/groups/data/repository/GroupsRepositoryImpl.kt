package app.loobby.feature.groups.data.repository

import app.loobby.feature.groups.data.mapper.toDomain
import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.UpdateGroupRequest
import app.loobby.feature.groups.data.remote.GroupsApi
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.repository.GroupsRepository

class GroupsRepositoryImpl(
    private val api: GroupsApi
) : GroupsRepository {

    override suspend fun createGroup(name: String, imageUrl: String?): GroupDomain =
        api.createGroup(
            CreateGroupRequest(name = name, imageUrl = imageUrl)).toDomain()

    override suspend fun listMyGroups(): List<GroupDomain> =
        api.listMyGroups().map { it.toDomain() }

    override suspend fun getGroupById(groupId: String): GroupDomain =
        api.getGroupById(groupId).toDomain()

    override suspend fun getGroupByInvite(inviteCode: String): GroupDomain =
        api.getGroupByInvite(inviteCode).toDomain()

    override suspend fun joinGroup(groupId: String) =
        api.joinGroup(groupId)

    override suspend fun leaveGroup(groupId: String) =
        api.leaveGroup(groupId)

    override suspend fun listMembers(groupId: String): List<GroupMemberResponse> =
        api.listMembers(groupId)

    override suspend fun updateGroupName(groupId: String, name: String): GroupDomain =
        api.updateGroup(groupId, UpdateGroupRequest(name = name)).toDomain()

    override suspend fun uploadGroupImage(groupId: String, imageBytes: ByteArray, fileName: String): GroupDomain =
        api.uploadGroupImage(groupId, imageBytes, fileName).toDomain()

    override suspend fun deleteGroup(groupId: String) =
        api.deleteGroup(groupId)

    override suspend fun removeMember(groupId: String, memberId: String) =
        api.removeMember(groupId, memberId)
}