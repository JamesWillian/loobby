package app.loobby.feature.gameplays.presentation.lobby

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
import app.loobby.core.ui.components.InlineError
import app.loobby.core.ui.components.ScreenHeader
import app.loobby.core.ui.rememberIsAtTop
import app.loobby.feature.gameplays.presentation.components.GameplaySummaryCard
import app.loobby.feature.gameplays.presentation.components.NewGameplayFab
import app.loobby.feature.gameplays.presentation.components.NewGameplayFabClearance
import app.loobby.feature.home.presentation.components.RailToggleButton
import app.loobby.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * O Lobby: as gameplays ativas da pessoa, de todos os grupos e as únicas. É a raiz
 * do Início quando o ícone do app está escolhido na barra lateral.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LobbyScreen(
    isRailOpen: Boolean,
    isAnonymous: Boolean,
    onToggleRail: () -> Unit,
    onOpenGameplay: (gameplayId: String) -> Unit,
    onNewGameplay: () -> Unit,
    onJoinInvite: () -> Unit,
    onCreateGroup: () -> Unit,
    onLogin: () -> Unit,
    vm: LobbyViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsState()
    val isOnline = LocalIsOnline.current
    val listState = rememberLazyListState()
    val isListAtTop by listState.rememberIsAtTop()

    // Com alguma gameplay ativa, a nova sai do botão flutuante; sem nenhuma, do
    // placeholder da lista. Sem rede, o botão some: criar depende da API.
    val showNewGameplayFab = !isAnonymous && isOnline && state.gameplays.isNotEmpty()

    // Volta e meia a lista muda por fora (uma gameplay nova, um voto noutra tela):
    // recarrega ao abrir, ao voltar para ela e ao voltar para o app.
    LaunchedEffect(Unit) { vm.refresh() }
    OnResumeEffect { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Lobby",
            subtitle = "Suas gameplays ativas",
            leading = { RailToggleButton(isRailOpen = isRailOpen, onToggle = onToggleRail) },
        )
        if (state.isLoading && state.gameplays.isEmpty()) {
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
                    items(state.gameplays, key = { it.id }) { gameplay ->
                        GameplaySummaryCard(model = gameplay, onClick = { onOpenGameplay(gameplay.id) })
                    }
                    if (state.isEmpty) {
                        item(key = "empty") {
                            LobbyEmptyState(
                                isAnonymous = isAnonymous,
                                onNewGameplay = onNewGameplay,
                                onJoinInvite = onJoinInvite,
                                onCreateGroup = onCreateGroup,
                                onLogin = onLogin,
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
