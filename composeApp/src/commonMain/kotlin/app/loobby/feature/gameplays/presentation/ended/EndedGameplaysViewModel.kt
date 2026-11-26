package app.loobby.feature.gameplays.presentation.ended

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.CrashReporter
import app.loobby.feature.gameplays.domain.usecase.GetEndedGroupGameplaysUseCase
import kotlinx.coroutines.flow.StateFlow

/** As Finalizadas de um grupo: as encerradas, da mais recente, em páginas. */
class EndedGameplaysViewModel(
    groupId: String,
    getEnded: GetEndedGroupGameplaysUseCase,
    crashReporter: CrashReporter,
) : ViewModel() {

    private val list = EndedListController(
        scope = viewModelScope,
        crashReporter = crashReporter,
        errorMessage = "Não deu para carregar as encerradas do grupo.",
        load = { before -> getEnded(groupId, before) },
    )

    val state: StateFlow<EndedListUiState> = list.state

    fun refresh(pulled: Boolean = false) = list.refresh(pulled)

    fun loadMore() = list.loadMore()
}
