package app.loobby.feature.games.data.remote

import app.loobby.feature.games.data.model.GameDetailsResponse
import app.loobby.feature.games.data.model.GameSearchResponse

interface GamesApi {
    /** Busca no catálogo (proxy do RAWG). Exige rede. */
    suspend fun searchGames(query: String, page: Int): GameSearchResponse

    /** Detalhe de um jogo por id do RAWG. */
    suspend fun getGameById(id: String): GameDetailsResponse
}
