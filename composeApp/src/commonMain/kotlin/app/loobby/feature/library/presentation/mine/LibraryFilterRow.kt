package app.loobby.feature.library.presentation.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.theme.Spacing

/** Os filtros da biblioteca: todos os jogos, ou só os do "tenho o jogo". */
@Composable
fun LibraryFilterRow(
    filter: LibraryFilter,
    total: Int,
    owned: Int,
    onFilter: (LibraryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        FilterChip(
            selected = filter == LibraryFilter.ALL,
            onClick = { onFilter(LibraryFilter.ALL) },
            label = { Text("Todos ($total)") },
        )
        FilterChip(
            selected = filter == LibraryFilter.OWNED,
            onClick = { onFilter(LibraryFilter.OWNED) },
            label = { Text("Tenho o jogo ($owned)") },
        )
    }
}
