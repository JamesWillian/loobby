package app.loobby.feature.gameplays.presentation.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.AnalyticsEvent
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.GameplayJoinedByInvite
import app.loobby.core.analytics.InviteSearchResult
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.feature.gameplays.domain.model.InviteCode
import app.loobby.feature.gameplays.domain.model.InviteKind
import app.loobby.feature.gameplays.domain.usecase.GetGameplayInviteUseCase
import app.loobby.feature.gameplays.domain.usecase.JoinGameplayByInviteUseCase
import app.loobby.feature.groups.domain.usecase.GetGroupByInviteUseCase
import app.loobby.feature.groups.domain.usecase.JoinGroupUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Entrar com convite: o mesmo campo serve para grupo e para gameplay única. O
 * formato do código diz qual é; o de evento da versão antiga recebe um aviso.
 */
class JoinInviteViewModel(
    private val getGroupByInvite: GetGroupByInviteUseCase,
    private val joinGroup: JoinGroupUseCase,
    private val getGameplayInvite: GetGameplayInviteUseCase,
    private val joinGameplay: JoinGameplayByInviteUseCase,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(JoinInviteUiState())
    val state: StateFlow<JoinInviteUiState> = _state.asStateFlow()

    private val _joined = Channel<InviteJoined>(Channel.BUFFERED)
    val joined: Flow<InviteJoined> = _joined.receiveAsFlow()

    fun onCodeChange(raw: String) {
        val code = raw.trim().removePrefix(InviteCode.PREFIX).uppercase()
            .filter { it in 'A'..'Z' || it in '0'..'9' || it == '-' }
        _state.update { it.copy(code = code, found = null, errorMessage = null) }
    }

    fun search() {
        val code = InviteCode.normalize(_state.value.code)
        when (InviteCode.kindOf(code)) {
            InviteKind.GROUP -> find(InviteSearchResult.GROUP) { InviteFound.Group(getGroupByInvite(code)) }
            InviteKind.GAMEPLAY -> find(InviteSearchResult.GAMEPLAY) { InviteFound.Gameplay(getGameplayInvite(code)) }
            InviteKind.LEGACY_EVENT -> {
                analytics.track(AnalyticsEvent.InviteCodeSearched(InviteSearchResult.LEGACY_EVENT))
                _state.update { it.copy(errorMessage = "Esse convite é de um evento da versão antiga, que o 4Players não abre mais.") }
            }
            InviteKind.UNKNOWN -> _state.update {
                it.copy(errorMessage = "Esse código não parece um convite. Confira e tente de novo.")
            }
        }
    }

    fun join() {
        val found = _state.value.found ?: return
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isJoining = true, errorMessage = null) }
            try {
                val joined = when (found) {
                    is InviteFound.Group -> {
                        joinGroup(found.group.id)
                        analytics.track(AnalyticsEvent.GroupJoined)
                        InviteJoined.Group(found.group.id, found.group.name)
                    }
                    is InviteFound.Gameplay -> {
                        if (!found.preview.joined) {
                            joinGameplay(InviteCode.normalize(_state.value.code))
                            analytics.track(GameplayJoinedByInvite)
                        }
                        InviteJoined.Gameplay(found.preview.id)
                    }
                }
                _state.update { it.copy(isJoining = false) }
                _joined.send(joined)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update { it.copy(isJoining = false, errorMessage = t.toUserMessage("Não deu para entrar.")) }
            }
        }
    }

    private fun find(result: InviteSearchResult, lookup: suspend () -> InviteFound) {
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, errorMessage = null, found = null) }
            try {
                val found = lookup()
                analytics.track(AnalyticsEvent.InviteCodeSearched(result))
                _state.update { it.copy(isSearching = false, found = found) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                analytics.track(AnalyticsEvent.InviteCodeSearched(InviteSearchResult.FAILED))
                crashReporter.reportIfUnexpected(t)
                _state.update { it.copy(isSearching = false, errorMessage = t.toUserMessage("Convite não encontrado.")) }
            }
        }
    }
}
