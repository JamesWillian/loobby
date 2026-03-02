package app.loobby.feature.groups.data.repository

import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.GroupResponse
import app.loobby.feature.groups.data.remote.GroupsApi
import app.loobby.feature.groups.domain.repository.GroupsRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class GroupsRepositoryImpl(
    private val api: GroupsApi,
    private val authApi: AuthApi,          // pra refresh
    private val tokenStorage: TokenStorage // pra salvar tokens atualizados
) : GroupsRepository {

    override suspend fun createGroup(name: String, imageUrl: String?): GroupResponse =
        callWithRefresh { api.createGroup(CreateGroupRequest(name = name, imageUrl = imageUrl)) }

    override suspend fun listMyGroups(): List<GroupResponse> =
        callWithRefresh { api.listMyGroups() }

    override suspend fun getGroupById(groupId: String): GroupResponse =
        callWithRefresh { api.getGroupById(groupId) }

    override suspend fun joinGroup(groupId: String) =
        callWithRefresh { api.joinGroup(groupId) }

    override suspend fun leaveGroup(groupId: String) =
        callWithRefresh { api.leaveGroup(groupId) }

    override suspend fun listMembers(groupId: String): List<GroupMemberResponse> =
        callWithRefresh { api.listMembers(groupId) }

    // ----------------- refresh helper -----------------

    private suspend fun <T> callWithRefresh(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: ClientRequestException) {
            if (e.response.status != HttpStatusCode.Unauthorized) throw e

            val tokens = tokenStorage.getTokens()
                ?: throw IllegalStateException("Not authenticated")

            // refresh
            val refreshed = authApi.refresh(tokens.refreshToken)

            // mantém se era anônimo ou registrado
            val updatedTokens = tokens.copy(
                accessToken = refreshed.accessToken,
                refreshToken = refreshed.refreshToken,
                userId = refreshed.userId,
                username = refreshed.username,
                roles = refreshed.roles
            )
            tokenStorage.saveTokens(updatedTokens)

            // tenta de novo
            return block()
        }
    }
}