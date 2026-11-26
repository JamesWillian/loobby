package app.loobby.feature.gameplays.presentation.ended

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.loobby.core.lifecycle.OnResumeEffect
import app.loobby.core.ui.components.BackTopBar
import app.loobby.core.ui.components.EmptyState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * As Finalizadas do grupo, abertas pelo Início dele: as encerradas, da mais recente,
 * com o que cada uma jogou. Tocar abre a gameplay, só para ver.
 */
@Composable
fun EndedGameplaysScreen(
    groupId: String,
    groupName: String,
    onBack: () -> Unit,
    onOpenGameplay: (gameplayId: String) -> Unit,
    vm: EndedGameplaysViewModel = koinViewModel(key = "group-ended:$groupId") { parametersOf(groupId) },
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }
    OnResumeEffect { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize()) {
        BackTopBar(title = if (groupName.isBlank()) "Finalizadas" else "Finalizadas · $groupName", onBack = onBack)
        EndedGameplaysList(
            state = state,
            showGroup = false,
            onRefresh = { vm.refresh(pulled = true) },
            onLoadMore = vm::loadMore,
            onOpenGameplay = onOpenGameplay,
        ) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = "Nenhuma encerrada ainda",
                message = "As gameplays do grupo aparecem aqui quando encerram, com o que cada uma jogou.",
            )
        }
    }
}
