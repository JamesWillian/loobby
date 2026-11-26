package app.loobby.feature.library.presentation.picks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.loobby.core.analytics.GamePickSource
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.library.domain.model.LibrarySource
import app.loobby.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val MAX_SHOWN = 30

/**
 * A escolha rápida do picker de jogo: com a busca vazia, os jogos da biblioteca do
 * grupo (na gameplay do grupo) ou os da pessoa (na única). Os de [exclude] já estão
 * na gameplay e ficam de fora. Sem nenhum jogo, não aparece nada.
 */
@Composable
fun LibraryQuickPicks(
    source: LibrarySource,
    onPick: (game: GameChoice, source: GamePickSource) -> Unit,
    modifier: Modifier = Modifier,
    exclude: Set<String> = emptySet(),
    vm: LibraryPicksViewModel = koinViewModel(key = "library-picks:${source.cacheKey}") { parametersOf(source) },
) {
    val picks by vm.picks.collectAsState()
    val available = picks.filter { it.catalogId !in exclude }
    if (available.isEmpty()) return

    val pickSource = if (source is LibrarySource.Group) GamePickSource.GROUP_LIBRARY else GamePickSource.MY_LIBRARY
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = if (source is LibrarySource.Group) "Da biblioteca do grupo" else "Da sua biblioteca",
            style = MaterialTheme.typography.labelLarge,
        )
        available.take(MAX_SHOWN).forEach { game ->
            QuickPickRow(game = game, onClick = { onPick(game, pickSource) })
        }
        if (available.size > MAX_SHOWN) {
            Text(
                text = "Busque pelo nome para achar os outros.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val LibrarySource.cacheKey: String
    get() = when (this) {
        LibrarySource.Mine -> "me"
        is LibrarySource.Group -> "group:$groupId"
    }
