package app.loobby.feature.events.teams.domain.repository

import app.loobby.feature.events.teams.domain.model.TeamDomain

interface TeamsRepository {
    suspend fun listTeams(eventId: String): List<TeamDomain>
    suspend fun createTeam(eventId: String, name: String, color: String?, playerIds: List<String>): TeamDomain
    suspend fun updateTeam(eventId: String, teamId: String, name: String?, color: String?, order: Int?): TeamDomain
    suspend fun deleteTeam(eventId: String, teamId: String)
    suspend fun addPlayer(eventId: String, teamId: String, userId: String): TeamDomain
    suspend fun removePlayer(eventId: String, teamId: String, userId: String): TeamDomain
    suspend fun movePlayer(eventId: String, fromTeamId: String, userId: String, toTeamId: String): TeamDomain
    suspend fun autoGenerate(
        eventId: String,
        teamCount: Int?,
        teamSize: Int?,
        includeReserves: Boolean
    ): List<TeamDomain>
}