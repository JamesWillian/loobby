package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.UpdateEventInput
import app.loobby.feature.events.domain.repository.EventsRepository

class UpdateEventUseCase(
    private val repo: EventsRepository
) {
    suspend operator fun invoke(eventId: String, input: UpdateEventInput): EventDomain =
        repo.updateEvent(eventId, input)
}