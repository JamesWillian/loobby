package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Abre uma rodada só com os jogos empatados no 1º lugar. */
class OpenTiebreakUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String) = repository.openTiebreak(id)
}
