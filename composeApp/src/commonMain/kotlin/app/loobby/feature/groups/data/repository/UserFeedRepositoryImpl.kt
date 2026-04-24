package app.loobby.feature.groups.data.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.db.LoobbyDatabase
import app.loobby.feature.groups.data.cache.toDomain
import app.loobby.feature.groups.data.mapper.toDomain
import app.loobby.feature.groups.data.remote.UserFeedApi
import app.loobby.feature.groups.domain.model.UserFeedDomain
import app.loobby.feature.groups.domain.repository.UserFeedRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * O feed é global do usuário (por userId); no cache guardamos uma única
 * tabela e sobrescrevemos por completo a cada refresh online. Isso
 * intencionalmente assume que o app é monousuário no device — se isso
 * deixar de valer (ex: multi-login), incluir `user_id` no WHERE.
 */
class UserFeedRepositoryImpl(
    private val api: UserFeedApi,
    private val db: LoobbyDatabase,
    private val connectivity: ConnectivityObserver,
) : UserFeedRepository {

    private val queries = db.feedItemQueries

    override suspend fun getUserFeed(userId: String): List<UserFeedDomain> {
        if (!connectivity.isOnlineNow()) return cached()
        return try {
            val fresh = api.getUserFeed(userId).map { it.toDomain() }
            replace(userId, fresh)
            fresh
        } catch (t: Throwable) {
            cached().ifEmpty { throw t }
        }
    }

    private fun cached(): List<UserFeedDomain> =
        queries.selectAll().executeAsList().map { it.toDomain() }

    private fun replace(userId: String, items: List<UserFeedDomain>) {
        val now = nowMillis()
        db.transaction {
            queries.clearAll()
            items.forEachIndexed { index, it ->
                queries.upsert(
                    id = it.id,
                    user_id = userId,       // guardamos o userId do contexto
                    name = it.name,
                    image_url = it.imageUrl,
                    entry_type = it.entryType.name,
                    is_finished = if (it.isFinished) 1L else 0L,
                    position = index.toLong(),
                    cached_at = now
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
