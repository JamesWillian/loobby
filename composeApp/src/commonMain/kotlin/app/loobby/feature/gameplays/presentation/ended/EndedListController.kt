package app.loobby.feature.gameplays.presentation.ended

import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.feature.gameplays.domain.model.GameplaysPage
import app.loobby.feature.gameplays.presentation.model.toCardModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * As encerradas em páginas, para o Histórico e as Finalizadas do grupo: a primeira
 * página (que tem cache), as seguintes ao rolar e o puxar para atualizar. [load]
 * recebe a última gameplay da página anterior, ou null para a primeira.
 */
class EndedListController(
    private val scope: CoroutineScope,
    private val crashReporter: CrashReporter,
    private val errorMessage: String,
    private val load: suspend (before: String?) -> GameplaysPage,
) {
    private val _state = MutableStateFlow(EndedListUiState())
    val state: StateFlow<EndedListUiState> = _state.asStateFlow()

    private var job: Job? = null

    /** Recarrega a primeira página. [pulled]: a pessoa puxou e vê o indicador. */
    fun refresh(pulled: Boolean = false) {
        if (job?.isActive == true && !pulled) return
        job?.cancel()
        job = scope.launch {
            _state.update { it.copy(isRefreshing = pulled, errorMessage = null, loadMoreFailed = false) }
            try {
                val page = load(null)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        gameplays = page.gameplays.map { g -> g.toCardModel() },
                        mayHaveMore = page.mayHaveMore,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update {
                    it.copy(isLoading = false, isRefreshing = false, errorMessage = t.toUserMessage(errorMessage))
                }
            }
        }
    }

    /** A página seguinte, depois da última que está na tela. */
    fun loadMore() {
        val current = _state.value
        val last = current.gameplays.lastOrNull()?.id ?: return
        if (!current.mayHaveMore || job?.isActive == true) return
        job = scope.launch {
            _state.update { it.copy(isLoadingMore = true, loadMoreFailed = false) }
            try {
                val page = load(last)
                _state.update { s ->
                    s.copy(
                        isLoadingMore = false,
                        gameplays = (s.gameplays + page.gameplays.map { it.toCardModel() }).distinctBy { it.id },
                        mayHaveMore = page.mayHaveMore,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update { it.copy(isLoadingMore = false, loadMoreFailed = true) }
            }
        }
    }

    /** A gameplay saiu do aparelho (foi excluída, por exemplo): sai da lista na hora. */
    fun remove(gameplayId: String) {
        _state.update { s -> s.copy(gameplays = s.gameplays.filterNot { it.id == gameplayId }) }
    }
}
