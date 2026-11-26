package app.loobby.feature.gameplays.presentation.ended

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import app.loobby.core.ui.LoadMoreEffect
import app.loobby.core.ui.components.InlineError
import app.loobby.feature.gameplays.presentation.components.EndedGameplayCard
import app.loobby.theme.Spacing

/**
 * As encerradas, da mais recente: puxar atualiza, e rolar até o fim traz as mais
 * antigas. É a lista do Histórico e das Finalizadas do grupo; [emptyContent] é o
 * que aparece quando não há nenhuma.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EndedGameplaysList(
    state: EndedListUiState,
    showGroup: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenGameplay: (gameplayId: String) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit,
) {
    val listState = rememberLazyListState()
    LoadMoreEffect(listState = listState, enabled = state.canLoadMore, onLoadMore = onLoadMore)

    Column(modifier = modifier.fillMaxSize()) {
        if (state.isLoading && state.gameplays.isEmpty()) {
            LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        InlineError(
            message = state.errorMessage,
            modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs),
        )
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = Spacing.screen,
                    top = Spacing.sm,
                    end = Spacing.screen,
                    bottom = Spacing.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.gameplays, key = { it.id }) { gameplay ->
                    EndedGameplayCard(
                        model = gameplay,
                        showGroup = showGroup,
                        onClick = { onOpenGameplay(gameplay.id) },
                    )
                }
                if (state.isEmpty) {
                    item(key = "empty") { emptyContent() }
                }
                if (state.isLoadingMore || state.loadMoreFailed) {
                    item(key = "more") { LoadMoreFooter(isLoading = state.isLoadingMore, onRetry = onLoadMore) }
                }
            }
        }
    }
}
