package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.repository.EventsRepository

class CreateInstantEventUseCase(
    private val repository: EventsRepository
) {
    suspend operator fun invoke(input: CreateEventInput): EventDomain {
        return repository.createInstantEvent(input)
    }
}