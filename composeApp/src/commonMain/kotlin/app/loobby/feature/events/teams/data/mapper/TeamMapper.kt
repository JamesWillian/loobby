package app.loobby.feature.events.teams.data.mapper

import app.loobby.feature.events.teams.data.model.EventTeamResponse
import app.loobby.feature.events.teams.data.model.TeamPlayerResponse
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.model.TeamPlayerDomain

fun EventTeamResponse.toDomain() = TeamDomain(
    id = id,
    eventId = eventId,
    order = order,
    name = name,
    color = color,
    players = players.map { it.toDomain() }
)

fun TeamPlayerResponse.toDomain() = TeamPlayerDomain(
    userId = userId,
    role = role,
    username = username,
    displayname = displayname,
    avatarUrl = avatarUrl
)