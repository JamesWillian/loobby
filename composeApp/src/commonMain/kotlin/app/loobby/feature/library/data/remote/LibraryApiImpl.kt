package app.loobby.feature.library.data.remote

import app.loobby.feature.library.data.model.GroupLibraryGameResponse
import app.loobby.feature.library.data.model.LibraryGameResponse
import app.loobby.feature.library.data.model.OwnedPlatformsRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

/** As rotas de biblioteca sobre o cliente autenticado; status fora de 2xx vira exceção. */
class LibraryApiImpl(private val client: HttpClient) : LibraryApi {

    override suspend fun myLibrary(): List<LibraryGameResponse> =
        client.get { path("v2", "me", "library") }.body()

    override suspend fun saveMine(gameId: String, ownedPlatforms: List<String>): LibraryGameResponse =
        client.put {
            path("v2", "me", "library", gameId)
            contentType(ContentType.Application.Json)
            setBody(OwnedPlatformsRequest(ownedPlatforms))
        }.body()

    override suspend fun removeMine(gameId: String) {
        client.delete { path("v2", "me", "library", gameId) }
    }

    override suspend fun groupLibrary(groupId: String): List<GroupLibraryGameResponse> =
        client.get { path("v2", "groups", groupId, "library") }.body()

    override suspend fun addToGroup(groupId: String, gameId: String): GroupLibraryGameResponse =
        client.put { path("v2", "groups", groupId, "library", gameId) }.body()

    override suspend fun removeFromGroup(groupId: String, gameId: String) {
        client.delete { path("v2", "groups", groupId, "library", gameId) }
    }

    private fun HttpRequestBuilder.path(vararg segments: String) {
        url { appendPathSegments(*segments) }
    }
}
