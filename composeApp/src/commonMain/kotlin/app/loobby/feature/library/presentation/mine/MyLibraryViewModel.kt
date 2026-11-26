package app.loobby.feature.library.presentation.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.LibraryGameAdded
import app.loobby.core.analytics.LibraryGameRemoved
import app.loobby.core.analytics.LibraryOwnedChanged
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.library.domain.model.LibraryGame
import app.loobby.feature.library.domain.model.LibraryRules
import app.loobby.feature.library.domain.usecase.GetMyLibraryUseCase
import app.loobby.feature.library.domain.usecase.RemoveLibraryGameUseCase
import app.loobby.feature.library.domain.usecase.SaveLibraryGameUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A biblioteca da pessoa, no Perfil: os jogos, o filtro "Tenho o jogo" e o sheet de
 * cada jogo, onde se marca em que plataforma a pessoa o tem. Vale para o anônimo.
 */
class MyLibraryViewModel(
    private val getMyLibrary: GetMyLibraryUseCase,
    private val saveGame: SaveLibraryGameUseCase,
    private val removeGame: RemoveLibraryGameUseCase,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(MyLibraryUiState())
    val state: StateFlow<MyLibraryUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** O que a tela mostra num snackbar: o erro de uma escrita. */
    val messages = _messages.receiveAsFlow()

    private var loadJob: Job? = null

    fun refresh() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            try {
                val games = getMyLibrary()
                _state.update { it.copy(isLoading = false, games = games, errorMessage = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update {
                    it.copy(isLoading = false, errorMessage = t.toUserMessage("Não deu para carregar a biblioteca."))
                }
            }
        }
    }

    fun setFilter(filter: LibraryFilter) = _state.update { it.copy(filter = filter) }

    fun openGame(catalogId: String) = _state.update { it.copy(openGameId = catalogId) }

    fun closeGame() = _state.update { it.copy(openGameId = null) }

    /** O jogo escolhido na busca entra só na biblioteca, e o sheet dele abre. */
    fun add(game: GameDomain) {
        val current = _state.value
        if (current.games.any { it.catalogId == game.id }) {
            openGame(game.id)
            return
        }
        if (current.games.size >= LibraryRules.MAX_MINE) {
            _messages.trySend("Sua biblioteca já tem ${LibraryRules.MAX_MINE} jogos.")
            return
        }
        write("Não deu para pôr o jogo na biblioteca.") {
            val saved = saveGame(game.id, emptySet())
            analytics.track(LibraryGameAdded)
            replace(saved)
            _state.update { it.copy(openGameId = saved.catalogId) }
        }
    }

    /** Marca ou desmarca o "tenho o jogo" em [platform]. */
    fun toggleOwned(game: LibraryGame, platform: Platform) {
        val owned = if (platform in game.ownedPlatforms) game.ownedPlatforms - platform else game.ownedPlatforms + platform
        write("Não deu para salvar o \"tenho o jogo\".") {
            val saved = saveGame(game.catalogId, owned)
            analytics.track(LibraryOwnedChanged(owned = saved.isOwned))
            replace(saved)
        }
    }

    fun remove(game: LibraryGame) {
        write("Não deu para tirar o jogo da biblioteca.") {
            removeGame(game.catalogId)
            analytics.track(LibraryGameRemoved)
            _state.update { s ->
                s.copy(games = s.games.filterNot { it.catalogId == game.catalogId }, openGameId = null)
            }
        }
    }

    private fun replace(saved: LibraryGame) = _state.update { s ->
        val games = (s.games.filterNot { it.catalogId == saved.catalogId } + saved)
            .sortedWith(compareBy({ it.name.lowercase() }, { it.catalogId }))
        s.copy(games = games)
    }

    private fun write(failure: String, block: suspend () -> Unit) {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _messages.trySend(t.toUserMessage(failure))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
