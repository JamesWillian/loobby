package app.loobby.feature.home.presentation.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.loobby.core.lifecycle.OnResumeEffect
import app.loobby.core.ui.components.ScreenHeader
import app.loobby.feature.gameplays.presentation.ended.EndedGameplaysList
import org.koin.compose.viewmodel.koinViewModel

/**
 * A aba Histórico: as gameplays encerradas em que a pessoa marcou Vou, da mais
 * recente, com o que cada uma jogou. As de um grupo de que ela saiu não aparecem.
 */
@Composable
fun HistoryScreen(
    isAnonymous: Boolean,
    onLogin: () -> Unit,
    onOpenGameplay: (gameplayId: String) -> Unit,
    modifier: Modifier = Modifier,
    vm: HistoryViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsState()

    // A conta pode mudar com a tela aberta (o anônimo que cria conta é o mesmo usuário).
    LaunchedEffect(isAnonymous) { vm.refresh() }
    OnResumeEffect { vm.refresh() }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Histórico", subtitle = "As gameplays em que você foi")
        EndedGameplaysList(
            state = state,
            showGroup = true,
            onRefresh = { vm.refresh(pulled = true) },
            onLoadMore = vm::loadMore,
            onOpenGameplay = onOpenGameplay,
        ) {
            HistoryEmptyState(isAnonymous = isAnonymous, onLogin = onLogin)
        }
    }
}
