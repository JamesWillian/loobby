package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.model.GameplayChanges
import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

class UpdateGameplayUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String, changes: GameplayChanges) = repository.update(id, changes)
}
