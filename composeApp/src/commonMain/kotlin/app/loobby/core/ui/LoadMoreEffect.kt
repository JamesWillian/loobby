package app.loobby.core.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Pede a página seguinte ([onLoadMore]) quando a lista chega a [threshold] itens do
 * fim. Pede uma vez por chegada: para pedir de novo, a lista precisa crescer ou
 * [enabled] voltar a valer.
 */
@Composable
fun LoadMoreEffect(listState: LazyListState, enabled: Boolean, threshold: Int = 3, onLoadMore: () -> Unit) {
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index
            last != null && last >= info.totalItemsCount - 1 - threshold
        }
    }
    val load by rememberUpdatedState(onLoadMore)
    LaunchedEffect(nearEnd, enabled) {
        if (nearEnd && enabled) load()
    }
}
