package app.loobby.feature.gameplays.presentation.grouphome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.loobby.core.lifecycle.OnResumeEffect
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.ui.LocalAppSnackbar
import app.loobby.core.ui.components.InlineError
import app.loobby.core.ui.rememberIsAtTop
import app.loobby.feature.gameplays.presentation.components.GroupGameplayCard
import app.loobby.feature.gameplays.presentation.components.NewGameplayFab
import app.loobby.feature.gameplays.presentation.components.NewGameplayFabClearance
import app.loobby.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * O Início de um grupo: as gameplays ativas, respondidas e votadas no próprio card.
 * As encerradas ficam nas Finalizadas, pelo botão do topo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GroupHomeScreen(
    groupId: String,
    groupName: String,
    isRailOpen: Boolean,
    canCreate: Boolean,
    onToggleRail: () -> Unit,
    onOpenGroupDetail: () -> Unit,
    onOpenEnded: () -> Unit,
    onOpenGameplay: (gameplayId: String) -> Unit,
    onNewGameplay: () -> Unit,
    vm: GroupHomeViewModel = koinViewModel(key = "group-home:$groupId") { parametersOf(groupId) },
) {
    val state by vm.state.collectAsState()
    val snackbar = LocalAppSnackbar.current
    val isOnline = LocalIsOnline.current
    val listState = rememberLazyListState()
    val isListAtTop by listState.rememberIsAtTop()

    // Com alguma gameplay ativa, a nova sai do botão flutuante; sem nenhuma, do
    // placeholder da lista. Sem rede, o botão some: criar depende da API.
    val showNewGameplayFab = canCreate && isOnline && state.cards.isNotEmpty()

    LaunchedEffect(Unit) { vm.refresh() }
    OnResumeEffect { vm.refresh() }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Column(modifier = Modifier.fillMaxSize()) {
        GroupHomeHeader(
            groupName = groupName,
            memberCount = state.memberCount,
            isRailOpen = isRailOpen,
            onToggleRail = onToggleRail,
            onOpenGroupDetail = onOpenGroupDetail,
            onOpenEnded = onOpenEnded,
        )
        if (state.isLoading && state.cards.isEmpty()) {
            LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        InlineError(
            message = state.errorMessage,
            modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs),
        )
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { vm.refresh(pulled = true) },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = Spacing.screen,
                        top = Spacing.sm,
                        end = Spacing.screen,
                        bottom = if (showNewGameplayFab) NewGameplayFabClearance else Spacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.cards, key = { it.id }) { card ->
                        GroupGameplayCard(
                            model = card.model,
                            detail = card.detail,
                            isBusy = card.isBusy || !isOnline,
                            onOpen = { onOpenGameplay(card.id) },
                            onRespond = { vm.respond(card.id, it) },
                            onVote = { vm.vote(card.id, it) },
                        )
                    }
                    if (state.isEmpty) {
                        item(key = "empty") {
                            GroupHomeEmptyState(
                                canCreate = canCreate,
                                isOnline = isOnline,
                                onNewGameplay = onNewGameplay,
                            )
                        }
                    }
                }
            }
            NewGameplayFab(
                visible = showNewGameplayFab,
                expanded = isListAtTop,
                onClick = onNewGameplay,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}
