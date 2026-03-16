package app.loobby.feature.events.data.repository

import app.loobby.feature.events.data.mapper.toDomain
import app.loobby.feature.events.data.mapper.toRequest
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.remote.EventsApi
import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.repository.EventsRepository

class EventsRepositoryImpl(
    private val api: EventsApi
) : EventsRepository {

    override suspend fun getGroupEvents(groupId: String): List<EventDomain> =
        api.getGroupEvents(groupId).map { it.toDomain() }

    override suspend fun upsertRsvp(
        eventId: String,
        status: RsvpStatus,
        isPaid: Boolean,
        obs: String?
    ): RsvpDomain =
        api.upsertRsvp(
            eventId = eventId,
            request = RsvpRequest(status = status.name, isPaid = isPaid, obs = obs)
        ).toDomain()

    override suspend fun createGroupEvent(groupId: String, input: CreateEventInput): EventDomain =
        api.createGroupEvent(
            groupId = groupId,
            request = input.toRequest()
        ).toDomain()

    override suspend fun createInstantEvent(input: CreateEventInput): EventDomain =
        api.createInstantEvent(input.toRequest()).toDomain()

    override suspend fun getEventById(eventId: String): EventDomain =
        api.getEventById(eventId).toDomain()

    override suspend fun listRsvps(eventId: String): List<RsvpDomain> =
        api.listRsvps(eventId).map { it.toDomain() }

}