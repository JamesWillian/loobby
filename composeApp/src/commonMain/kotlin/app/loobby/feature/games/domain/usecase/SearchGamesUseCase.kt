package app.loobby.feature.games.domain.usecase

import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.games.domain.repository.GamesRepository

class SearchGamesUseCase(private val repository: GamesRepository) {
    suspend operator fun invoke(query: String, page: Int = 1): List<GameDomain> =
        repository.search(query, page)
}
