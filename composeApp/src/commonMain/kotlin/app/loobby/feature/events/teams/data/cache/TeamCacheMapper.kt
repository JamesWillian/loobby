package app.loobby.feature.events.teams.data.cache

import app.loobby.db.TeamEntity
import app.loobby.db.TeamPlayerEntity
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.model.TeamPlayerDomain

/**
 * Converte um par (linha de TeamEntity + lista de linhas TeamPlayerEntity) em
 * TeamDomain. Separado em duas etapas porque SQLDelight entrega as linhas das
 * duas tabelas em queries distintas — o repositório monta o agregado.
 */
fun TeamEntity.toDomain(players: List<TeamPlayerDomain>): TeamDomain = TeamDomain(
    id = id,
    eventId = event_id,
    order = team_order.toInt(),
    name = name,
    color = color,
    players = players
)

fun TeamPlayerEntity.toDomain(): TeamPlayerDomain = TeamPlayerDomain(
    userId = user_id,
    role = role,
    username = username,
    displayname = displayname,
    avatarUrl = avatar_url
)
