package app.loobby.feature.library.presentation.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.InlineError
import app.loobby.feature.library.domain.model.LibraryRules
import app.loobby.feature.library.presentation.components.LibraryGameRow
import app.loobby.feature.library.presentation.components.ownedLine
import app.loobby.theme.Spacing

/**
 * A biblioteca da pessoa como itens de uma lista que já existe (a do Perfil): o
 * cabeçalho com o Adicionar, os filtros e um jogo por linha.
 */
fun LazyListScope.myLibraryItems(
    state: MyLibraryUiState,
    isOnline: Boolean,
    onAdd: () -> Unit,
    onFilter: (LibraryFilter) -> Unit,
    onOpenGame: (catalogId: String) -> Unit,
) {
    item(key = "library-header") {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            LibrarySectionHeader(
                title = "Biblioteca",
                count = state.games.size,
                canAdd = isOnline && !state.isSaving && state.games.size < LibraryRules.MAX_MINE,
                onAdd = onAdd,
            )
            InlineError(message = state.errorMessage)
            if (state.games.isNotEmpty()) {
                LibraryFilterRow(
                    filter = state.filter,
                    total = state.games.size,
                    owned = state.ownedCount,
                    onFilter = onFilter,
                )
            }
        }
    }
    items(state.shownGames, key = { "library:${it.catalogId}" }) { game ->
        LibraryGameRow(
            name = game.name,
            coverUrl = game.coverUrl,
            detail = game.ownedLine(),
            onClick = { onOpenGame(game.catalogId) },
        )
    }
    if (!state.isLoading && state.shownGames.isEmpty()) {
        item(key = "library-empty") { LibraryEmptyNotice(filter = state.filter) }
    }
}
