package app.loobby.feature.library.presentation.mine

import app.loobby.feature.library.domain.model.LibraryGame

/** O filtro da biblioteca: todos os jogos, ou só os do "tenho o jogo". */
enum class LibraryFilter { ALL, OWNED }

data class MyLibraryUiState(
    /** A primeira carga, sem nada para mostrar ainda. */
    val isLoading: Boolean = true,
    val games: List<LibraryGame> = emptyList(),
    val filter: LibraryFilter = LibraryFilter.ALL,
    /** O jogo aberto no sheet do "tenho o jogo". */
    val openGameId: String? = null,
    /** Uma escrita em andamento: os botões esperam. */
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val shownGames: List<LibraryGame>
        get() = if (filter == LibraryFilter.OWNED) games.filter { it.isOwned } else games

    val ownedCount: Int get() = games.count { it.isOwned }

    val openGame: LibraryGame? get() = openGameId?.let { id -> games.firstOrNull { it.catalogId == id } }

    val isEmpty: Boolean get() = !isLoading && games.isEmpty()
}
