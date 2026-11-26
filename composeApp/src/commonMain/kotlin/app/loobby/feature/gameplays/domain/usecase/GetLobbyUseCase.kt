package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** As gameplays ativas da pessoa, de todos os grupos e as únicas. */
class GetLobbyUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke() = repository.lobby()
}
