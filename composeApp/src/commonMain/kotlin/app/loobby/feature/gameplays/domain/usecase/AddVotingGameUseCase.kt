package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

class AddVotingGameUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String, game: GameChoice) = repository.addVotingGame(id, game)
}
