package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Vou, Não vou ou Pendente. Sair do Vou apaga os votos da rodada aberta. */
class RespondToGameplayUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(id: String, status: ParticipationStatus) = repository.respond(id, status)
}
