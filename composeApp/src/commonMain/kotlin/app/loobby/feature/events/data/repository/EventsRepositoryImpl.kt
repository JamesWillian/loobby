package app.loobby.feature.events.data.repository

import app.loobby.feature.events.data.model.CreateEventRequest
import app.loobby.feature.events.data.model.CreateGameplayDetailsRequest
import app.loobby.feature.events.data.model.CreateSportDetailsRequest
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.remote.EventsApi
import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.GameplayDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.model.SportDomain
import app.loobby.feature.events.domain.repository.EventsRepository

class EventsRepositoryImpl(
    private val api: EventsApi
) : EventsRepository {

    override suspend fun getGroupEvents(groupId: String): List<EventDomain> =
        api.getGroupEvents(groupId).map { it.toDomain() }

    override suspend fun confirmRsvp(
        eventId: String,
        status: RsvpStatus,
        isPaid: Boolean,
        obs: String?
    ): RsvpDomain =
        api.confirmRsvp(
            eventId,
            RsvpRequest(status = status.name, isPaid = isPaid, obs = obs)
        ).toDomain()

    override suspend fun createGroupEvent(input: CreateEventInput): EventDomain =
        api.createGroupEvent(
            groupId = input.groupId,
            request = CreateEventRequest(
                eventType = input.eventType.name,
                name = input.name,
                description = input.description,
                scheduledDatetime = input.scheduledDatetime,
                gameplay = input.gameplay?.let {
                    CreateGameplayDetailsRequest(gameName = it.gameName, gameId = it.gameId)
                },
                sport = input.sport?.let {
                    CreateSportDetailsRequest(
                        durationMinutes = it.durationMinutes,
                        arena = it.arena,
                        pricePerPlayer = it.pricePerPlayer,
                        maxPlayers = it.maxPlayers,
                        acceptReserve = it.acceptReserve
                    )
                }
            )
        ).toDomain()

    // ── Mappers ──────────────────────────────────────────────────────────────

    private fun app.loobby.feature.events.data.model.EventResponse.toDomain() = EventDomain(
        id = id,
        eventType = runCatching { EventType.valueOf(eventType) }.getOrDefault(EventType.SPORT),
        groupId = groupId,
        isInstant = isInstant,
        ownerId = ownerId,
        scheduledDatetime = scheduledDatetime,
        name = name,
        description = description,
        inviteCode = inviteCode,
        createdAt = createdAt,
        gameplay = gameplay?.let { GameplayDomain(it.gameId, it.gameName) },
        sport = sport?.let {
            SportDomain(it.durationMinutes, it.arena, it.pricePerPlayer, it.maxPlayers, it.acceptReserve)
        },
        rsvpStatus = runCatching { rsvpStatus?.let { RsvpStatus.valueOf(it) } }.getOrNull()
    )

    private fun app.loobby.feature.events.data.model.RsvpResponse.toDomain() = RsvpDomain(
        eventId = eventId,
        userId = userId,
        status = runCatching { RsvpStatus.valueOf(status) }.getOrDefault(RsvpStatus.YES),
        isPaid = isPaid,
        obs = obs,
        username = username,
        displayname = displayname,
        avatarUrl = avatarUrl,
        isOwner = isOwner
    )

}