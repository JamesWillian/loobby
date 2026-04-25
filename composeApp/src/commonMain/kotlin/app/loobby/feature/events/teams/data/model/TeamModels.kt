package app.loobby.feature.events.teams.data.model

import kotlinx.serialization.Serializable

// ─── Responses ───────────────────────────────────────────────────────────────

@Serializable
data class EventTeamResponse(
    val id: String,
    val eventId: String,
    val order: Int,
    val name: String,
    val color: String? = null,
    val players: List<TeamPlayerResponse> = emptyList()
)

@Serializable
data class TeamPlayerResponse(
    val userId: String,
    val role: String? = null,
    val username: String,
    val displayname: String? = null,
    val avatarUrl: String? = null
)

// ─── Requests ────────────────────────────────────────────────────────────────

@Serializable
data class CreateTeamRequest(
    val name: String,
    val color: String? = null,
    val order: Int? = null,
    val players: List<TeamPlayerRequest> = emptyList()
)

@Serializable
data class TeamPlayerRequest(
    val userId: String,
    val role: String? = null
)

@Serializable
data class UpdateTeamRequest(
    val name: String? = null,
    val color: String? = null,
    val order: Int? = null
)

@Serializable
data class AddPlayerToTeamRequest(
    val userId: String,
    val role: String? = null
)

@Serializable
data class UpdateTeamPlayerRequest(
    val newTeamId: String? = null,
    val role: String? = null
)

@Serializable
data class AutoGenerateTeamsRequest(
    val teamSize: Int? = null,
    val teamCount: Int? = null,
    val includeReserves: Boolean = true
)