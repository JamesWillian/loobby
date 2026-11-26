package app.loobby.feature.library.data.cache

import app.loobby.core.db.CacheJson
import app.loobby.db.FourPlayersDatabase
import app.loobby.feature.library.data.model.GroupLibraryGameResponse
import app.loobby.feature.library.data.model.LibraryGameResponse
import kotlin.time.Clock

/**
 * O cache offline das bibliotecas: as respostas da API como vieram (JSON), por
 * usuário e na ordem da API, que é a do nome do jogo.
 */
class LibraryCache(
    db: FourPlayersDatabase,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val database = db
    private val queries = db.gameLibraryQueries

    fun mine(userId: String): List<LibraryGameResponse> = read(userId, MINE)

    fun replaceMine(userId: String, items: List<LibraryGameResponse>) =
        replace(userId, MINE, items.map { it.gameId to CacheJson.encodeToString(it) })

    /** O jogo salvo entra no lugar dele, pelo nome; o resto da lista fica como estava. */
    fun upsertMine(userId: String, item: LibraryGameResponse) {
        val list = mine(userId).filterNot { it.gameId == item.gameId } + item
        replaceMine(userId, list.sortedWith(compareBy({ it.name.lowercase() }, { it.gameId })))
    }

    fun group(userId: String, groupId: String): List<GroupLibraryGameResponse> = read(userId, groupKey(groupId))

    fun replaceGroup(userId: String, groupId: String, items: List<GroupLibraryGameResponse>) =
        replace(userId, groupKey(groupId), items.map { it.gameId to CacheJson.encodeToString(it) })

    fun upsertGroup(userId: String, groupId: String, item: GroupLibraryGameResponse) {
        val list = group(userId, groupId).filterNot { it.gameId == item.gameId } + item
        replaceGroup(userId, groupId, list.sortedWith(compareBy({ it.name.lowercase() }, { it.gameId })))
    }

    fun removeMine(userId: String, gameId: String) = queries.deleteGame(userId, MINE, gameId)

    fun removeFromGroup(userId: String, groupId: String, gameId: String) =
        queries.deleteGame(userId, groupKey(groupId), gameId)

    private inline fun <reified T> read(userId: String, key: String): List<T> =
        queries.selectLibrary(userId, key).executeAsList().mapNotNull { decode<T>(it) }

    private fun replace(userId: String, key: String, items: List<Pair<String, String>>) {
        val time = now()
        database.transaction {
            queries.clearLibrary(userId, key)
            items.forEachIndexed { index, (gameId, json) ->
                queries.insert(userId, key, gameId, json, index.toLong(), time)
            }
        }
    }

    /** Um JSON que o app de hoje não lê (de uma versão antiga do cache) é ignorado. */
    private inline fun <reified T> decode(json: String): T? =
        runCatching { CacheJson.decodeFromString<T>(json) }.getOrNull()

    private companion object {
        const val MINE = "me"
        fun groupKey(groupId: String) = "group:$groupId"
    }
}
