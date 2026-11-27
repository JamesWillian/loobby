package app.loobby.feature.gameplays.presentation.detail

import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayComment

data class GameplayDetailUiState(
    val gameplay: Gameplay? = null,
    val isLoading: Boolean = true,
    /** A gameplay não carregou e não há nada para mostrar. */
    val loadError: String? = null,
    /** A ação em andamento; os botões esperam ela terminar. */
    val pendingAction: DetailAction? = null,
    val comments: CommentsUiState = CommentsUiState(),
) {
    val isBusy: Boolean get() = pendingAction != null
}

enum class DetailAction { RESPOND, VOTE, ADD_GAME, REMOVE_GAME, TIEBREAK, END, DELETE, LEAVE }

data class CommentsUiState(
    val items: List<GameplayComment> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val mayHaveMore: Boolean = false,
    val draft: String = "",
    val isPosting: Boolean = false,
) {
    /** Quem denunciou um comentário não o vê mais. */
    val visible: List<GameplayComment> get() = items.filterNot { it.reportedByMe }
}

/** O que acontece uma vez só: um aviso no snackbar, a pessoa saiu ou a gameplay foi excluída. */
sealed interface GameplayDetailEvent {
    data class Message(val text: String) : GameplayDetailEvent
    data object Left : GameplayDetailEvent
    data object Deleted : GameplayDetailEvent
}
