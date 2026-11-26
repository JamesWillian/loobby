package app.loobby.feature.library.presentation.group

import app.loobby.feature.library.domain.model.GroupLibraryGame

data class GroupLibraryUiState(
    val isLoading: Boolean = true,
    val games: List<GroupLibraryGame> = emptyList(),
    /** Uma escrita em andamento: os botões esperam. */
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
