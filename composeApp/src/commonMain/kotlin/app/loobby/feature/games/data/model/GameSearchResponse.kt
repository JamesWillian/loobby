package app.loobby.feature.games.data.model

import kotlinx.serialization.Serializable

/** Resposta de GET /games/search. */
@Serializable
data class GameSearchResponse(
    val page: Int = 1,
    val count: Int = 0,
    val results: List<GameSummaryResponse> = emptyList(),
)
