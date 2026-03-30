package app.loobby.feature.groups.domain.repository

import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.domain.model.GroupDomain

interface GroupsRepository {
    suspend fun createGroup(name: String, imageUrl: String?): GroupDomain
    suspend fun listMyGroups(): List<GroupDomain>
    suspend fun getGroupById(groupId: String): GroupDomain
    suspend fun getGroupByInvite(inviteCode: String): GroupDomain

    suspend fun joinGroup(groupId: String)
    suspend fun leaveGroup(groupId: String)

    suspend fun listMembers(groupId: String): List<GroupMemberResponse>

    suspend fun updateGroupName(groupId: String, name: String): GroupDomain
    suspend fun uploadGroupImage(groupId: String, imageBytes: ByteArray, fileName: String): GroupDomain
    suspend fun deleteGroup(groupId: String)
    suspend fun removeMember(groupId: String, memberId: String)
}