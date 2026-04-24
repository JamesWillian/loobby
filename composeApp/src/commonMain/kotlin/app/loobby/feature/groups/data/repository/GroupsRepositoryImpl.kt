package app.loobby.feature.groups.data.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.OfflineException
import app.loobby.db.LoobbyDatabase
import app.loobby.feature.groups.data.cache.toDomain
import app.loobby.feature.groups.data.cache.toResponse
import app.loobby.feature.groups.data.mapper.toDomain
import app.loobby.feature.groups.data.model.CreateGroupRequest
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.UpdateGroupRequest
import app.loobby.feature.groups.data.remote.GroupsApi
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.repository.GroupsRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Implementação single-source-of-truth dos grupos.
 *  - Leituras: se online, chama a API, atualiza o cache e retorna o fresco.
 *    Se offline (ou se o fetch lança), tenta devolver o que tem no cache.
 *  - Escritas: chamam a API; em sucesso, sincronizam o cache localmente.
 *    Ainda não bloqueiam com OfflineException — isso virá no passo 4.
 */
class GroupsRepositoryImpl(
    private val api: GroupsApi,
    private val db: LoobbyDatabase,
    private val connectivity: ConnectivityObserver,
) : GroupsRepository {

    private val groupQueries = db.groupQueries
    private val memberQueries = db.groupMemberQueries

    override suspend fun createGroup(name: String, imageUrl: String?): GroupDomain {
        requireOnline()
        val created = api.createGroup(CreateGroupRequest(name = name, imageUrl = imageUrl)).toDomain()
        // Insere no cache já como "meu grupo" — a próxima chamada a
        // `listMyGroups` vai sobrescrever posições, mas manter o grupo.
        upsertMine(created, position = Long.MAX_VALUE)
        return created
    }

    override suspend fun listMyGroups(): List<GroupDomain> {
        if (!connectivity.isOnlineNow()) return cachedMyGroups()
        return try {
            val fresh = api.listMyGroups().map { it.toDomain() }
            replaceMyGroups(fresh)
            fresh
        } catch (t: Throwable) {
            cachedMyGroups().ifEmpty { throw t }
        }
    }

    override suspend fun getGroupById(groupId: String): GroupDomain {
        if (!connectivity.isOnlineNow()) return cachedGroupById(groupId)
        return try {
            val fresh = api.getGroupById(groupId).toDomain()
            upsertOther(fresh)
            fresh
        } catch (t: Throwable) {
            runCatching { cachedGroupById(groupId) }.getOrNull() ?: throw t
        }
    }

    override suspend fun getGroupByInvite(inviteCode: String): GroupDomain {
        if (!connectivity.isOnlineNow()) return cachedGroupByInvite(inviteCode)
        return try {
            val fresh = api.getGroupByInvite(inviteCode).toDomain()
            upsertOther(fresh)
            fresh
        } catch (t: Throwable) {
            runCatching { cachedGroupByInvite(inviteCode) }.getOrNull() ?: throw t
        }
    }

    override suspend fun joinGroup(groupId: String) {
        requireOnline()
        api.joinGroup(groupId)
        // A próxima `listMyGroups` materializa; nada a fazer aqui.
    }

    override suspend fun leaveGroup(groupId: String) {
        requireOnline()
        api.leaveGroup(groupId)
        groupQueries.deleteById(groupId)
        memberQueries.clearByGroup(groupId)
    }

    override suspend fun listMembers(groupId: String): List<GroupMemberResponse> {
        if (!connectivity.isOnlineNow()) return cachedMembers(groupId)
        return try {
            val fresh = api.listMembers(groupId)
            replaceMembers(groupId, fresh)
            fresh
        } catch (t: Throwable) {
            cachedMembers(groupId).ifEmpty { throw t }
        }
    }

    override suspend fun updateGroupName(groupId: String, name: String): GroupDomain {
        requireOnline()
        val updated = api.updateGroup(groupId, UpdateGroupRequest(name = name)).toDomain()
        upsertKeepingFlag(updated)
        return updated
    }

    override suspend fun uploadGroupImage(groupId: String, imageBytes: ByteArray, fileName: String): GroupDomain {
        requireOnline()
        val updated = api.uploadGroupImage(groupId, imageBytes, fileName).toDomain()
        upsertKeepingFlag(updated)
        return updated
    }

    override suspend fun deleteGroup(groupId: String) {
        requireOnline()
        api.deleteGroup(groupId)
        groupQueries.deleteById(groupId)
        memberQueries.clearByGroup(groupId)
    }

    override suspend fun removeMember(groupId: String, memberId: String) {
        requireOnline()
        api.removeMember(groupId, memberId)
        memberQueries.deleteMember(groupId, memberId)
    }

    // ── helpers de cache ───────────────────────────────────────────────

    private fun cachedMyGroups(): List<GroupDomain> =
        groupQueries.selectMyGroups().executeAsList().map { it.toDomain() }

    private fun cachedGroupById(id: String): GroupDomain =
        groupQueries.selectById(id).executeAsOne().toDomain()

    private fun cachedGroupByInvite(code: String): GroupDomain =
        groupQueries.selectByInvite(code).executeAsOne().toDomain()

    private fun cachedMembers(groupId: String): List<GroupMemberResponse> =
        memberQueries.selectByGroup(groupId).executeAsList().map { it.toResponse() }

    private fun replaceMyGroups(groups: List<GroupDomain>) {
        val now = nowMillis()
        db.transaction {
            // Limpa o flag de "meu grupo" de todos; os novos serão remarcados.
            groupQueries.clearMyGroupsFlag()
            groups.forEachIndexed { index, g ->
                groupQueries.upsert(
                    id = g.id,
                    name = g.name,
                    invite_code = g.inviteCode,
                    image_url = g.imageUrl,
                    owner_id = g.ownerId,
                    created_at = g.createdAt,
                    is_mine = 1L,
                    position = index.toLong(),
                    cached_at = now
                )
            }
        }
    }

    private fun replaceMembers(groupId: String, members: List<GroupMemberResponse>) {
        val now = nowMillis()
        db.transaction {
            memberQueries.clearByGroup(groupId)
            members.forEachIndexed { index, m ->
                memberQueries.upsert(
                    group_id = groupId,
                    user_id = m.userId,
                    username = m.username,
                    displayname = m.displayname,
                    avatar_url = m.avatarUrl,
                    is_owner = if (m.isOwner) 1L else 0L,
                    position = index.toLong(),
                    cached_at = now
                )
            }
        }
    }

    private fun upsertMine(g: GroupDomain, position: Long) {
        groupQueries.upsert(
            id = g.id,
            name = g.name,
            invite_code = g.inviteCode,
            image_url = g.imageUrl,
            owner_id = g.ownerId,
            created_at = g.createdAt,
            is_mine = 1L,
            position = position,
            cached_at = nowMillis()
        )
    }

    private fun upsertOther(g: GroupDomain) {
        // Preserva o flag `is_mine` se já estiver cacheado (ex.: getGroupById
        // sobre um grupo que o usuário já entrou). Se não existir, entra como
        // "outro" (não aparece em Meus Grupos).
        val existing = groupQueries.selectById(g.id).executeAsOneOrNull()
        groupQueries.upsert(
            id = g.id,
            name = g.name,
            invite_code = g.inviteCode,
            image_url = g.imageUrl,
            owner_id = g.ownerId,
            created_at = g.createdAt,
            is_mine = existing?.is_mine ?: 0L,
            position = existing?.position ?: -1L,
            cached_at = nowMillis()
        )
    }

    /** Para updateGroupName/uploadGroupImage: mantém is_mine/position. */
    private fun upsertKeepingFlag(g: GroupDomain) = upsertOther(g)

    @OptIn(ExperimentalTime::class)
    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * Guarda de escrita: sem rede, nem tentamos bater no backend — a camada
     * superior (ViewModel / UI) traduz em mensagem apropriada. Evita pending
     * requests travando coroutines e mantém o contrato "sem fila offline".
     */
    private fun requireOnline() {
        if (!connectivity.isOnlineNow()) throw OfflineException()
    }
}
