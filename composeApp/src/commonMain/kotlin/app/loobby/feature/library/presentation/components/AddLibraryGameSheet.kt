package app.loobby.feature.library.presentation.components

import androidx.compose.runtime.Composable
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.games.presentation.GamePickerSheet

/** A busca no catálogo para pôr um jogo numa biblioteca. Só jogo do catálogo: sem o nome digitado. */
@Composable
fun AddLibraryGameSheet(
    title: String,
    screen: AppScreen,
    onDismiss: () -> Unit,
    onPick: (GameDomain) -> Unit,
) {
    TrackScreen(screen)
    GamePickerSheet(
        title = title,
        onDismiss = onDismiss,
        onPickCatalogGame = onPick,
        onPickTypedName = null,
    )
}
