package app.loobby.feature.library.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.GroupLibraryGameAdded
import app.loobby.core.analytics.GroupLibraryGameRemoved
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.library.domain.model.GroupLibraryGame
import app.loobby.feature.library.domain.model.LibraryRules
import app.loobby.feature.library.domain.usecase.AddGroupLibraryGameUseCase
import app.loobby.feature.library.domain.usecase.GetGroupLibraryUseCase
import app.loobby.feature.library.domain.usecase.RemoveGroupLibraryGameUseCase
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
 * A biblioteca do grupo, nos Detalhes do grupo: os jogos que o grupo joga. Qualquer
 * membro que não seja anônimo adiciona e tira; a API confere de novo.
 */
class GroupLibraryViewModel(
    private val groupId: String,
    private val getGroupLibrary: GetGroupLibraryUseCase,
    private val addGame: AddGroupLibraryGameUseCase,
    private val removeGame: RemoveGroupLibraryGameUseCase,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(GroupLibraryUiState())
    val state: StateFlow<GroupLibraryUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    private var loadJob: Job? = null

    fun refresh() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            try {
                val games = getGroupLibrary(groupId)
                _state.update { it.copy(isLoading = false, games = games, errorMessage = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update {
                    it.copy(isLoading = false, errorMessage = t.toUserMessage("Não deu para carregar a biblioteca do grupo."))
                }
            }
        }
    }

    fun add(game: GameDomain) {
        val current = _state.value
        if (current.games.any { it.catalogId == game.id }) {
            _messages.trySend("${game.name} já está na biblioteca do grupo.")
            return
        }
        if (current.games.size >= LibraryRules.MAX_GROUP) {
            _messages.trySend("A biblioteca do grupo já tem ${LibraryRules.MAX_GROUP} jogos.")
            return
        }
        write("Não deu para pôr o jogo na biblioteca do grupo.") {
            val added = addGame(groupId, game.id)
            analytics.track(GroupLibraryGameAdded)
            _state.update { s ->
                val games = (s.games.filterNot { it.catalogId == added.catalogId } + added)
                    .sortedWith(compareBy({ it.name.lowercase() }, { it.catalogId }))
                s.copy(games = games)
            }
        }
    }

    fun remove(game: GroupLibraryGame) {
        write("Não deu para tirar o jogo da biblioteca do grupo.") {
            removeGame(groupId, game.catalogId)
            analytics.track(GroupLibraryGameRemoved)
            _state.update { s -> s.copy(games = s.games.filterNot { it.catalogId == game.catalogId }) }
        }
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
