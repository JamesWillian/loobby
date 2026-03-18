package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.repository.EventsRepository

class GetMyRsvpUseCase(private val repo: EventsRepository) {
    // Retorna null se o usuário ainda não deu RSVP neste evento
    suspend operator fun invoke(eventId: String): RsvpDomain? = repo.getMyRsvp(eventId)
}