package app.loobby.feature.groups.data.remote

import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.GroupResponse
import app.loobby.feature.groups.data.model.UpdateGroupRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class GroupsApiImpl(
    private val client: HttpClient
) : GroupsApi {

    override suspend fun createGroup(request: CreateGroupRequest): GroupResponse =
        client.post("/groups") { setBody(request) }.body()

    override suspend fun listMyGroups(): List<GroupResponse> =
        client.get("/groups").body()

    override suspend fun getGroupById(groupId: String): GroupResponse =
        client.get("/groups/$groupId").body()

    override suspend fun getGroupByInvite(inviteCode: String): GroupResponse =
        client.get("groups/invite/$inviteCode").body()

    override suspend fun joinGroup(groupId: String) {
        client.post("/groups/$groupId/members")
    }

    override suspend fun leaveGroup(groupId: String) {
        client.delete("/groups/$groupId/members/me")
    }

    override suspend fun listMembers(groupId: String): List<GroupMemberResponse> =
        client.get("/groups/$groupId/members").body()

    override suspend fun updateGroup(groupId: String, request: UpdateGroupRequest): GroupResponse =
        client.patch("/groups/$groupId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun uploadGroupImage(
        groupId: String,
        imageBytes: ByteArray,
        fileName: String
    ): GroupResponse =
        client.submitFormWithBinaryData(
            url = "/groups/$groupId/image",
            formData = formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    append(HttpHeaders.ContentType, "image/jpeg")
                })
            }
        ).body()

    override suspend fun deleteGroup(groupId: String) {
        client.delete("/groups/$groupId")
    }

    override suspend fun removeMember(groupId: String, memberId: String) {
        client.delete("/groups/$groupId/members/$memberId")
    }
}