package app.loobby.feature.events.data.remote

import app.loobby.feature.events.data.model.EventResponse
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.model.RsvpResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class EventsApiImpl(
    private val client: HttpClient
) : EventsApi {

    override suspend fun getGroupEvents(groupId: String): List<EventResponse> =
        client.get("groups/$groupId/events").body()

    override suspend fun confirmRsvp(eventId: String, request: RsvpRequest): RsvpResponse =
        client.put("events/$eventId/rsvps") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}