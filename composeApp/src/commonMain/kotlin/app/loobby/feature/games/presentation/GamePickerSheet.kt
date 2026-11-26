package app.loobby.feature.games.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import app.loobby.core.navigation.ScopedViewModelStore
import app.loobby.core.ui.components.AppSheet
import app.loobby.feature.games.domain.model.GameDomain

/** O picker de jogo num sheet próprio. A busca começa limpa a cada abertura. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePickerSheet(
    title: String,
    onDismiss: () -> Unit,
    onPickCatalogGame: (GameDomain) -> Unit,
    onPickTypedName: (String) -> Unit,
) {
    ScopedViewModelStore {
        AppSheet(onDismiss = onDismiss, title = title) {
            GameSearchPicker(
                onPickCatalogGame = onPickCatalogGame,
                onPickTypedName = onPickTypedName,
            )
        }
    }
}
