package app.loobby.feature.games.data.remote

import app.loobby.feature.games.data.model.GameDetailsResponse
import app.loobby.feature.games.data.model.GameSearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GamesApiImpl(
    private val client: HttpClient,
) : GamesApi {

    override suspend fun searchGames(query: String, page: Int): GameSearchResponse =
        client.get("games/search") {
            parameter("q", query)
            parameter("page", page)
        }.body()

    override suspend fun getGameById(id: String): GameDetailsResponse =
        client.get("games/$id").body()
}
