package app.loobby.feature.events.presentation

import app.loobby.core.media.ImagePrefetcher
import app.loobby.feature.auth.domain.repository.AuthRepository // CHANGED: import
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.usecase.DeleteEventUseCase // CHANGED: import
import app.loobby.feature.events.domain.usecase.DeleteMyRsvpUseCase // CHANGED: import (remover presença)
import app.loobby.feature.events.domain.usecase.UpsertRsvpUseCase
import app.loobby.feature.events.domain.usecase.GetEventByIdUseCase
import app.loobby.feature.events.domain.usecase.GetMyRsvpUseCase
import app.loobby.feature.events.domain.usecase.ListEventRsvpsUseCase
import app.loobby.feature.games.domain.usecase.GetGameUseCase // imagem do jogo (RAWG)
import app.loobby.feature.groups.domain.usecase.ListGroupMembersUseCase // CHANGED: import
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
    private val getMyRsvp: GetMyRsvpUseCase,
    private val deleteEvent: DeleteEventUseCase,            // CHANGED: novo
    private val deleteMyRsvp: DeleteMyRsvpUseCase,           // CHANGED: novo (remover presença)
    private val authRepository: AuthRepository,              // CHANGED: novo
    private val listGroupMembers: ListGroupMembersUseCase,   // CHANGED: novo
    private val getGame: GetGameUseCase,                     // imagem/detalhes do jogo (RAWG)
    private val imagePrefetcher: ImagePrefetcher
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    // job para cancelar o debounce de obs a cada nova digitação
    private var obsDebounceJob: Job? = null

    fun load(eventId: String) {
        scope.launch {
            _uiState.update {
                EventDetailUiState(isLoading = true)
            }
            try {
                // CHANGED: busca userId atual
                val userId = authRepository.currentUserId()

                val event = getEventById(eventId)
                val rsvps = listRsvps(eventId)
                val myRsvp = getMyRsvp(eventId)

                // CHANGED: verifica se é dono do grupo (se evento pertence a um grupo)
                val isGroupOwner = if (event.groupId != null && userId != null) {
                    runCatching {
                        val members = listGroupMembers(event.groupId)
                        members.any { it.userId == userId && it.isOwner }
                    }.getOrDefault(false)
                } else false

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        event = event,
                        rsvps = rsvps,
                        isPaid = myRsvp?.isPaid ?: false,
                        obs = myRsvp?.obs ?: "",
                        currentUserId = userId,       // CHANGED
                        isGroupOwner = isGroupOwner    // CHANGED
                    )
                }

                // Aquece o disk cache: avatares dos RSVPs + avatares dos confirmados
                // que vêm na própria resposta do evento. Isso garante que a tela de
                // detalhe continue visualmente completa se o usuário ficar offline.
                imagePrefetcher.prefetch(rsvps.map { it.avatarUrl })
                event.confirmedAvatars?.let { imagePrefetcher.prefetch(it) }

                // Carrega a capa do jogo (RAWG) para o hero do detalhe — só quando
                // o evento é gameplay e tem um id de jogo do catálogo. Best-effort:
                // falha/offline apenas deixa o hero usar o placeholder.
                event.gameplay?.gameId?.let { gameId ->
                    runCatching { getGame(gameId) }.getOrNull()?.let { game ->
                        imagePrefetcher.prefetch(listOf(game.backgroundImage))
                        _uiState.update { it.copy(game = game) }
                    }
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

    // Digitação da obs → debounce de 3s antes de efetivar
    fun setObs(eventId: String, obs: String) {
        _uiState.update { it.copy(obs = obs) }

        val status = _uiState.value.event?.rsvpStatus ?: return

        // Cancela o job anterior e agenda um novo
        obsDebounceJob?.cancel()
        obsDebounceJob = scope.launch {
            delay(3_000)
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

    // CHANGED: excluir evento
    fun delete(eventId: String) {
        scope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            try {
                deleteEvent(eventId)
                _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isDeleting = false, errorMessage = t.message ?: "Erro ao excluir evento")
                }
            }
        }
    }

    // CHANGED: remover a própria presença (RSVP) do evento.
    // Permite remover mesmo em eventos finalizados — útil pra "limpar" o histórico.
    // Após sucesso, recarrega evento (rsvpStatus passa a null) e lista de RSVPs.
    fun removeMyRsvp(eventId: String) {
        scope.launch {
            val userId = _uiState.value.currentUserId
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Usuário não identificado") }
                return@launch
            }
            _uiState.update { it.copy(isRemovingRsvp = true, errorMessage = null) }
            try {
                deleteMyRsvp(eventId, userId)
                // recarrega tudo para refletir a ausência do RSVP do usuário e a
                // contagem atualizada de confirmados.
                val event = getEventById(eventId)
                val rsvps = listRsvps(eventId)
                _uiState.update {
                    it.copy(
                        isRemovingRsvp = false,
                        removeRsvpSuccess = true,
                        event = event,
                        rsvps = rsvps,
                        // limpa estado local relacionado ao RSVP
                        isPaid = false,
                        obs = ""
                    )
                }
                // reseta a flag de sucesso após curto delay (evita reaparecer snackbar)
                delay(2_000)
                _uiState.update { it.copy(removeRsvpSuccess = false) }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isRemovingRsvp = false,
                        errorMessage = t.message ?: "Erro ao remover presença"
                    )
                }
            }
        }
    }

    // CHANGED: mostrar/esconder sheet de edição
    fun showEditSheet() {
        _uiState.update { it.copy(showEditSheet = true) }
    }

    fun hideEditSheet() {
        _uiState.update { it.copy(showEditSheet = false) }
    }

    // CHANGED: recarrega evento após atualização
    fun onEventUpdated(eventId: String) {
        _uiState.update { it.copy(showEditSheet = false, updateSuccess = true) }
        load(eventId)
        // reseta flag após curto delay
        scope.launch {
            delay(2_000)
            _uiState.update { it.copy(updateSuccess = false) }
        }
    }
}