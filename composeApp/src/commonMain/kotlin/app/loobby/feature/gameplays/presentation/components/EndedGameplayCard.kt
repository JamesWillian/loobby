package app.loobby.feature.gameplays.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.feature.gameplays.presentation.model.GameplayCardModel

/**
 * O card de uma gameplay encerrada: o nome, o grupo ou "Única" ([showGroup]), a data
 * e a plataforma; o que se jogou; e quem foi. Tocar abre a gameplay, só para ver.
 */
@Composable
fun EndedGameplayCard(
    model: GameplayCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showGroup: Boolean = true,
) {
    GameplayCardSurface(onClick = onClick, modifier = modifier) {
        GameplayCardHeader(model = model, showGroup = showGroup)
        ModeHighlightLine(highlight = model.highlight, isEnded = true)
        GoingLine(goingCount = model.goingCount, avatars = model.goingAvatars)
    }
}
