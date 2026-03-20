package app.loobby.feature.events.teams.data.remote

import app.loobby.feature.events.teams.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TeamsApiImpl(
    private val client: HttpClient
) : TeamsApi {

    override suspend fun listTeams(eventId: String): List<EventTeamResponse> =
        client.get("events/$eventId/teams").body()

    override suspend fun createTeam(eventId: String, request: CreateTeamRequest): EventTeamResponse =
        client.post("events/$eventId/teams") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun updateTeam(eventId: String, teamId: String, request: UpdateTeamRequest): EventTeamResponse =
        client.put("events/$eventId/teams/$teamId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun deleteTeam(eventId: String, teamId: String) {
        client.delete("events/$eventId/teams/$teamId")
    }

    override suspend fun addPlayer(eventId: String, teamId: String, request: AddPlayerToTeamRequest): EventTeamResponse =
        client.post("events/$eventId/teams/$teamId/players") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun updatePlayer(
        eventId: String, teamId: String, userId: String, request: UpdateTeamPlayerRequest
    ): EventTeamResponse =
        client.put("events/$eventId/teams/$teamId/players/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun removePlayer(eventId: String, teamId: String, userId: String): EventTeamResponse =
        client.delete("events/$eventId/teams/$teamId/players/$userId").body()

    override suspend fun autoGenerate(eventId: String, request: AutoGenerateTeamsRequest): List<EventTeamResponse> =
        client.post("events/$eventId/teams/auto-generate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}