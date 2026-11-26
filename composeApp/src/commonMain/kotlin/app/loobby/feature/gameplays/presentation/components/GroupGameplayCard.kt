package app.loobby.feature.gameplays.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.presentation.model.GameplayCardModel

/**
 * O card do Início do grupo. Além do resumo, responde e vota sem abrir a gameplay.
 * Os botões só aparecem quando a gameplay inteira ([detail]) chegou, porque vêm
 * das permissões dela.
 */
@Composable
fun GroupGameplayCard(
    model: GameplayCardModel,
    detail: Gameplay?,
    isBusy: Boolean,
    onOpen: () -> Unit,
    onRespond: (ParticipationStatus) -> Unit,
    onVote: (votingGameId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    GameplayCardSurface(onClick = onOpen, modifier = modifier) {
        GameplayCardHeader(model = model, showGroup = false)

        val voting = detail?.voting
        if (voting != null) {
            BallotPreview(
                voting = voting,
                canVote = detail.permissions.vote && !isBusy,
                onVote = onVote,
                onSeeAll = onOpen,
            )
        }
        ModeHighlightLine(highlight = model.highlight, isEnded = model.isEnded)
        GoingLine(goingCount = model.goingCount, avatars = model.goingAvatars)

        if (detail?.permissions?.respond == true) {
            ParticipationButtons(
                selected = model.myStatus,
                onSelect = onRespond,
                enabled = !isBusy,
            )
        }
    }
}
