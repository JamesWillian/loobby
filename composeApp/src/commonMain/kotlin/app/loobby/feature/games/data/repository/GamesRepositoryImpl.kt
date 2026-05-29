package app.loobby.feature.games.data.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.OfflineException
import app.loobby.db.LoobbyDatabase
import app.loobby.feature.games.data.cache.toDomain
import app.loobby.feature.games.data.mapper.toDomain
import app.loobby.feature.games.data.remote.GamesApi
import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.games.domain.repository.GamesRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Segue o mesmo padrão dos demais repositórios (ex.: TeamsRepositoryImpl):
 *  - busca exige rede;
 *  - getGame faz read-through (rede -> cache, com fallback no cache);
 *  - saveGame persiste apenas o jogo efetivamente escolhido para um evento.
 */
class GamesRepositoryImpl(
    private val api: GamesApi,
    private val db: LoobbyDatabase,
    private val connectivity: ConnectivityObserver,
) : GamesRepository {

    private val gameQueries = db.gameQueries

    override suspend fun search(query: String, page: Int): List<GameDomain> {
        if (!connectivity.isOnlineNow()) throw OfflineException()
        return api.searchGames(query, page).results.map { it.toDomain() }
    }

    override suspend fun getGame(id: String): GameDomain? {
        if (!connectivity.isOnlineNow()) return cachedGame(id)
        return try {
            val fresh = api.getGameById(id).toDomain()
            persist(fresh)
            fresh
        } catch (t: Throwable) {
            cachedGame(id)
        }
    }

    override suspend fun saveGame(game: GameDomain) {
        persist(game)
    }

    override fun cachedGame(id: String): GameDomain? =
        gameQueries.selectById(id).executeAsOneOrNull()?.toDomain()

    // ── helpers de cache ─────────────────────────────────────────────────────

    private fun persist(game: GameDomain) {
        gameQueries.upsert(
            id = game.id,
            slug = game.slug,
            name = game.name,
            background_image = game.backgroundImage,
            released = game.released,
            rating = game.rating,
            metacritic = game.metacritic?.toLong(),
            cached_at = nowMillis(),
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
