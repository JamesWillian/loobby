package app.loobby.feature.gameplays.presentation.grouphome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.ActionSource
import app.loobby.core.analytics.GameplayResponded
import app.loobby.core.analytics.GameplayVoted
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.feature.gameplays.domain.model.Ballot
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.usecase.GetGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.GetGroupGameplaysUseCase
import app.loobby.feature.gameplays.domain.usecase.RespondToGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.VoteUseCase
import app.loobby.feature.groups.domain.usecase.ListGroupMembersUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * O Início de um grupo: as gameplays ativas, com resposta e voto direto no card.
 * Cada card busca a gameplay inteira por conta própria, depois da lista.
 */
class GroupHomeViewModel(
    private val groupId: String,
    private val getGroupGameplays: GetGroupGameplaysUseCase,
    private val getGameplay: GetGameplayUseCase,
    private val respondToGameplay: RespondToGameplayUseCase,
    private val castVote: VoteUseCase,
    private val listMembers: ListGroupMembersUseCase,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(GroupHomeUiState())
    val state: StateFlow<GroupHomeUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)

    /** Avisos de uma vez só (um voto recusado, por exemplo), para o snackbar. */
    val messages: Flow<String> = _messages.receiveAsFlow()

    private var loadJob: Job? = null

    fun refresh(pulled: Boolean = false) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isRefreshing = pulled, errorMessage = null) }
            launch { loadMemberCount() }
            try {
                val summaries = getGroupGameplays(groupId)
                _state.update { current ->
                    val known = current.cards.associateBy { it.id }
                    current.copy(
                        isLoading = false,
                        isRefreshing = false,
                        cards = summaries.map { GroupGameplayCardState(summary = it, detail = known[it.id]?.detail) },
                    )
                }
                summaries.forEach { launch { loadDetail(it.id) } }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = t.toUserMessage("Não deu para carregar as gameplays do grupo."),
                    )
                }
            }
        }
    }

    fun respond(gameplayId: String, status: ParticipationStatus) = act(gameplayId) {
        respondToGameplay(gameplayId, status).also {
            analytics.track(GameplayResponded(status, ActionSource.GROUP_CARD))
        }
    }

    fun vote(gameplayId: String, votingGameId: String) {
        val voting = card(gameplayId)?.detail?.voting ?: return
        val votes = Ballot.afterTap(voting.myVotes, votingGameId, voting.allowsMultipleVotes)
        act(gameplayId) {
            castVote(gameplayId, votes).also {
                analytics.track(GameplayVoted(ActionSource.GROUP_CARD, cleared = votes.isEmpty()))
            }
        }
    }

    /** Roda uma ação sobre a gameplay e troca o card pelo que a API devolveu. */
    private fun act(gameplayId: String, action: suspend () -> Gameplay) {
        if (card(gameplayId)?.isBusy != false) return
        viewModelScope.launch {
            updateCard(gameplayId) { it.copy(isBusy = true) }
            try {
                val updated = action()
                updateCard(gameplayId) { it.copy(detail = updated, isBusy = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                updateCard(gameplayId) { it.copy(isBusy = false) }
                _messages.trySend(t.toUserMessage("Não deu para salvar. Tente de novo."))
            }
        }
    }

    private suspend fun loadDetail(gameplayId: String) {
        try {
            val detail = getGameplay(gameplayId)
            updateCard(gameplayId) { it.copy(detail = detail) }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            // O card continua com o resumo; os botões só não aparecem.
            crashReporter.reportIfUnexpected(t)
        }
    }

    private suspend fun loadMemberCount() {
        runCatching { listMembers(groupId).size }
            .onSuccess { count -> _state.update { it.copy(memberCount = count) } }
    }

    private fun card(gameplayId: String): GroupGameplayCardState? =
        _state.value.cards.firstOrNull { it.id == gameplayId }

    private fun updateCard(gameplayId: String, change: (GroupGameplayCardState) -> GroupGameplayCardState) {
        _state.update { state ->
            state.copy(cards = state.cards.map { if (it.id == gameplayId) change(it) else it })
        }
    }
}
