package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.repository.EventsRepository

class UpsertRsvpUseCase(private val repository: EventsRepository) {
    suspend operator fun invoke(
        eventId: String,
        status: RsvpStatus,
        isPaid: Boolean = false,
        obs: String? = null
    ): RsvpDomain = repository.upsertRsvp(eventId, status, isPaid, obs)
}