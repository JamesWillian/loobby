package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: EventDomain? = null,
    val rsvps: List<RsvpDomain> = emptyList(),
    val isRsvpLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPaid: Boolean = false,
    val obs: String = "",
    val isObsSaved: Boolean = false
) {
    val rsvpsByStatus: Map<RsvpStatus, List<RsvpDomain>>
        get() = rsvps.groupBy { it.status }
}