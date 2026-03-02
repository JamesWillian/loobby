package app.loobby.feature.groups.domain.repository

import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.GroupResponse

interface GroupsRepository {
    suspend fun createGroup(name: String, imageUrl: String?): GroupResponse
    suspend fun listMyGroups(): List<GroupResponse>
    suspend fun getGroupById(groupId: String): GroupResponse

    suspend fun joinGroup(groupId: String)
    suspend fun leaveGroup(groupId: String)

    suspend fun listMembers(groupId: String): List<GroupMemberResponse>
}