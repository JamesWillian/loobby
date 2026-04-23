package app.loobby.feature.events.domain.usecase

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.repository.EventsRepository

class GetEventByInviteUseCase(private val repository: EventsRepository) {
    suspend operator fun invoke(inviteCode: String): EventDomain =
        repository.getEventByInvite(inviteCode)
}
