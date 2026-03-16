package app.loobby.feature.events.data.mapper

import app.loobby.feature.events.data.model.CreateEventRequest
import app.loobby.feature.events.data.model.CreateGameplayDetailsRequest
import app.loobby.feature.events.data.model.CreateSportDetailsRequest
import app.loobby.feature.events.data.model.EventResponse
import app.loobby.feature.events.data.model.RsvpResponse
import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.GameplayDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.model.SportDomain

fun CreateEventInput.toRequest() = CreateEventRequest(
    eventType = eventType.name,
    name = name,
    description = description,
    scheduledDatetime = scheduledDatetime,
    gameplay = gameplay?.let {
        CreateGameplayDetailsRequest(gameName = it.gameName, gameId = it.gameId)
    },
    sport = sport?.let {
        CreateSportDetailsRequest(
            durationMinutes = it.durationMinutes,
            arena = it.arena,
            pricePerPlayer = it.pricePerPlayer,
            maxPlayers = it.maxPlayers,
            acceptReserve = it.acceptReserve
        )
    }
)

fun EventResponse.toDomain() = EventDomain(
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
    rsvpStatus = runCatching { rsvpStatus?.let { RsvpStatus.valueOf(it) } }.getOrNull(),
    confirmedCount = confirmedCount,
    confirmedAvatars = confirmedAvatars
)

fun RsvpResponse.toDomain() = RsvpDomain(
    eventId = eventId,
    userId = userId,
    status = runCatching { RsvpStatus.valueOf(status) }.getOrDefault(RsvpStatus.PENDING),
    isPaid = isPaid,
    obs = obs,
    username = username,
    displayname = displayname,
    avatarUrl = avatarUrl,
    isOwner = isOwner
)