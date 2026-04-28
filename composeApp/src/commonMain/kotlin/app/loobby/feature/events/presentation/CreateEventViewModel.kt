package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.CreateGameplayInput
import app.loobby.feature.events.domain.model.CreateSportInput
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.UpdateEventInput // import
import app.loobby.feature.events.domain.usecase.CreateGroupEventUseCase
import app.loobby.feature.events.domain.usecase.CreateInstantEventUseCase
import app.loobby.feature.events.domain.usecase.UpdateEventUseCase // import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant // import
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime // import
import kotlin.time.Clock

class CreateEventViewModel(
    private val createGroupEvent: CreateGroupEventUseCase,
    private val createInstantEvent: CreateInstantEventUseCase,
    private val updateEvent: UpdateEventUseCase // novo parâmetro
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun selectType(type: EventType) = _uiState.update { it.copy(selectedType = type) }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, isDirty = true) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v, isDirty = true) }

    fun onDateChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(scheduledDate = digits, isDirty = true) }
    }

    fun onTimeChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(scheduledTime = digits, isDirty = true) }
    }

    // Sport
    fun onDurationChange(v: String) = _uiState.update { it.copy(durationMinutes = v, isDirty = true) }
    fun onArenaChange(v: String) = _uiState.update { it.copy(arena = v, isDirty = true) }
    fun onPriceChange(v: String) = _uiState.update { it.copy(pricePerPlayer = v, isDirty = true) }
    fun onMaxPlayersChange(v: String) = _uiState.update { it.copy(maxPlayers = v, isDirty = true) }
    fun onAcceptReserveChange(v: Boolean) = _uiState.update { it.copy(acceptReserve = v, isDirty = true) }

    // Gameplay
    fun onGameNameChange(v: String) = _uiState.update { it.copy(gameName = v, isDirty = true) }
    fun onGameIdChange(v: String) = _uiState.update { it.copy(gameId = v, isDirty = true) }

    fun reset() = _uiState.update { CreateEventUiState() }

    // carrega dados do evento existente para edição
    fun loadForEdit(event: EventDomain) {
        val tz = TimeZone.currentSystemDefault()
        // Converte ISO-8601 UTC → local date/time para exibição
        val localDt = runCatching {
            Instant.parse(event.scheduledDatetime).toLocalDateTime(tz)
        }.getOrNull()

        val dd = localDt?.dayOfMonth?.toString()?.padStart(2, '0') ?: ""
        val mm = localDt?.monthNumber?.toString()?.padStart(2, '0') ?: ""
        val yyyy = localDt?.year?.toString() ?: ""
        // Armazena apenas dígitos brutos — DateTransformation cuida da formatação visual
        val displayDate = if (dd.isNotEmpty()) "$dd$mm$yyyy" else ""

        val hh = localDt?.hour?.toString()?.padStart(2, '0') ?: ""
        val min = localDt?.minute?.toString()?.padStart(2, '0') ?: ""
        // Armazena apenas dígitos brutos — TimeTransformation cuida da formatação visual
        val displayTime = if (hh.isNotEmpty()) "$hh$min" else ""

        _uiState.update {
            CreateEventUiState(
                isEditMode = true,
                editingEventId = event.id,
                editingGroupId = event.groupId,
                selectedType = event.eventType,
                name = event.name,
                description = event.description ?: "",
                scheduledDate = displayDate,
                scheduledTime = displayTime,
                // Sport
                durationMinutes = event.sport?.durationMinutes?.toString() ?: "",
                arena = event.sport?.arena ?: "",
                pricePerPlayer = event.sport?.pricePerPlayer?.let {
                    if (it > 0.0) it.toString() else ""
                } ?: "",
                maxPlayers = event.sport?.maxPlayers?.toString() ?: "",
                acceptReserve = event.sport?.acceptReserve ?: false,
                // Gameplay
                gameName = event.gameplay?.gameName ?: "",
                gameId = event.gameplay?.gameId ?: ""
            )
        }
    }

    fun submit(groupId: String?) {
        val s = _uiState.value

        // se é modo edição, redireciona para submitUpdate
        if (s.isEditMode && s.editingEventId != null) {
            submitUpdate(s.editingEventId)
            return
        }

        val type = s.selectedType ?: return

        if (s.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nome é obrigatório") }
            return
        }

        val (instant, dateErr) = parseScheduledDateTime(s)
        if (dateErr != null) {
            _uiState.update { it.copy(errorMessage = dateErr) }
            return
        }
        val scheduledDatetime = instant!!.toString()

        val sportInput = if (type == EventType.SPORT) {
            val duration = s.durationMinutes.toIntOrNull()
            if (duration == null || duration <= 0) {
                _uiState.update { it.copy(errorMessage = "Duração inválida") }
                return
            }
            CreateSportInput(
                durationMinutes = duration,
                arena = s.arena.takeIf { it.isNotBlank() },
                pricePerPlayer = s.pricePerPlayer.toDoubleOrNull(),
                maxPlayers = s.maxPlayers.toIntOrNull(),
                acceptReserve = s.acceptReserve
            )
        } else null

        val gameplayInput = if (type == EventType.GAMEPLAY) {
            if (s.gameName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Nome do jogo é obrigatório") }
                return
            }
            CreateGameplayInput(
                gameName = s.gameName,
                gameId = s.gameId.takeIf { it.isNotBlank() }
            )
        } else null

        val input = CreateEventInput(
            eventType = type,
            name = s.name.trim(),
            description = s.description.takeIf { it.isNotBlank() },
            scheduledDatetime = scheduledDatetime,
            gameplay = gameplayInput,
            sport = sportInput
        )
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {

                if (groupId.isNullOrBlank())
                    createInstantEvent(input)
                else
                    createGroupEvent(groupId, input)

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "Erro ao criar evento") }
            }
        }
    }

    // submete atualização do evento existente
    private fun submitUpdate(eventId: String) {
        val s = _uiState.value
        val type = s.selectedType ?: return

        if (s.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nome é obrigatório") }
            return
        }

        val (instant, dateErr) = parseScheduledDateTime(s)
        if (dateErr != null) {
            _uiState.update { it.copy(errorMessage = dateErr) }
            return
        }
        val scheduledDatetime = instant!!.toString()

        val sportInput = if (type == EventType.SPORT) {
            val duration = s.durationMinutes.toIntOrNull()
            if (duration == null || duration <= 0) {
                _uiState.update { it.copy(errorMessage = "Duração inválida") }
                return
            }
            CreateSportInput(
                durationMinutes = duration,
                arena = s.arena.takeIf { it.isNotBlank() },
                pricePerPlayer = s.pricePerPlayer.toDoubleOrNull(),
                maxPlayers = s.maxPlayers.toIntOrNull(),
                acceptReserve = s.acceptReserve
            )
        } else null

        val gameplayInput = if (type == EventType.GAMEPLAY) {
            if (s.gameName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Nome do jogo é obrigatório") }
                return
            }
            CreateGameplayInput(
                gameName = s.gameName,
                gameId = s.gameId.takeIf { it.isNotBlank() }
            )
        } else null

        val clearDesc = s.description.isBlank() // se descrição está vazia, limpa no backend

        val input = UpdateEventInput(
            name = s.name.trim(),
            description = s.description.takeIf { it.isNotBlank() },
            scheduledDatetime = scheduledDatetime,
            gameplay = gameplayInput,
            sport = sportInput,
            clearDescription = clearDesc
        )

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                updateEvent(eventId, input)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = t.message ?: "Erro ao atualizar evento")
                }
            }
        }
    }

    /**
     * Valida e converte os dígitos brutos de data (DDMMYYYY) e hora (HHMM) do formulário
     * para um Instant no fuso do usuário. Retorna `Pair(instant, null)` em caso de sucesso
     * ou `Pair(null, mensagemDeErro)` em caso de falha.
     *
     * Regras:
     * - Ambos os campos preenchidos completamente.
     * - Data/hora parseável.
     * - Resultado deve ser estritamente no futuro (no fuso local do usuário).
     */
    private fun parseScheduledDateTime(s: CreateEventUiState): Pair<Instant?, String?> {
        if (s.scheduledDate.length != 8 || s.scheduledTime.length != 4) {
            return null to "Data e hora são obrigatórios"
        }
        val tz = TimeZone.currentSystemDefault()
        val dd = s.scheduledDate.substring(0, 2)
        val mm = s.scheduledDate.substring(2, 4)
        val yyyy = s.scheduledDate.substring(4, 8)
        val hh = s.scheduledTime.substring(0, 2)
        val minute = s.scheduledTime.substring(2, 4)
        val localDateTime = runCatching {
            LocalDateTime.parse("$yyyy-$mm-${dd}T$hh:$minute")
        }.getOrNull() ?: return null to "Data ou hora inválida"

        val instant = localDateTime.toInstant(tz)
        if (instant <= Clock.System.now()) {
            return null to "A data e hora devem estar no futuro"
        }
        return instant to null
    }
}