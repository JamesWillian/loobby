package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.repository.EventsRepository

class GetEventByIdUseCase(private val repo: EventsRepository) {
    suspend operator fun invoke(eventId: String) = repo.getEventById(eventId)
}