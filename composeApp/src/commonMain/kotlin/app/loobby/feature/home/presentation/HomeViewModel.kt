package app.loobby.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.media.ImagePrefetcher
import app.loobby.core.navigation.AppRoute
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.usecase.ListMyGroupsUseCase
import app.loobby.feature.home.domain.HomePreferences
import app.loobby.feature.home.domain.HomeSelection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val groups: List<GroupDomain> = emptyList(),
    /** A lista de grupos já veio pelo menos uma vez nesta sessão. */
    val hasLoadedGroups: Boolean = false,
    val isRailOpen: Boolean = true,
)

/**
 * A barra lateral do Início: os grupos da pessoa, o que está escolhido (Lobby ou um
 * grupo) e se a barra está aberta. As escolhas ficam salvas para a próxima abertura.
 */
class HomeViewModel(
    private val listMyGroups: ListMyGroupsUseCase,
    private val preferences: HomePreferences,
    private val auth: AuthRepository,
    private val imagePrefetcher: ImagePrefetcher,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(isRailOpen = preferences.isRailOpen))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        // Grupos são de quem está logado: recarrega a cada troca de conta. No logout,
        // o Início volta para o Lobby.
        viewModelScope.launch {
            auth.sessionFlow
                .map { it?.userId }
                .distinctUntilChanged()
                .collect { userId ->
                    if (userId == null) {
                        preferences.selection = HomeSelection.Lobby
                        _state.update { it.copy(groups = emptyList(), hasLoadedGroups = false) }
                    } else {
                        refreshGroups()
                    }
                }
        }
    }

    /** A raiz do Início ao abrir o app: o que estava escolhido da última vez. */
    fun initialHomeRoot(): AppRoute = when (val selection = preferences.selection) {
        HomeSelection.Lobby -> AppRoute.Lobby
        is HomeSelection.Group -> AppRoute.GroupHome(groupId = selection.groupId)
    }

    fun refreshGroups() {
        viewModelScope.launch {
            try {
                val groups = listMyGroups()
                _state.update { it.copy(groups = groups, hasLoadedGroups = true) }
                imagePrefetcher.prefetch(groups.map { it.imageUrl })
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
            }
        }
    }

    fun select(selection: HomeSelection) {
        preferences.selection = selection
    }

    fun toggleRail() {
        val open = !_state.value.isRailOpen
        preferences.isRailOpen = open
        _state.update { it.copy(isRailOpen = open) }
    }
}
