package app.loobby.feature.games.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.loobby.core.ui.collectAsImmediateState
import app.loobby.feature.games.domain.model.GameDomain
import org.koin.compose.viewmodel.koinViewModel

/**
 * O [GamePickerContent] já com a busca ligada. Devolve o jogo do catálogo ou o nome
 * digitado, e limpa a busca para a próxima escolha.
 */
@Composable
fun GameSearchPicker(
    onPickCatalogGame: (game: GameDomain) -> Unit,
    onPickTypedName: (name: String) -> Unit,
    modifier: Modifier = Modifier,
    vm: GameSearchViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsImmediateState()
    GamePickerContent(
        query = state.query,
        results = state.results,
        isSearching = state.isSearching,
        manualName = state.manualName,
        hasSearchError = state.hasError,
        onQueryChange = vm::onQueryChange,
        onSelectGame = { game ->
            vm.clear()
            onPickCatalogGame(game)
        },
        onManualNameChange = vm::onManualNameChange,
        onUseManual = {
            val name = state.manualName.trim()
            vm.clear()
            onPickTypedName(name)
        },
        modifier = modifier,
    )
}
