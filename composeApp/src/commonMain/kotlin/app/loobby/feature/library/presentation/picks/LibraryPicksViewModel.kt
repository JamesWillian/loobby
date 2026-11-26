package app.loobby.feature.library.presentation.picks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.library.domain.model.LibrarySource
import app.loobby.feature.library.domain.usecase.GetLibraryPicksUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A escolha rápida do picker de jogo: os jogos de uma biblioteca, carregados uma vez
 * por abertura. Falhar não atrapalha a busca: a lista só não aparece.
 */
class LibraryPicksViewModel(
    private val source: LibrarySource,
    private val getPicks: GetLibraryPicksUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _picks = MutableStateFlow<List<GameChoice>>(emptyList())
    val picks: StateFlow<List<GameChoice>> = _picks.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _picks.value = getPicks(source)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
            }
        }
    }
}
