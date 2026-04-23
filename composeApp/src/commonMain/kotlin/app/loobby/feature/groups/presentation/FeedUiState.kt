package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.domain.model.UserFeedDomain

/**
 * Estado da sidebar / feed unificado (grupos + eventos instantâneos).
 *
 * Foi extraído de [GroupsUiState] para desacoplar os estados:
 *  - [FeedUiState]    → lista lateral, item selecionado, persistência.
 *  - [GroupsUiState]  → detalhe do grupo, membros, CRUD de grupo, convites.
 */
data class FeedUiState(
    val isLoading: Boolean = true,
    val feed: List<UserFeedDomain> = emptyList(),
    val selectedFeedId: String? = null,
    val selectedFeedType: FeedType? = null,
    val errorMessage: String? = null
)
