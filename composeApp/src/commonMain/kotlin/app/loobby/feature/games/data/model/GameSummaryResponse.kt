package app.loobby.feature.games.data.model

import kotlinx.serialization.Serializable

/** Item retornado por GET /games/search (resposta da API loobby, proxy do RAWG). */
@Serializable
data class GameSummaryResponse(
    val id: String,
    val slug: String? = null,
    val name: String,
    val backgroundImage: String? = null,
    val released: String? = null,
    val rating: Double? = null,
    val metacritic: Int? = null,
)
