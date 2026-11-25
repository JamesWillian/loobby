package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** Entra na única pelo convite, como Pendente. */
class JoinGameplayByInviteUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(code: String) = repository.joinByInvite(code)
}
