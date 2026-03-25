package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.CreateGameplayInput
import app.loobby.feature.events.domain.model.CreateSportInput
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.usecase.CreateGroupEventUseCase
import app.loobby.feature.events.domain.usecase.CreateInstantEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class CreateEventViewModel(
    private val createGroupEvent: CreateGroupEventUseCase,
    private val createInstantEvent: CreateInstantEventUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun selectType(type: EventType) = _uiState.update { it.copy(selectedType = type) }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    // CHANGED: apply DD-MM-YYYY mask automatically; store raw digits only
    fun onDateChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(8)
        val masked = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 2 || i == 4) append('-')
                append(c)
            }
        }
        _uiState.update { it.copy(scheduledDate = masked) }
    }

    // CHANGED: apply HH:MM mask automatically
    fun onTimeChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(4)
        val masked = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 2) append(':')
                append(c)
            }
        }
        _uiState.update { it.copy(scheduledTime = masked) }
    }

    // Sport
    fun onDurationChange(v: String) = _uiState.update { it.copy(durationMinutes = v) }
    fun onArenaChange(v: String) = _uiState.update { it.copy(arena = v) }
    fun onPriceChange(v: String) = _uiState.update { it.copy(pricePerPlayer = v) }
    fun onMaxPlayersChange(v: String) = _uiState.update { it.copy(maxPlayers = v) }
    fun onAcceptReserveChange(v: Boolean) = _uiState.update { it.copy(acceptReserve = v) }

    // Gameplay
    fun onGameNameChange(v: String) = _uiState.update { it.copy(gameName = v) }
    fun onGameIdChange(v: String) = _uiState.update { it.copy(gameId = v) }

    fun reset() = _uiState.update { CreateEventUiState() }

    fun submit(groupId: String?) {
        val s = _uiState.value
        val type = s.selectedType ?: return

        if (s.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nome é obrigatório") }
            return
        }
        if (s.scheduledDate.isBlank() || s.scheduledTime.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Data e hora são obrigatórios") }
            return
        }

        val tz = TimeZone.currentSystemDefault()
        // CHANGED: convert display format DD-MM-YYYY → ISO YYYY-MM-DD for LocalDateTime.parse
        val parts = s.scheduledDate.split("-")
        val isoDate = if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else s.scheduledDate
        val localDateTime = LocalDateTime.parse("${isoDate}T${s.scheduledTime}")
        val instant = localDateTime.toInstant(tz)
        val scheduledDatetime = instant.toString()

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
}