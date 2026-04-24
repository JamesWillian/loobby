package app.loobby.feature.events.data.cache

import app.loobby.db.RsvpEntity
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus

fun RsvpEntity.toDomain(): RsvpDomain = RsvpDomain(
    eventId = event_id,
    userId = user_id,
    status = runCatching { RsvpStatus.valueOf(status) }.getOrDefault(RsvpStatus.PENDING),
    isPaid = is_paid == 1L,
    obs = obs,
    username = username,
    displayname = displayname,
    avatarUrl = avatar_url,
    isOwner = is_owner == 1L
)
