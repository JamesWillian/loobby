package app.loobby.feature.library.data.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.OfflineException
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.library.data.cache.LibraryCache
import app.loobby.feature.library.data.mapper.toDomain
import app.loobby.feature.library.data.mapper.toRequest
import app.loobby.feature.library.data.remote.LibraryApi
import app.loobby.feature.library.domain.model.GroupLibraryGame
import app.loobby.feature.library.domain.model.LibraryGame
import app.loobby.feature.library.domain.repository.LibraryRepository
import kotlinx.io.IOException

/**
 * As bibliotecas com cache offline, como as gameplays:
 * - leitura: com rede, a API, e o resultado vai para o cache; sem rede, ou com a
 *   conexão caindo no meio, o cache. Um erro da API (403, 404) não cai no cache;
 * - escrita: exige rede, e o que volta da API atualiza o cache.
 */
class LibraryRepositoryImpl(
    private val api: LibraryApi,
    private val cache: LibraryCache,
    private val connectivity: ConnectivityObserver,
    private val auth: AuthRepository,
) : LibraryRepository {

    override suspend fun myLibrary(): List<LibraryGame> {
        val user = userId()
        val items = readThrough(
            fetch = { api.myLibrary().also { cache.replaceMine(user, it) } },
            cached = { cache.mine(user) },
        )
        return items.map { it.toDomain() }
    }

    override suspend fun saveToMyLibrary(catalogId: String, owned: Set<Platform>): LibraryGame {
        requireOnline()
        val saved = api.saveMine(catalogId, owned.toRequest())
        cache.upsertMine(userId(), saved)
        return saved.toDomain()
    }

    override suspend fun removeFromMyLibrary(catalogId: String) {
        requireOnline()
        api.removeMine(catalogId)
        cache.removeMine(userId(), catalogId)
    }

    override suspend fun groupLibrary(groupId: String): List<GroupLibraryGame> {
        val user = userId()
        val items = readThrough(
            fetch = { api.groupLibrary(groupId).also { cache.replaceGroup(user, groupId, it) } },
            cached = { cache.group(user, groupId) },
        )
        return items.map { it.toDomain() }
    }

    override suspend fun addToGroupLibrary(groupId: String, catalogId: String): GroupLibraryGame {
        requireOnline()
        val added = api.addToGroup(groupId, catalogId)
        cache.upsertGroup(userId(), groupId, added)
        return added.toDomain()
    }

    override suspend fun removeFromGroupLibrary(groupId: String, catalogId: String) {
        requireOnline()
        api.removeFromGroup(groupId, catalogId)
        cache.removeFromGroup(userId(), groupId, catalogId)
    }

    /** Com rede, [fetch]; sem rede ou com a conexão caindo, [cached]. */
    private suspend fun <T> readThrough(fetch: suspend () -> List<T>, cached: () -> List<T>): List<T> {
        if (!connectivity.isOnlineNow()) return cached()
        return try {
            fetch()
        } catch (e: IOException) {
            cached().ifEmpty { throw e }
        }
    }

    private suspend fun userId(): String = auth.currentUserId().orEmpty()

    private fun requireOnline() {
        if (!connectivity.isOnlineNow()) throw OfflineException()
    }
}
