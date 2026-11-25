package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository

/** A prévia do convite de uma gameplay única. */
class GetGameplayInviteUseCase(private val repository: GameplaysRepository) {
    suspend operator fun invoke(code: String) = repository.invitePreview(code)
}
