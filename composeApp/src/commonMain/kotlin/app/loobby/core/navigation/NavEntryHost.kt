package app.loobby.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Guarda o estado salvo (rolagem, campos) de todas as telas abertas. Fica no topo
 * do app, criado uma vez: quando uma tela sai da pilha, o estado dela vai embora.
 */
@Composable
fun rememberNavEntryStates(navigator: AppNavigator): SaveableStateHolder {
    val stateHolder = rememberSaveableStateHolder()
    DisposableEffect(navigator, stateHolder) {
        navigator.onEntryRemoved = { stateHolder.removeState(it.id) }
        onDispose {
            navigator.onEntryRemoved = {}
            navigator.disposeAll()
        }
    }
    return stateHolder
}

/**
 * Mostra a tela [entry] com o que é dela: o estado salvo e os ViewModels. Quando a
 * tela volta a aparecer, depois de outra aba ou de uma tela por cima, ela reaparece
 * como estava.
 */
@Composable
fun NavEntryHost(
    states: SaveableStateHolder,
    entry: NavEntry,
    content: @Composable (AppRoute) -> Unit,
) {
    states.SaveableStateProvider(key = entry.id) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides entry) {
            content(entry.route)
        }
    }
}
