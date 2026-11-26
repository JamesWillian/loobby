package app.loobby.feature.gameplays.presentation.grouphome

import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplaySummary
import app.loobby.feature.gameplays.presentation.model.GameplayCardModel
import app.loobby.feature.gameplays.presentation.model.toCardModel

data class GroupHomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val memberCount: Int? = null,
    val cards: List<GroupGameplayCardState> = emptyList(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && cards.isEmpty()
}

/**
 * Um card do grupo. Nasce com o resumo da lista e ganha a gameplay inteira logo
 * depois: é ela que traz a cédula e os botões permitidos.
 */
data class GroupGameplayCardState(
    val summary: GameplaySummary,
    val detail: Gameplay? = null,
    /** Uma resposta ou um voto está a caminho. */
    val isBusy: Boolean = false,
) {
    val id: String get() = summary.id
    val model: GameplayCardModel get() = detail?.toCardModel() ?: summary.toCardModel()
}
