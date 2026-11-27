package app.loobby.feature.gameplays.data.remote

import app.loobby.feature.gameplays.data.model.CommentRequest
import app.loobby.feature.gameplays.data.model.CommentResponse
import app.loobby.feature.gameplays.data.model.CreateGameplayRequest
import app.loobby.feature.gameplays.data.model.GameChoiceRequest
import app.loobby.feature.gameplays.data.model.GameplayResponse
import app.loobby.feature.gameplays.data.model.GameplaySummaryResponse
import app.loobby.feature.gameplays.data.model.InvitePreviewResponse
import app.loobby.feature.gameplays.data.model.ReportRequest
import app.loobby.feature.gameplays.data.model.RespondRequest
import app.loobby.feature.gameplays.data.model.VotesRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject

/**
 * As rotas /v2 sobre o cliente autenticado (token e refresh automáticos; status
 * fora de 2xx vira exceção). Os caminhos são montados por segmento, para que o
 * "$" do convite e os ids cheguem como estão.
 */
class GameplaysApiImpl(private val client: HttpClient) : GameplaysApi {

    override suspend fun lobby(): List<GameplaySummaryResponse> =
        client.get { path("v2", "me", "gameplays") }.body()

    override suspend fun activeInGroup(groupId: String): List<GameplaySummaryResponse> =
        client.get {
            path("v2", "groups", groupId, "gameplays")
            parameter("status", "active")
        }.body()

    override suspend fun endedInGroup(groupId: String, before: String?, limit: Int): List<GameplaySummaryResponse> =
        client.get {
            path("v2", "groups", groupId, "gameplays")
            parameter("status", "ended")
            before?.let { parameter("before", it) }
            parameter("limit", limit)
        }.body()

    override suspend fun history(before: String?, limit: Int): List<GameplaySummaryResponse> =
        client.get {
            path("v2", "me", "history")
            before?.let { parameter("before", it) }
            parameter("limit", limit)
        }.body()

    override suspend fun gameplay(id: String): GameplayResponse =
        client.get { gameplayPath(id) }.body()

    override suspend fun createInGroup(groupId: String, body: CreateGameplayRequest): GameplayResponse =
        client.post {
            path("v2", "groups", groupId, "gameplays")
            json(body)
        }.body()

    override suspend fun createSingle(body: CreateGameplayRequest): GameplayResponse =
        client.post {
            path("v2", "gameplays")
            json(body)
        }.body()

    override suspend fun update(id: String, changes: JsonObject): GameplayResponse =
        client.patch {
            gameplayPath(id)
            json(changes)
        }.body()

    override suspend fun end(id: String): GameplayResponse =
        client.post { gameplayPath(id, "end") }.body()

    override suspend fun delete(id: String) {
        client.delete { gameplayPath(id) }
    }

    override suspend fun invitePreview(code: String): InvitePreviewResponse =
        client.get { path("v2", "invites", code) }.body()

    override suspend fun joinByInvite(code: String): GameplayResponse =
        client.post { path("v2", "invites", code) }.body()

    override suspend fun respond(id: String, status: String): GameplayResponse =
        client.put {
            gameplayPath(id, "participants", "me")
            json(RespondRequest(status))
        }.body()

    override suspend fun leave(id: String) {
        client.delete { gameplayPath(id, "participants", "me") }
    }

    override suspend fun addGame(id: String, game: GameChoiceRequest): GameplayResponse =
        client.post {
            gameplayPath(id, "games")
            json(game)
        }.body()

    override suspend fun removeGame(id: String, votingGameId: String): GameplayResponse =
        client.delete { gameplayPath(id, "games", votingGameId) }.body()

    override suspend fun vote(id: String, votingGameIds: List<String>): GameplayResponse =
        client.put {
            gameplayPath(id, "votes", "me")
            json(VotesRequest(votingGameIds))
        }.body()

    override suspend fun tiebreak(id: String): GameplayResponse =
        client.post { gameplayPath(id, "tiebreak") }.body()

    override suspend fun comments(id: String, before: String?, limit: Int): List<CommentResponse> =
        client.get {
            gameplayPath(id, "comments")
            before?.let { parameter("before", it) }
            parameter("limit", limit)
        }.body()

    override suspend fun comment(id: String, body: String): CommentResponse =
        client.post {
            gameplayPath(id, "comments")
            json(CommentRequest(body))
        }.body()

    override suspend fun deleteComment(id: String, commentId: String) {
        client.delete { gameplayPath(id, "comments", commentId) }
    }

    override suspend fun reportComment(id: String, commentId: String, reason: String?) {
        client.post {
            gameplayPath(id, "comments", commentId, "report")
            json(ReportRequest(reason))
        }
    }

    private fun HttpRequestBuilder.path(vararg segments: String) {
        url { appendPathSegments(*segments) }
    }

    private fun HttpRequestBuilder.gameplayPath(id: String, vararg rest: String) =
        path("v2", "gameplays", id, *rest)

    private inline fun <reified T> HttpRequestBuilder.json(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
