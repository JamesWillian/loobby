package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Troca os votos da pessoa na rodada aberta; lista vazia tira o voto. */
class VoteUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String, votingGameIds: List<String>) = repository.vote(id, votingGameIds)
}
