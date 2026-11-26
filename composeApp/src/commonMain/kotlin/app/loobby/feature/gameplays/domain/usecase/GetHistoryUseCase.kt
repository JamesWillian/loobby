package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** O Histórico: as encerradas em que a pessoa marcou Vou, da mais recente. */
class GetHistoryUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(before: String? = null) = repository.history(before)
}
