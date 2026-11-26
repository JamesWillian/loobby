package app.loobby.feature.games.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.games.domain.usecase.SearchGamesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameSearchUiState(
    val query: String = "",
    val results: List<GameDomain> = emptyList(),
    val isSearching: Boolean = false,
    val hasError: Boolean = false,
    val manualName: String = "",
)

/**
 * A busca no catálogo enquanto a pessoa digita. Espera a pessoa parar de digitar
 * antes de buscar, para poupar a cota da RAWG.
 */
class GameSearchViewModel(
    private val searchGames: SearchGamesUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(GameSearchUiState())
    val state: StateFlow<GameSearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), isSearching = false, hasError = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            _state.update { it.copy(isSearching = true, hasError = false) }
            try {
                val results = searchGames(query.trim())
                _state.update { it.copy(results = results, isSearching = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update { it.copy(results = emptyList(), isSearching = false, hasError = true) }
            }
        }
    }

    fun onManualNameChange(name: String) = _state.update { it.copy(manualName = name) }

    /** Limpa a busca para a próxima escolha. */
    fun clear() {
        searchJob?.cancel()
        _state.value = GameSearchUiState()
    }

    private companion object {
        const val DEBOUNCE_MS = 350L
    }
}
