package app.loobby.feature.home.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.CrashReporter
import app.loobby.feature.gameplays.domain.usecase.GetHistoryUseCase
import app.loobby.feature.gameplays.domain.usecase.ObserveRemovedGameplaysUseCase
import app.loobby.feature.gameplays.presentation.ended.EndedListController
import app.loobby.feature.gameplays.presentation.ended.EndedListUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** O Histórico: as encerradas em que a pessoa marcou Vou, da mais recente, em páginas. */
class HistoryViewModel(
    getHistory: GetHistoryUseCase,
    observeRemovals: ObserveRemovedGameplaysUseCase,
    crashReporter: CrashReporter,
) : ViewModel() {

    private val list = EndedListController(
        scope = viewModelScope,
        crashReporter = crashReporter,
        errorMessage = "Não deu para carregar o histórico.",
        load = { before -> getHistory(before) },
    )

    val state: StateFlow<EndedListUiState> = list.state

    init {
        // A aba do Histórico fica viva por trás do Início: a excluída sai daqui também.
        viewModelScope.launch { observeRemovals().collect { list.remove(it) } }
    }

    fun refresh(pulled: Boolean = false) = list.refresh(pulled)

    fun loadMore() = list.loadMore()
}
