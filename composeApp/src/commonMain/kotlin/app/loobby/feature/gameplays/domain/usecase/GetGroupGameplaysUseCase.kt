package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** As gameplays ativas de um grupo. */
class GetGroupGameplaysUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(groupId: String) = repository.activeInGroup(groupId)
}
