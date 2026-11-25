package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Tira o jogo da cédula, com os votos nele. */
class RemoveVotingGameUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String, votingGameId: String) = repository.removeVotingGame(id, votingGameId)
}
