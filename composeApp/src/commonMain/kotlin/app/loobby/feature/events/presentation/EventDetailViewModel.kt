package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.usecase.UpsertRsvpUseCase
import app.loobby.feature.events.domain.usecase.GetEventByIdUseCase
import app.loobby.feature.events.domain.usecase.ListEventRsvpsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EventDetailViewModel(
    private val getEventById: GetEventByIdUseCase,
    private val listRsvps: ListEventRsvpsUseCase,
    private val upsertRsvp: UpsertRsvpUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    fun load(eventId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val event = getEventById(eventId)
                val rsvps = listRsvps(eventId)
                _uiState.update { it.copy(isLoading = false, event = event, rsvps = rsvps) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message) }
            }
        }
    }

    fun rsvp(eventId: String, status: RsvpStatus, isPaid: Boolean = false, obs: String? = null) {
        scope.launch {
            _uiState.update { it.copy(isRsvpLoading = true) }
            try {
                upsertRsvp(eventId, status, isPaid, obs)
                // Reload both event (updated rsvpStatus) and rsvp list
                val event = getEventById(eventId)
                val rsvps = listRsvps(eventId)
                _uiState.update {
                    it.copy(isRsvpLoading = false, event = event, rsvps = rsvps)
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isRsvpLoading = false, errorMessage = t.message ?: "Erro ao confirmar presença")
                }
            }
        }
    }
}