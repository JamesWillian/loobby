package app.loobby.feature.gameplays.data.remote

import app.loobby.feature.gameplays.data.model.CommentResponse
import app.loobby.feature.gameplays.data.model.CreateGameplayRequest
import app.loobby.feature.gameplays.data.model.GameChoiceRequest
import app.loobby.feature.gameplays.data.model.GameplayResponse
import app.loobby.feature.gameplays.data.model.GameplaySummaryResponse
import app.loobby.feature.gameplays.data.model.InvitePreviewResponse
import kotlinx.serialization.json.JsonObject

/** As 19 rotas /v2 da API. Ver "Rotas da 2.0: gameplays" no README do loobby-api. */
interface GameplaysApi {
    suspend fun lobby(): List<GameplaySummaryResponse>
    suspend fun activeInGroup(groupId: String): List<GameplaySummaryResponse>
    suspend fun gameplay(id: String): GameplayResponse
    suspend fun createInGroup(groupId: String, body: CreateGameplayRequest): GameplayResponse
    suspend fun createSingle(body: CreateGameplayRequest): GameplayResponse

    /** [changes] leva só os campos que mudam. */
    suspend fun update(id: String, changes: JsonObject): GameplayResponse
    suspend fun end(id: String): GameplayResponse
    suspend fun invitePreview(code: String): InvitePreviewResponse
    suspend fun joinByInvite(code: String): GameplayResponse
    suspend fun respond(id: String, status: String): GameplayResponse
    suspend fun leave(id: String)
    suspend fun addGame(id: String, game: GameChoiceRequest): GameplayResponse
    suspend fun removeGame(id: String, votingGameId: String): GameplayResponse
    suspend fun vote(id: String, votingGameIds: List<String>): GameplayResponse
    suspend fun tiebreak(id: String): GameplayResponse
    suspend fun comments(id: String, before: String?, limit: Int): List<CommentResponse>
    suspend fun comment(id: String, body: String): CommentResponse
    suspend fun deleteComment(id: String, commentId: String)
    suspend fun reportComment(id: String, commentId: String, reason: String?)
}
