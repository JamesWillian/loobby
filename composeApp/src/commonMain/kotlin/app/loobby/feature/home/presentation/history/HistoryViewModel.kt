package app.loobby.feature.home.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.CrashReporter
import app.loobby.feature.gameplays.domain.usecase.GetHistoryUseCase
import app.loobby.feature.gameplays.presentation.ended.EndedListController
import app.loobby.feature.gameplays.presentation.ended.EndedListUiState
import kotlinx.coroutines.flow.StateFlow

/** O Histórico: as encerradas em que a pessoa marcou Vou, da mais recente, em páginas. */
class HistoryViewModel(
    getHistory: GetHistoryUseCase,
    crashReporter: CrashReporter,
) : ViewModel() {

    private val list = EndedListController(
        scope = viewModelScope,
        crashReporter = crashReporter,
        errorMessage = "Não deu para carregar o histórico.",
        load = { before -> getHistory(before) },
    )

    val state: StateFlow<EndedListUiState> = list.state

    fun refresh(pulled: Boolean = false) = list.refresh(pulled)

    fun loadMore() = list.loadMore()
}
