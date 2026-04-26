package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.repository.EventsRepository

/**
 * Remove a presença do usuário em um evento (apaga o registro de event_rsvps).
 * `userId` é necessário aqui apenas para que o repositório consiga limpar a
 * linha correta do cache local — o backend resolve o usuário a partir do JWT.
 */
class DeleteMyRsvpUseCase(
    private val repo: EventsRepository
) {
    suspend operator fun invoke(eventId: String, userId: String) =
        repo.deleteMyRsvp(eventId, userId)
}
