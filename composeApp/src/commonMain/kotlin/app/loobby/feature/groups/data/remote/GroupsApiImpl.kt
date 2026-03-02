package app.loobby.feature.groups.data.remote

import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.GroupResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*

class GroupsApiImpl(
    private val client: HttpClient
) : GroupsApi {

    override suspend fun createGroup(request: CreateGroupRequest): GroupResponse =
        client.post("/groups") { setBody(request) }.body()

    override suspend fun listMyGroups(): List<GroupResponse> =
        client.get("/groups").body()

    override suspend fun getGroupById(groupId: String): GroupResponse =
        client.get("/groups/$groupId").body()

    override suspend fun joinGroup(groupId: String) {
        client.post("/groups/$groupId/members")
    }

    override suspend fun leaveGroup(groupId: String) {
        client.delete("/groups/$groupId/members/me")
    }

    override suspend fun listMembers(groupId: String): List<GroupMemberResponse> =
        client.get("/groups/$groupId/members").body()
}