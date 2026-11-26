package app.loobby.feature.games.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import app.loobby.core.navigation.ScopedViewModelStore
import app.loobby.core.ui.components.AppSheet
import app.loobby.feature.games.domain.model.GameDomain

/**
 * O picker de jogo num sheet próprio. A busca começa limpa a cada abertura. Sem
 * [onPickTypedName], só vale jogo do catálogo; [emptyQueryContent] é a escolha
 * rápida, com a busca vazia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePickerSheet(
    title: String,
    onDismiss: () -> Unit,
    onPickCatalogGame: (GameDomain) -> Unit,
    onPickTypedName: ((String) -> Unit)?,
    emptyQueryContent: @Composable () -> Unit = {},
) {
    ScopedViewModelStore {
        AppSheet(onDismiss = onDismiss, title = title) {
            GameSearchPicker(
                onPickCatalogGame = onPickCatalogGame,
                onPickTypedName = onPickTypedName,
                emptyQueryContent = emptyQueryContent,
            )
        }
    }
}
