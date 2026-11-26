package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.model.GameplayDraft
import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Cria a gameplay do grupo ou, sem grupo, a única, já com o convite. */
class CreateGameplayUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(draft: GameplayDraft) = repository.create(draft)
}
