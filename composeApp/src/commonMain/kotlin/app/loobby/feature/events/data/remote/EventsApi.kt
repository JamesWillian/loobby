package app.loobby.feature.events.data.remote

import app.loobby.feature.events.data.model.CreateEventRequest
import app.loobby.feature.events.data.model.EventResponse
import app.loobby.feature.events.data.model.RsvpResponse
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.model.UpdateEventRequest // import novo DTO

interface EventsApi {
    suspend fun getGroupEvents(groupId: String): List<EventResponse>
    suspend fun upsertRsvp(eventId: String, request: RsvpRequest): RsvpResponse
    suspend fun createGroupEvent(groupId: String, request: CreateEventRequest): EventResponse
    suspend fun createInstantEvent(request: CreateEventRequest): EventResponse
    suspend fun getEventById(eventId: String): EventResponse
    suspend fun listRsvps(eventId: String): List<RsvpResponse>
    suspend fun getMyRsvp(eventId: String): RsvpResponse?
    suspend fun updateEvent(eventId: String, request: UpdateEventRequest): EventResponse
    suspend fun deleteEvent(eventId: String)
}