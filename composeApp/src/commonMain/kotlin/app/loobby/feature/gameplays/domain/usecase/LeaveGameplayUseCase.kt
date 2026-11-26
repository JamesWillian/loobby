package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Sai da gameplay única. */
class LeaveGameplayUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String) = repository.leave(id)
}
