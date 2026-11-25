package app.loobby.feature.gameplays.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.model.Platform

// Os nomes e ícones que a pessoa vê para os valores do domínio.

val GameplayMode.label: String
    get() = when (this) {
        GameplayMode.VOTING -> "Votação"
        GameplayMode.FIXED -> "Jogo definido"
        GameplayMode.FREE -> "Livre"
    }

/** Uma linha sobre o modo, para quem está escolhendo. */
val GameplayMode.explanation: String
    get() = when (this) {
        GameplayMode.VOTING -> "A galera vota no jogo"
        GameplayMode.FIXED -> "Você já sabe o que vão jogar"
        GameplayMode.FREE -> "Decidem na hora"
    }

val GameplayMode.icon: ImageVector
    get() = when (this) {
        GameplayMode.VOTING -> Icons.Outlined.HowToVote
        GameplayMode.FIXED -> Icons.Outlined.VideogameAsset
        GameplayMode.FREE -> Icons.Outlined.Shuffle
    }

val Platform.label: String
    get() = when (this) {
        Platform.PC -> "PC"
        Platform.PLAYSTATION -> "PlayStation"
        Platform.XBOX -> "Xbox"
        Platform.SWITCH -> "Switch"
        Platform.MOBILE -> "Celular"
    }

val Platform.icon: ImageVector
    get() = when (this) {
        Platform.PC -> Icons.Outlined.Computer
        Platform.MOBILE -> Icons.Outlined.PhoneAndroid
        Platform.PLAYSTATION, Platform.XBOX, Platform.SWITCH -> Icons.Outlined.SportsEsports
    }

val ParticipationStatus.label: String
    get() = when (this) {
        ParticipationStatus.GOING -> "Vou"
        ParticipationStatus.NOT_GOING -> "Não vou"
        ParticipationStatus.PENDING -> "Pendente"
    }

/** "1 vai", "3 vão". */
fun goingLabel(count: Int): String = if (count == 1) "1 vai" else "$count vão"

/** "1 jogo", "6 jogos". */
fun gamesLabel(count: Int): String = if (count == 1) "1 jogo" else "$count jogos"
