package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

class DeleteCommentUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(gameplayId: String, commentId: String) = repository.deleteComment(gameplayId, commentId)
}
