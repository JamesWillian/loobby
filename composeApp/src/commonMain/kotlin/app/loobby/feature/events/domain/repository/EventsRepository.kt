package app.loobby.feature.events.domain.repository

import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus

interface EventsRepository {
    suspend fun getGroupEvents(groupId: String): List<EventDomain>
    suspend fun upsertRsvp(eventId: String, status: RsvpStatus, isPaid: Boolean, obs: String?): RsvpDomain
    suspend fun createGroupEvent(groupId: String, input: CreateEventInput): EventDomain
    suspend fun createInstantEvent(input: CreateEventInput): EventDomain
    suspend fun getEventById(eventId: String): EventDomain
    suspend fun listRsvps(eventId: String): List<RsvpDomain>
}