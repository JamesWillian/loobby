package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.repository.EventsRepository

class GetGroupEventsUseCase(private val repository: EventsRepository) {
    suspend operator fun invoke(groupId: String): List<EventDomain> =
        repository.getGroupEvents(groupId)
}

class ConfirmRsvpUseCase(private val repository: EventsRepository) {
    suspend operator fun invoke(
        eventId: String,
        status: RsvpStatus,
        isPaid: Boolean = false,
        obs: String? = null
    ): RsvpDomain = repository.confirmRsvp(eventId, status, isPaid, obs)
}