package app.loobby.feature.events.data.repository

import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.remote.EventsApi
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.GameplayDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.model.SportDomain
import app.loobby.feature.events.domain.repository.EventsRepository
import io.ktor.client.plugins.*
import io.ktor.http.*

class EventsRepositoryImpl(
    private val api: EventsApi,
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : EventsRepository {

    override suspend fun getGroupEvents(groupId: String): List<EventDomain> =
        callWithRefresh { api.getGroupEvents(groupId).map { it.toDomain() } }

    override suspend fun confirmRsvp(
        eventId: String,
        status: RsvpStatus,
        isPaid: Boolean,
        obs: String?
    ): RsvpDomain = callWithRefresh {
        api.confirmRsvp(
            eventId,
            RsvpRequest(status = status.name, isPaid = isPaid, obs = obs)
        ).toDomain()
    }

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

    // ── Token refresh helper (same pattern as GroupsRepositoryImpl) ───────────

    private suspend fun <T> callWithRefresh(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: ClientRequestException) {
            if (e.response.status != HttpStatusCode.Unauthorized) throw e

            val tokens = tokenStorage.getTokens()
                ?: throw IllegalStateException("Not authenticated")

            val refreshed = authApi.refresh(tokens.refreshToken)

            tokenStorage.saveTokens(
                tokens.copy(
                    accessToken = refreshed.accessToken,
                    refreshToken = refreshed.refreshToken,
                    userId = refreshed.userId,
                    username = refreshed.username,
                    roles = refreshed.roles
                )
            )

            return block()
        }
    }
}