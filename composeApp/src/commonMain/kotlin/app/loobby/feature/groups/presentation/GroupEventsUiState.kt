package app.loobby.feature.groups.presentation

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.groups.domain.model.GroupEventFilter
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

data class GroupEventsUiState(
    val isLoading: Boolean = false,
    val allEvents: List<EventDomain> = emptyList(),
    val activeFilter: GroupEventFilter = GroupEventFilter.UPCOMING,
    val memberCount: Int? = null,
    val errorMessage: String? = null
) {
    val filteredEvents: List<EventDomain>
        get() = filterEvents(allEvents, activeFilter)

    /**
     * Quantidade de eventos em cada filtro, calculada a partir de [allEvents].
     * Usada para exibir o contador nos chips de filtro.
     */
    val filterCounts: Map<GroupEventFilter, Int>
        get() = GroupEventFilter.entries.associateWith { filter ->
            filterEvents(allEvents, filter).size
        }
}

internal fun filterEvents(
    events: List<EventDomain>,
    filter: GroupEventFilter
): List<EventDomain> {
    val now = Clock.System.now()

    return when (filter) {
        GroupEventFilter.TODAY -> events.filter { event ->
            val scheduled = runCatching {
                Instant.parse(event.scheduledDatetime)
            }.getOrNull() ?: return@filter false
            val date = scheduled.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            date == today
        }

        GroupEventFilter.UPCOMING -> events.filter { event ->
            val scheduled = runCatching {
                Instant.parse(event.scheduledDatetime)
            }.getOrNull() ?: return@filter false
            scheduled > now
        }

        GroupEventFilter.FINISHED -> events.filter { event ->
            val scheduled = runCatching {
                Instant.parse(event.scheduledDatetime)
            }.getOrNull() ?: return@filter false
            scheduled < now
        }

        GroupEventFilter.CONFIRMED -> events.filter { it.rsvpStatus == RsvpStatus.YES }
    }
}