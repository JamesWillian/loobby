package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.usecase.UpsertRsvpUseCase
import app.loobby.feature.events.domain.usecase.GetEventByIdUseCase
import app.loobby.feature.events.domain.usecase.GetMyRsvpUseCase
import app.loobby.feature.events.domain.usecase.ListEventRsvpsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EventDetailViewModel(
    private val getEventById: GetEventByIdUseCase,
    private val listRsvps: ListEventRsvpsUseCase,
    private val upsertRsvp: UpsertRsvpUseCase,
    private val getMyRsvp: GetMyRsvpUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    // job para cancelar o debounce de obs a cada nova digitação
    private var obsDebounceJob: Job? = null

    fun load(eventId: String) {
        scope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }
            try {
                val event = getEventById(eventId)
                val rsvps = listRsvps(eventId)
                val myRsvp = getMyRsvp(eventId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        event = event,
                        rsvps = rsvps,
                        isPaid = myRsvp?.isPaid ?: false,
                        obs = myRsvp?.obs ?: ""
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message) }
            }
        }
    }

    fun rsvp(eventId: String, status: RsvpStatus) {
        scope.launch {
            _uiState.update { it.copy(isRsvpLoading = true) }
            try {
                val isPaid = _uiState.value.isPaid
                val obs = _uiState.value.obs.takeIf { it.isNotBlank() }

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

    fun setPaid(eventId: String, isPaid: Boolean) {
        _uiState.update { it.copy(isPaid = isPaid) }

        val status = _uiState.value.event?.rsvpStatus ?: return
        scope.launch {
            try {
                val state = _uiState.value
                upsertRsvp(eventId, status, isPaid, state.obs.takeIf { it.isNotBlank() })
                val rsvps = listRsvps(eventId)
                _uiState.update { it.copy(rsvps = rsvps) }
            } catch (t: Throwable) {
                // Reverte em caso de erro
                _uiState.update { it.copy(isPaid = !isPaid, errorMessage = t.message) }
            }
        }
    }

    // Digitação da obs → debounce de 5s antes de efetivar
    fun setObs(eventId: String, obs: String) {
        _uiState.update { it.copy(obs = obs) }

        val status = _uiState.value.event?.rsvpStatus ?: return

        // Cancela o job anterior e agenda um novo
        obsDebounceJob?.cancel()
        obsDebounceJob = scope.launch {
            delay(5_000)
            try {
                val state = _uiState.value
                upsertRsvp(eventId, status, state.isPaid, obs.takeIf { it.isNotBlank() })
                val rsvps = listRsvps(eventId)
                // marca como salvo e reseta após 3s
                _uiState.update { it.copy(rsvps = rsvps, isObsSaved = true) }
                delay(3_000)
                _uiState.update { it.copy(isObsSaved = false) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = t.message) }
            }
        }
    }

}