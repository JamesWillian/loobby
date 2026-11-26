package app.loobby.core.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

/**
 * Se a lista está no começo, sem nada rolado. Só muda ao sair do topo ou voltar a
 * ele, então quem lê não recompõe a cada quadro da rolagem.
 */
@Composable
fun LazyListState.rememberIsAtTop(): State<Boolean> = remember(this) {
    derivedStateOf { firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0 }
}
