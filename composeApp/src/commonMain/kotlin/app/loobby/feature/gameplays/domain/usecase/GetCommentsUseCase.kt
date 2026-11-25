package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

class GetCommentsUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(gameplayId: String, before: String? = null) = repository.comments(gameplayId, before)
}
