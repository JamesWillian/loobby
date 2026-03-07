package app.loobby.feature.groups.presentation

import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.usecase.ConfirmRsvpUseCase
import app.loobby.feature.events.domain.usecase.GetGroupEventsUseCase
import app.loobby.feature.groups.domain.model.GroupEventFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupEventsViewModel(
    private val getGroupEvents: GetGroupEventsUseCase,
    private val confirmRsvp: ConfirmRsvpUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GroupEventsUiState())
    val uiState: StateFlow<GroupEventsUiState> = _uiState.asStateFlow()

    private var currentGroupId: String? = null

    fun loadEvents(groupId: String) {
        currentGroupId = groupId
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val events = getGroupEvents(groupId)
                _uiState.update { it.copy(isLoading = false, allEvents = events) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "Erro ao carregar eventos") }
            }
        }
    }

    fun setFilter(filter: GroupEventFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun rsvp(eventId: String, status: RsvpStatus, isPaid: Boolean = false, obs: String? = null) {
        val groupId = currentGroupId ?: return
        scope.launch {
            try {
                confirmRsvp(eventId, status, isPaid, obs)
                loadEvents(groupId)
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = t.message ?: "Erro ao confirmar presença") }
            }
        }
    }
}