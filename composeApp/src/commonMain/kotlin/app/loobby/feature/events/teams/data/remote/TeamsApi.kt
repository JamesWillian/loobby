package app.loobby.feature.events.teams.data.remote

import app.loobby.feature.events.teams.data.model.*

interface TeamsApi {
    suspend fun listTeams(eventId: String): List<EventTeamResponse>
    suspend fun createTeam(eventId: String, request: CreateTeamRequest): EventTeamResponse
    suspend fun updateTeam(eventId: String, teamId: String, request: UpdateTeamRequest): EventTeamResponse
    suspend fun deleteTeam(eventId: String, teamId: String)
    suspend fun addPlayer(eventId: String, teamId: String, request: AddPlayerToTeamRequest): EventTeamResponse
    suspend fun updatePlayer(eventId: String, teamId: String, userId: String, request: UpdateTeamPlayerRequest): EventTeamResponse
    suspend fun removePlayer(eventId: String, teamId: String, userId: String): EventTeamResponse
    suspend fun autoGenerate(eventId: String, request: AutoGenerateTeamsRequest): List<EventTeamResponse>
}