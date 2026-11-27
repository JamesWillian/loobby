package app.loobby.feature.gameplays.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.ActionSource
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CommentDeleted
import app.loobby.core.analytics.CommentPosted
import app.loobby.core.analytics.CommentReported
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.GameplayDeleted
import app.loobby.core.analytics.GameplayEnded
import app.loobby.core.analytics.GameplayLeft
import app.loobby.core.analytics.GameplayResponded
import app.loobby.core.analytics.GameplayVoted
import app.loobby.core.analytics.TiebreakOpened
import app.loobby.core.analytics.GamePickSource
import app.loobby.core.analytics.VotingGameAdded
import app.loobby.core.analytics.VotingGameRemoved
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.feature.gameplays.domain.model.Ballot
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.usecase.AddVotingGameUseCase
import app.loobby.feature.gameplays.domain.usecase.DeleteCommentUseCase
import app.loobby.feature.gameplays.domain.usecase.DeleteGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.EndGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.GetCommentsUseCase
import app.loobby.feature.gameplays.domain.usecase.GetGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.LeaveGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.OpenTiebreakUseCase
import app.loobby.feature.gameplays.domain.usecase.PostCommentUseCase
import app.loobby.feature.gameplays.domain.usecase.RemoveVotingGameUseCase
import app.loobby.feature.gameplays.domain.usecase.ReportCommentUseCase
import app.loobby.feature.gameplays.domain.usecase.RespondToGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.VoteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A tela de uma gameplay: participação, votação, encerramento, exclusão e comentários. */
class GameplayDetailViewModel(
    private val gameplayId: String,
    private val useCases: GameplayDetailUseCases,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(GameplayDetailUiState())
    val state: StateFlow<GameplayDetailUiState> = _state.asStateFlow()

    private val _events = Channel<GameplayDetailEvent>(Channel.BUFFERED)
    val events: Flow<GameplayDetailEvent> = _events.receiveAsFlow()

    // ── Carga ──────────────────────────────────────────────────────────────

    /** Carrega a gameplay e a primeira página de comentários. */
    fun load() {
        viewModelScope.launch { loadGameplay() }
        viewModelScope.launch { loadComments() }
    }

    private suspend fun loadGameplay() {
        try {
            val gameplay = useCases.getGameplay(gameplayId)
            _state.update { it.copy(gameplay = gameplay, isLoading = false, loadError = null) }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            crashReporter.reportIfUnexpected(t)
            val message = t.toUserMessage("Não deu para abrir a gameplay.")
            if (_state.value.gameplay == null) {
                _state.update { it.copy(isLoading = false, loadError = message) }
            } else {
                _state.update { it.copy(isLoading = false) }
                _events.trySend(GameplayDetailEvent.Message(message))
            }
        }
    }

    // ── Participação e votação ─────────────────────────────────────────────

    fun respond(status: ParticipationStatus) = act(DetailAction.RESPOND) {
        useCases.respond(gameplayId, status).also {
            analytics.track(GameplayResponded(status, ActionSource.DETAIL))
        }
    }

    fun vote(votingGameId: String) {
        val voting = _state.value.gameplay?.voting ?: return
        val votes = Ballot.afterTap(voting.myVotes, votingGameId, voting.allowsMultipleVotes)
        act(DetailAction.VOTE) {
            useCases.vote(gameplayId, votes).also {
                analytics.track(GameplayVoted(ActionSource.DETAIL, cleared = votes.isEmpty()))
            }
        }
    }

    /** [source]: de onde veio o jogo (busca, digitado ou uma das bibliotecas), para o Analytics. */
    fun addGame(choice: GameChoice, source: GamePickSource) = act(DetailAction.ADD_GAME) {
        useCases.addVotingGame(gameplayId, choice).also { analytics.track(VotingGameAdded(source)) }
    }

    fun removeGame(votingGameId: String) = act(DetailAction.REMOVE_GAME) {
        useCases.removeVotingGame(gameplayId, votingGameId).also { analytics.track(VotingGameRemoved) }
    }

    fun openTiebreak() = act(DetailAction.TIEBREAK) {
        useCases.openTiebreak(gameplayId).also { analytics.track(TiebreakOpened) }
    }

    fun end() = act(DetailAction.END) {
        useCases.end(gameplayId).also { analytics.track(GameplayEnded) }
    }

    /** A gameplay voltou editada do formulário. */
    fun onEdited(gameplay: Gameplay) = _state.update { it.copy(gameplay = gameplay) }

    fun leave() = closeAfter(DetailAction.LEAVE, GameplayDetailEvent.Left) {
        useCases.leave(gameplayId)
        analytics.track(GameplayLeft)
    }

    /** Exclui a gameplay, ativa ou encerrada. A tela fecha quando a API confirma. */
    fun delete() {
        val wasEnded = _state.value.gameplay?.isEnded ?: return
        closeAfter(DetailAction.DELETE, GameplayDetailEvent.Deleted) {
            useCases.delete(gameplayId)
            analytics.track(GameplayDeleted(wasEnded))
        }
    }

    /** Uma ação depois da qual não há mais o que ver aqui: roda [call] e manda [event]. */
    private fun closeAfter(action: DetailAction, event: GameplayDetailEvent, call: suspend () -> Unit) {
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(pendingAction = action) }
            try {
                call()
                _events.send(event)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                fail(t)
            } finally {
                _state.update { it.copy(pendingAction = null) }
            }
        }
    }

    /** Uma ação por vez: roda [call] e troca a gameplay pela que a API devolveu. */
    private fun act(action: DetailAction, call: suspend () -> Gameplay) {
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(pendingAction = action) }
            try {
                val updated = call()
                _state.update { it.copy(gameplay = updated, pendingAction = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                _state.update { it.copy(pendingAction = null) }
                fail(t)
            }
        }
    }

    // ── Comentários ────────────────────────────────────────────────────────

    fun onDraftChange(text: String) = updateComments { it.copy(draft = text) }

    fun postComment() {
        val draft = _state.value.comments.draft
        if (draft.isBlank() || _state.value.comments.isPosting) return
        viewModelScope.launch {
            updateComments { it.copy(isPosting = true) }
            try {
                val comment = useCases.postComment(gameplayId, draft)
                analytics.track(CommentPosted)
                updateComments { it.copy(items = listOf(comment) + it.items, draft = "", isPosting = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                updateComments { it.copy(isPosting = false) }
                fail(t)
            }
        }
    }

    fun loadMoreComments() {
        val comments = _state.value.comments
        val oldest = comments.items.lastOrNull() ?: return
        if (comments.isLoadingMore || !comments.mayHaveMore) return
        viewModelScope.launch {
            updateComments { it.copy(isLoadingMore = true) }
            try {
                val page = useCases.getComments(gameplayId, before = oldest.id)
                updateComments {
                    it.copy(items = it.items + page.comments, mayHaveMore = page.mayHaveMore, isLoadingMore = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                updateComments { it.copy(isLoadingMore = false) }
                fail(t)
            }
        }
    }

    fun deleteComment(commentId: String) = commentAction {
        useCases.deleteComment(gameplayId, commentId)
        analytics.track(CommentDeleted)
        updateComments { state -> state.copy(items = state.items.filterNot { it.id == commentId }) }
    }

    fun reportComment(commentId: String, reason: String?) = commentAction {
        useCases.reportComment(gameplayId, commentId, reason)
        analytics.track(CommentReported)
        updateComments { state ->
            state.copy(items = state.items.map { if (it.id == commentId) it.copy(reportedByMe = true) else it })
        }
        _events.send(GameplayDetailEvent.Message("Denúncia enviada. O comentário não aparece mais para você."))
    }

    private suspend fun loadComments() {
        try {
            val page = useCases.getComments(gameplayId)
            updateComments { it.copy(items = page.comments, mayHaveMore = page.mayHaveMore, isLoading = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            crashReporter.reportIfUnexpected(t)
            updateComments { it.copy(isLoading = false) }
        }
    }

    private fun commentAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                fail(t)
            }
        }
    }

    private fun updateComments(change: (CommentsUiState) -> CommentsUiState) =
        _state.update { it.copy(comments = change(it.comments)) }

    private fun fail(t: Throwable) {
        crashReporter.reportIfUnexpected(t)
        _events.trySend(GameplayDetailEvent.Message(t.toUserMessage("Não deu para salvar. Tente de novo.")))
    }
}

/** Os casos de uso da tela, num pacote só para o construtor não ficar enorme. */
class GameplayDetailUseCases(
    val getGameplay: GetGameplayUseCase,
    val respond: RespondToGameplayUseCase,
    val vote: VoteUseCase,
    val addVotingGame: AddVotingGameUseCase,
    val removeVotingGame: RemoveVotingGameUseCase,
    val openTiebreak: OpenTiebreakUseCase,
    val end: EndGameplayUseCase,
    val delete: DeleteGameplayUseCase,
    val leave: LeaveGameplayUseCase,
    val getComments: GetCommentsUseCase,
    val postComment: PostCommentUseCase,
    val deleteComment: DeleteCommentUseCase,
    val reportComment: ReportCommentUseCase,
)
