package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.model.GameplayComment
import app.loobby.feature.gameplays.domain.model.GameplayRules
import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Publica um comentário, sem os espaços das pontas. */
class PostCommentUseCase(private val repository: GameplaysRepository) {

    suspend operator fun invoke(gameplayId: String, body: String): GameplayComment {
        val text = body.trim()
        require(text.isNotEmpty()) { "Escreva o comentário" }
        require(text.length <= GameplayRules.MAX_COMMENT) { "Use até ${GameplayRules.MAX_COMMENT} caracteres" }
        return repository.postComment(gameplayId, text)
    }
}
