package app.loobby.feature.gameplays.presentation.lobby

import app.loobby.feature.gameplays.presentation.model.GameplayCardModel

data class LobbyUiState(
    /** A primeira carga, sem nada para mostrar ainda. */
    val isLoading: Boolean = true,
    /** A pessoa puxou a lista para baixo. */
    val isRefreshing: Boolean = false,
    val gameplays: List<GameplayCardModel> = emptyList(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && gameplays.isEmpty()
}
