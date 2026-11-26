package app.loobby.feature.gameplays.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.presentation.model.GameplayCardModel

/** O card do Lobby: o resumo da gameplay e a resposta da pessoa. Tocar abre a gameplay. */
@Composable
fun GameplaySummaryCard(
    model: GameplayCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GameplayCardSurface(
        onClick = onClick,
        dimmed = model.myStatus == ParticipationStatus.NOT_GOING,
        modifier = modifier,
    ) {
        GameplayCardHeader(model = model, trailing = { ParticipationChip(model.myStatus) })
        ModeHighlightLine(highlight = model.highlight, isEnded = model.isEnded)
        GoingLine(goingCount = model.goingCount, avatars = model.goingAvatars)
    }
}
