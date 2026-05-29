package app.loobby.feature.games.domain.usecase

import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.games.domain.repository.GamesRepository

class GetGameUseCase(private val repository: GamesRepository) {
    suspend operator fun invoke(id: String): GameDomain? = repository.getGame(id)
}
