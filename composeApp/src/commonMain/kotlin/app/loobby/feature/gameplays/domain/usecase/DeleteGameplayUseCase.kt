package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Exclui a gameplay, ativa ou encerrada: ela some para todo mundo. */
class DeleteGameplayUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
