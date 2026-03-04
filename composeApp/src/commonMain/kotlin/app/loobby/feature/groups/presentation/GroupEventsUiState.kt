package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.domain.model.GroupEventFilter
import app.loobby.feature.groups.domain.model.GroupEventUi

data class GroupEventsUiState(
    val isLoading: Boolean = false,
    val groupName: String = "",
    val activeFilter: GroupEventFilter = GroupEventFilter.TODAY,
    val events: List<GroupEventUi> = emptyList(),
    val errorMessage: String? = null
) {
    val filteredEvents: List<GroupEventUi>
        get() = events.filter { it.filter == activeFilter }
}