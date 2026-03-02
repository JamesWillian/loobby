package app.loobby.feature.groups.data.remote

import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.GroupResponse

interface GroupsApi {
    suspend fun createGroup(request: CreateGroupRequest): GroupResponse
    suspend fun listMyGroups(): List<GroupResponse>
    suspend fun getGroupById(groupId: String): GroupResponse

    suspend fun joinGroup(groupId: String) // 204
    suspend fun leaveGroup(groupId: String) // 204

    suspend fun listMembers(groupId: String): List<GroupMemberResponse>
}