package app.loobby.feature.games.data.model

import kotlinx.serialization.Serializable

/**
 * Resposta de GET /games/{id}. O backend devolve mais campos (descriptionRaw,
 * genres, platforms), mas só mapeamos o que é usado/persistido no app — o Json
 * do Ktor está com ignoreUnknownKeys, então os demais são descartados.
 */
@Serializable
data class GameDetailsResponse(
    val id: String,
    val slug: String? = null,
    val name: String,
    val backgroundImage: String? = null,
    val released: String? = null,
    val rating: Double? = null,
    val metacritic: Int? = null,
)
