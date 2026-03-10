package app.loobby.feature.groups.data.repository

import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.RefreshTokenRequest
import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.groups.data.mapper.toDomain
import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.GroupResponse
import app.loobby.feature.groups.data.remote.GroupsApi
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.repository.GroupsRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

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

    override suspend fun joinGroup(groupId: String) =
        api.joinGroup(groupId)

    override suspend fun leaveGroup(groupId: String) =
        api.leaveGroup(groupId)

    override suspend fun listMembers(groupId: String): List<GroupMemberResponse> =
        api.listMembers(groupId)
}