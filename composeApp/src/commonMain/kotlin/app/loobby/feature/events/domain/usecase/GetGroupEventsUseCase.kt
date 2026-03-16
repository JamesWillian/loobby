package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.repository.EventsRepository

class GetGroupEventsUseCase(private val repository: EventsRepository) {
    suspend operator fun invoke(groupId: String): List<EventDomain> =
        repository.getGroupEvents(groupId)
}