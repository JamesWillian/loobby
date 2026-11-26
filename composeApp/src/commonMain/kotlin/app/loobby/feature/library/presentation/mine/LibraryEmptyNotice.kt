package app.loobby.feature.library.presentation.mine

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** O aviso da biblioteca sem jogos, ou do filtro sem nenhum marcado. */
@Composable
fun LibraryEmptyNotice(filter: LibraryFilter, modifier: Modifier = Modifier) {
    Text(
        text = when (filter) {
            LibraryFilter.ALL ->
                "Adicione os jogos que você joga e marque onde tem cada um. Nas gameplays, o grupo vê " +
                    "quem vai e tem o jogo."
            LibraryFilter.OWNED -> "Nenhum jogo com o \"tenho o jogo\" marcado."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
