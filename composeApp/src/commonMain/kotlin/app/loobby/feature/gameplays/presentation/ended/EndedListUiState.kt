package app.loobby.feature.gameplays.presentation.ended

import app.loobby.feature.gameplays.presentation.model.GameplayCardModel

/** Uma lista de encerradas em páginas: o Histórico ou as Finalizadas do grupo. */
data class EndedListUiState(
    /** A primeira carga, sem nada para mostrar ainda. */
    val isLoading: Boolean = true,
    /** A pessoa puxou a lista para baixo. */
    val isRefreshing: Boolean = false,
    /** A página seguinte está vindo. */
    val isLoadingMore: Boolean = false,
    /** A página seguinte falhou: o fim da lista oferece tentar de novo. */
    val loadMoreFailed: Boolean = false,
    val gameplays: List<GameplayCardModel> = emptyList(),
    /** A última página veio cheia: pode haver mais antigas. */
    val mayHaveMore: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && gameplays.isEmpty()
    val canLoadMore: Boolean get() = mayHaveMore && !isLoading && !isLoadingMore && !loadMoreFailed
}
