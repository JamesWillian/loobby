package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Denuncia um comentário. O motivo é opcional: em branco, vai sem motivo. */
class ReportCommentUseCase(private val repository: GameplaysRepository) {

    suspend operator fun invoke(gameplayId: String, commentId: String, reason: String?) {
        val text = reason?.trim()?.takeIf { it.isNotEmpty() }
        repository.reportComment(gameplayId, commentId, text)
    }
}
