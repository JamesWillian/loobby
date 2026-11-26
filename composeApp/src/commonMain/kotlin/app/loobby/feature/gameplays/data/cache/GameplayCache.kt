package app.loobby.feature.gameplays.data.cache

import app.loobby.core.db.CacheJson
import app.loobby.db.FourPlayersDatabase
import app.loobby.feature.gameplays.data.model.CommentResponse
import app.loobby.feature.gameplays.data.model.GameplayResponse
import app.loobby.feature.gameplays.data.model.GameplaySummaryResponse
import kotlin.time.Clock

/**
 * O cache offline das gameplays. Guarda as respostas da API como vieram (JSON), por
 * usuário: o que uma pessoa vê (status, votos, permissões) não aparece para outra
 * que entrar no mesmo aparelho.
 */
class GameplayCache(
    db: FourPlayersDatabase,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val database = db
    private val summaries = db.gameplaySummaryQueries
    private val lists = db.gameplayListEntryQueries
    private val details = db.gameplayDetailQueries
    private val comments = db.gameplayCommentQueries

    // ── Listas ─────────────────────────────────────────────────────────────

    fun list(userId: String, key: String): List<GameplaySummaryResponse> =
        lists.selectList(userId, key).executeAsList().mapNotNull { decode<GameplaySummaryResponse>(it) }

    /** Troca a lista inteira pela que veio da API, na mesma ordem. */
    fun replaceList(userId: String, key: String, items: List<GameplaySummaryResponse>) {
        val time = now()
        database.transaction {
            lists.clearList(userId, key)
            items.forEachIndexed { index, item ->
                summaries.upsert(userId, item.id, CacheJson.encodeToString(item), time)
                lists.insert(userId, key, item.id, index.toLong())
            }
            summaries.deleteOrphans(userId)
        }
    }

    // ── Detalhe ────────────────────────────────────────────────────────────

    fun detail(userId: String, id: String): GameplayResponse? =
        details.selectById(userId, id).executeAsOneOrNull()?.let { decode<GameplayResponse>(it) }

    fun saveDetail(userId: String, gameplay: GameplayResponse) {
        details.upsert(userId, gameplay.id, CacheJson.encodeToString(gameplay), now())
    }

    /** A pessoa saiu da gameplay: ela some das listas e do detalhe. */
    fun forget(userId: String, gameplayId: String) {
        database.transaction {
            lists.removeGameplay(userId, gameplayId)
            summaries.deleteOrphans(userId)
            details.deleteById(userId, gameplayId)
            comments.clearByGameplay(userId, gameplayId)
        }
    }

    // ── Comentários ────────────────────────────────────────────────────────

    fun comments(userId: String, gameplayId: String): List<CommentResponse> =
        comments.selectByGameplay(userId, gameplayId).executeAsList().mapNotNull { decode<CommentResponse>(it) }

    fun replaceComments(userId: String, gameplayId: String, page: List<CommentResponse>) {
        val time = now()
        database.transaction {
            comments.clearByGameplay(userId, gameplayId)
            page.forEachIndexed { index, comment ->
                comments.insert(userId, gameplayId, comment.id, CacheJson.encodeToString(comment), index.toLong(), time)
            }
        }
    }

    /** O comentário novo entra antes de todos: a posição negativa o põe no topo. */
    fun addComment(userId: String, comment: CommentResponse) {
        val time = now()
        comments.insert(userId, comment.gameplayId, comment.id, CacheJson.encodeToString(comment), -time, time)
    }

    fun removeComment(userId: String, commentId: String) {
        comments.deleteById(userId, commentId)
    }

    /** Um JSON que o app de hoje não lê (de uma versão antiga do cache) é ignorado. */
    private inline fun <reified T> decode(json: String): T? =
        runCatching { CacheJson.decodeFromString<T>(json) }.getOrNull()

    companion object {
        const val LOBBY = "lobby"

        /** Só a primeira página do Histórico. */
        const val HISTORY = "history"
        fun group(groupId: String) = "group:$groupId"

        /** Só a primeira página das Finalizadas do grupo. */
        fun ended(groupId: String) = "ended:$groupId"
    }
}
