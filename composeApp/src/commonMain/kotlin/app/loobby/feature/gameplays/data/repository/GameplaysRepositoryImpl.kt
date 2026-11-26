package app.loobby.feature.gameplays.data.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.OfflineException
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.gameplays.data.cache.GameplayCache
import app.loobby.feature.gameplays.data.mapper.toDomain
import app.loobby.feature.gameplays.data.mapper.toPatchJson
import app.loobby.feature.gameplays.data.mapper.toRequest
import app.loobby.feature.gameplays.data.model.GameplayResponse
import app.loobby.feature.gameplays.data.model.GameplaySummaryResponse
import app.loobby.feature.gameplays.data.remote.GameplaysApi
import app.loobby.feature.gameplays.domain.model.CommentsPage
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayChanges
import app.loobby.feature.gameplays.domain.model.GameplayComment
import app.loobby.feature.gameplays.domain.model.GameplayDraft
import app.loobby.feature.gameplays.domain.model.GameplayInvitePreview
import app.loobby.feature.gameplays.domain.model.GameplayRules
import app.loobby.feature.gameplays.domain.model.GameplaySummary
import app.loobby.feature.gameplays.domain.model.InviteCode
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.repository.GameplaysRepository
import kotlinx.io.IOException

/**
 * As gameplays com cache offline.
 * - Leitura: com rede, a API, e o resultado vai para o cache. Sem rede, ou se a
 *   conexão cair no meio, o cache. Um erro da API (404, 403) não cai no cache: o
 *   que ele diz vale mais do que o que estava guardado.
 * - Escrita: exige rede. A gameplay que volta da API substitui a do cache.
 */
class GameplaysRepositoryImpl(
    private val api: GameplaysApi,
    private val cache: GameplayCache,
    private val connectivity: ConnectivityObserver,
    private val auth: AuthRepository,
) : GameplaysRepository {

    override suspend fun lobby(): List<GameplaySummary> =
        readList(GameplayCache.LOBBY) { api.lobby() }

    override suspend fun activeInGroup(groupId: String): List<GameplaySummary> =
        readList(GameplayCache.group(groupId)) { api.activeInGroup(groupId) }

    override suspend fun gameplay(id: String): Gameplay {
        val user = userId()
        val response = readThrough(
            fetch = { api.gameplay(id).also { cache.saveDetail(user, it) } },
            cached = { cache.detail(user, id) },
        ) ?: throw OfflineException("Sem conexão, e essa gameplay ainda não foi aberta neste aparelho.")
        return response.toDomain()
    }

    override suspend fun create(draft: GameplayDraft): Gameplay = write {
        val body = draft.toRequest()
        draft.groupId?.let { api.createInGroup(it, body) } ?: api.createSingle(body)
    }

    override suspend fun update(id: String, changes: GameplayChanges): Gameplay =
        write { api.update(id, changes.toPatchJson()) }

    override suspend fun end(id: String): Gameplay = write { api.end(id) }

    override suspend fun respond(id: String, status: ParticipationStatus): Gameplay =
        write { api.respond(id, status.id) }

    override suspend fun vote(id: String, votingGameIds: List<String>): Gameplay =
        write { api.vote(id, votingGameIds) }

    override suspend fun addVotingGame(id: String, game: GameChoice): Gameplay =
        write { api.addGame(id, game.toRequest()) }

    override suspend fun removeVotingGame(id: String, votingGameId: String): Gameplay =
        write { api.removeGame(id, votingGameId) }

    override suspend fun openTiebreak(id: String): Gameplay = write { api.tiebreak(id) }

    override suspend fun leave(id: String) {
        requireOnline()
        api.leave(id)
        cache.forget(userId(), id)
    }

    override suspend fun invitePreview(code: String): GameplayInvitePreview {
        requireOnline()
        return api.invitePreview(InviteCode.normalize(code)).toDomain()
    }

    override suspend fun joinByInvite(code: String): Gameplay =
        write { api.joinByInvite(InviteCode.normalize(code)) }

    override suspend fun comments(gameplayId: String, before: String?): CommentsPage {
        val limit = GameplayRules.COMMENTS_PAGE
        if (before != null) {
            requireOnline()
            val older = api.comments(gameplayId, before, limit)
            return CommentsPage(older.map { it.toDomain() }, mayHaveMore = older.size == limit)
        }
        val user = userId()
        val firstPage = readThrough(
            fetch = { api.comments(gameplayId, null, limit).also { cache.replaceComments(user, gameplayId, it) } },
            cached = { cache.comments(user, gameplayId) },
        ).orEmpty()
        return CommentsPage(firstPage.map { it.toDomain() }, mayHaveMore = firstPage.size == limit)
    }

    override suspend fun postComment(gameplayId: String, body: String): GameplayComment {
        requireOnline()
        val created = api.comment(gameplayId, body)
        cache.addComment(userId(), created)
        return created.toDomain()
    }

    override suspend fun deleteComment(gameplayId: String, commentId: String) {
        requireOnline()
        api.deleteComment(gameplayId, commentId)
        cache.removeComment(userId(), commentId)
    }

    override suspend fun reportComment(gameplayId: String, commentId: String, reason: String?) {
        requireOnline()
        api.reportComment(gameplayId, commentId, reason)
        // Quem denunciou não vê mais o comentário: ele sai também do cache.
        cache.removeComment(userId(), commentId)
    }

    // ── Apoio ──────────────────────────────────────────────────────────────

    private suspend fun readList(
        key: String,
        fetch: suspend () -> List<GameplaySummaryResponse>,
    ): List<GameplaySummary> {
        val user = userId()
        val items = readThrough(
            fetch = { fetch().also { cache.replaceList(user, key, it) } },
            cached = { cache.list(user, key) },
        ).orEmpty()
        return items.map { it.toDomain() }
    }

    /** Com rede, [fetch]; sem rede ou com a conexão caindo, [cached]. */
    private suspend fun <T> readThrough(fetch: suspend () -> T, cached: () -> T?): T? {
        if (!connectivity.isOnlineNow()) return cached()
        return try {
            fetch()
        } catch (e: IOException) {
            cached() ?: throw e
        }
    }

    private suspend fun write(call: suspend () -> GameplayResponse): Gameplay {
        requireOnline()
        val response = call()
        cache.saveDetail(userId(), response)
        return response.toDomain()
    }

    private suspend fun userId(): String = auth.currentUserId().orEmpty()

    private fun requireOnline() {
        if (!connectivity.isOnlineNow()) throw OfflineException()
    }
}
