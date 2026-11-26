package app.loobby.feature.games.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.theme.Spacing

const val DEFAULT_GAME_PICKER_HINT = "Busque no catálogo ou, se o jogo não estiver lá, digite o nome."

/** A dica do picker que só aceita jogo do catálogo (as bibliotecas). */
const val CATALOG_ONLY_GAME_PICKER_HINT = "Busque o jogo no catálogo."

/**
 * A escolha de um jogo: a busca no catálogo da RAWG (pela API) e, para o que não
 * está lá, o nome digitado ([allowTypedName]). Com a busca vazia, aparece
 * [emptyQueryContent]: a escolha rápida das bibliotecas. Só desenha; a busca com
 * espera entre as teclas fica em quem chama ([GameSearchViewModel] nas telas novas).
 *
 * As linhas da lista não são lazy porque o picker vive dentro de sheets que já
 * rolam: uma LazyColumn aninhada quebraria as medidas.
 */
@Composable
fun GamePickerContent(
    query: String,
    results: List<GameDomain>,
    isSearching: Boolean,
    manualName: String,
    onQueryChange: (String) -> Unit,
    onSelectGame: (GameDomain) -> Unit,
    onManualNameChange: (String) -> Unit,
    onUseManual: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = DEFAULT_GAME_PICKER_HINT,
    hasSearchError: Boolean = false,
    allowTypedName: Boolean = true,
    emptyQueryContent: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Buscar jogo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (isSearching) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )

        if (query.isBlank()) emptyQueryContent()

        results.forEach { game -> GameResultRow(game = game, onClick = { onSelectGame(game) }) }

        if (query.isNotBlank() && !isSearching && !hasSearchError && results.isEmpty()) {
            Text(
                text = "Nenhum jogo encontrado para \"$query\".",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (allowTypedName) {
            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            Text(text = "Não está no catálogo?", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = manualName,
                onValueChange = onManualNameChange,
                label = { Text("Nome do jogo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            Button(
                onClick = onUseManual,
                modifier = Modifier.fillMaxWidth(),
                enabled = manualName.isNotBlank(),
            ) { Text("Usar este nome") }
        }

        // A RAWG exige a atribuição.
        Text(
            text = "Dados de jogos fornecidos por RAWG.io",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
