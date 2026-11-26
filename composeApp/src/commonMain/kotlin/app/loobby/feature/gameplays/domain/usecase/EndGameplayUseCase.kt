package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Encerra a gameplay: ela vira só leitura. */
class EndGameplayUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String) = repository.end(id)
}
