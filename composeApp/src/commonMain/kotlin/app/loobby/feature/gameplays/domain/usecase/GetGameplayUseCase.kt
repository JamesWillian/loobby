package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

class GetGameplayUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String) = repository.gameplay(id)
}
