package app.loobby.feature.events.data.remote

import app.loobby.feature.events.data.model.CreateEventRequest
import app.loobby.feature.events.data.model.EventResponse
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.model.RsvpResponse

interface EventsApi {
    suspend fun getGroupEvents(groupId: String): List<EventResponse>
    suspend fun confirmRsvp(eventId: String, request: RsvpRequest): RsvpResponse
    suspend fun createGroupEvent(groupId: String, request: CreateEventRequest): EventResponse
    suspend fun createInstantEvent(request: CreateEventRequest): EventResponse
}