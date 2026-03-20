package app.loobby.feature.events.teams.data.repository

import app.loobby.feature.events.teams.data.mapper.toDomain
import app.loobby.feature.events.teams.data.model.*
import app.loobby.feature.events.teams.data.remote.TeamsApi
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class TeamsRepositoryImpl(
    private val api: TeamsApi
) : TeamsRepository {

    override suspend fun listTeams(eventId: String): List<TeamDomain> =
        api.listTeams(eventId).map { it.toDomain() }

    override suspend fun createTeam(
        eventId: String, name: String, color: String?, playerIds: List<String>
    ): TeamDomain {
        val request = CreateTeamRequest(
            name = name,
            color = color,
            players = playerIds.map { TeamPlayerRequest(userId = it) }
        )
        return api.createTeam(eventId, request).toDomain()
    }

    override suspend fun updateTeam(
        eventId: String, teamId: String, name: String?, color: String?, order: Int?
    ): TeamDomain {
        val request = UpdateTeamRequest(name = name, color = color, order = order)
        return api.updateTeam(eventId, teamId, request).toDomain()
    }

    override suspend fun deleteTeam(eventId: String, teamId: String) =
        api.deleteTeam(eventId, teamId)

    override suspend fun addPlayer(eventId: String, teamId: String, userId: String): TeamDomain {
        val request = AddPlayerToTeamRequest(userId = userId)
        return api.addPlayer(eventId, teamId, request).toDomain()
    }

    override suspend fun removePlayer(eventId: String, teamId: String, userId: String): TeamDomain =
        api.removePlayer(eventId, teamId, userId).toDomain()

    override suspend fun movePlayer(
        eventId: String, fromTeamId: String, userId: String, toTeamId: String
    ): TeamDomain {
        val request = UpdateTeamPlayerRequest(newTeamId = toTeamId)
        return api.updatePlayer(eventId, fromTeamId, userId, request).toDomain()
    }

    override suspend fun autoGenerate(
        eventId: String, teamCount: Int?, teamSize: Int?
    ): List<TeamDomain> {
        val request = AutoGenerateTeamsRequest(teamSize = teamSize, teamCount = teamCount)
        return api.autoGenerate(eventId, request).map { it.toDomain() }
    }
}