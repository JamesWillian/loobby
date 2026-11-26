package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** As Finalizadas do grupo: as encerradas, da mais recente. */
class GetEndedGroupGameplaysUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(groupId: String, before: String? = null) = repository.endedInGroup(groupId, before)
}
