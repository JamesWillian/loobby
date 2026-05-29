package app.loobby.feature.games.domain.model

/**
 * Jogo do catálogo RAWG, na forma usada pelo app. É o que persistimos no cache
 * local quando um jogo é escolhido para um evento de gameplay.
 */
data class GameDomain(
    val id: String,
    val slug: String?,
    val name: String,
    val backgroundImage: String?,
    val released: String?,        // ISO date (yyyy-MM-dd); formatação fica na UI
    val rating: Double?,
    val metacritic: Int?,
)
