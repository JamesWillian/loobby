package app.loobby.feature.events.teams.presentation

import app.loobby.feature.events.domain.usecase.ListEventRsvpsUseCase
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamsViewModel(
    private val listTeams: ListTeamsUseCase,
    private val createTeam: CreateTeamUseCase,
    private val updateTeam: UpdateTeamUseCase,
    private val deleteTeam: DeleteTeamUseCase,
    private val addPlayer: AddPlayerToTeamUseCase,
    private val removePlayer: RemovePlayerFromTeamUseCase,
    private val movePlayer: MovePlayerUseCase,
    private val autoGenerate: AutoGenerateTeamsUseCase,
    private val listRsvps: ListEventRsvpsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(TeamsUiState())
    val uiState: StateFlow<TeamsUiState> = _uiState.asStateFlow()

    private var currentEventId: String? = null

    fun load(eventId: String) {
        currentEventId = eventId
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val teams = listTeams(eventId)
                val rsvps = listRsvps(eventId)
                val confirmed = rsvps.filter {
                    it.status == app.loobby.feature.events.domain.model.RsvpStatus.YES
                }
                val reserve = rsvps.filter {
                    it.status == app.loobby.feature.events.domain.model.RsvpStatus.RESERVE
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        teams = teams,
                        confirmedPlayers = confirmed,
                        reservePlayers = reserve
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message) }
            }
        }
    }

    fun onCreateTeam(eventId: String, name: String, color: String?, playerIds: List<String>) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                createTeam(eventId, name, color, playerIds)
                reload(eventId)
                showSuccess("Time criado!")
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message) }
            }
        }
    }

    fun onUpdateTeam(eventId: String, teamId: String, name: String?, color: String?) {
        scope.launch {
            try {
                updateTeam(eventId, teamId, name, color)
                reload(eventId)
                showSuccess("Time atualizado!")
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = t.message) }
            }
        }
    }

    fun onDeleteTeam(eventId: String, teamId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteTeam(eventId, teamId)
                reload(eventId)
                showSuccess("Time excluído!")
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message) }
            }
        }
    }

    fun onAddPlayer(eventId: String, teamId: String, userId: String) {
        scope.launch {
            try {
                addPlayer(eventId, teamId, userId)
                reload(eventId)
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = t.message) }
            }
        }
    }

    fun onRemovePlayer(eventId: String, teamId: String, userId: String) {
        scope.launch {
            try {
                removePlayer(eventId, teamId, userId)
                reload(eventId)
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = t.message) }
            }
        }
    }

    fun onMovePlayer(eventId: String, fromTeamId: String, userId: String, toTeamId: String) {
        scope.launch {
            try {
                movePlayer(eventId, fromTeamId, userId, toTeamId)
                reload(eventId)
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = t.message) }
            }
        }
    }

    fun onAutoGenerate(eventId: String, teamCount: Int?, teamSize: Int?) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                autoGenerate(eventId, teamCount, teamSize)
                reload(eventId)
                showSuccess("Times gerados!")
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message) }
            }
        }
    }

    fun onReorderTeams(eventId: String, reorderedTeams: List<TeamDomain>) {
        // Compara com a ordem atual para só atualizar os que mudaram
        val currentTeams = _uiState.value.teams

        // Atualiza UI otimisticamente
        _uiState.update { it.copy(teams = reorderedTeams) }

        scope.launch {
            try {
                // Para cada time na nova posição, se a order mudou, faz update
                reorderedTeams.forEachIndexed { newIndex, team ->
                    val oldIndex = currentTeams.indexOfFirst { it.id == team.id }
                    if (oldIndex != newIndex) {
                        updateTeam(eventId, team.id, order = newIndex)
                    }
                }
                // Reload para garantir consistência com o servidor
                reload(eventId)
            } catch (t: Throwable) {
                // Em caso de erro, reverte para a ordem do servidor
                _uiState.update { it.copy(errorMessage = "Erro ao reordenar: ${t.message}") }
                reload(eventId)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private suspend fun reload(eventId: String) {
        val teams = listTeams(eventId)
        val rsvps = listRsvps(eventId)
        val confirmed = rsvps.filter {
            it.status == app.loobby.feature.events.domain.model.RsvpStatus.YES
        }
        _uiState.update {
            it.copy(isLoading = false, teams = teams, confirmedPlayers = confirmed, errorMessage = null)
        }
    }

    private fun showSuccess(message: String) {
        scope.launch {
            _uiState.update { it.copy(successMessage = message) }
            delay(2_000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }
}