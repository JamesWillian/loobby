package app.loobby.feature.gameplays.presentation.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.feature.gameplays.domain.usecase.GetLobbyUseCase
import app.loobby.feature.gameplays.presentation.model.toCardModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** O Lobby: as gameplays ativas da pessoa, da mais próxima para a mais distante. */
class LobbyViewModel(
    private val getLobby: GetLobbyUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(LobbyUiState())
    val state: StateFlow<LobbyUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    /** Recarrega a lista. [pulled]: a pessoa puxou para atualizar e vê o indicador. */
    fun refresh(pulled: Boolean = false) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isRefreshing = pulled, errorMessage = null) }
            try {
                val gameplays = getLobby().map { it.toCardModel() }
                _state.update { it.copy(isLoading = false, isRefreshing = false, gameplays = gameplays) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = t.toUserMessage("Não deu para carregar o Lobby."),
                    )
                }
            }
        }
    }
}
