package app.loobby.feature.events.data.remote

import app.loobby.feature.events.data.model.CreateEventRequest
import app.loobby.feature.events.data.model.EventResponse
import app.loobby.feature.events.data.model.RsvpResponse
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.model.UpdateEventRequest // import novo DTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

class EventsApiImpl(
    private val client: HttpClient
) : EventsApi {

    override suspend fun getGroupEvents(groupId: String): List<EventResponse> =
        client.get("groups/$groupId/events").body()

    override suspend fun upsertRsvp(eventId: String, request: RsvpRequest): RsvpResponse =
        client.put("events/$eventId/rsvps") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun createGroupEvent(groupId: String, request: CreateEventRequest): EventResponse =
        client.post("groups/$groupId/events") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun createInstantEvent(request: CreateEventRequest): EventResponse =
        client.post("events/instant") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun getEventById(eventId: String): EventResponse =
        client.get("/events/$eventId").body()

    override suspend fun getEventByInvite(inviteCode: String): EventResponse =
        client.get("/events/invite/$inviteCode").body()

    override suspend fun listRsvps(eventId: String): List<RsvpResponse> =
        client.get("/events/$eventId/rsvps").body()

    override suspend fun getMyRsvp(eventId: String): RsvpResponse? {
        val response = client.get("/events/$eventId/rsvps/me")
        val text = response.bodyAsText()
        if (text.isBlank()) return null

        return kotlinx.serialization.json.Json.decodeFromString(text)
    }

    // PUT /events/{eventId} — atualizar evento
    override suspend fun updateEvent(eventId: String, request: UpdateEventRequest): EventResponse =
        client.put("/events/$eventId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // DELETE /events/{eventId} — excluir evento (204 NO_CONTENT)
    override suspend fun deleteEvent(eventId: String) {
        client.delete("/events/$eventId")
    }
}