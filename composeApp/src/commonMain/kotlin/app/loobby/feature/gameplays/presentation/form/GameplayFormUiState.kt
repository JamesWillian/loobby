package app.loobby.feature.gameplays.presentation.form

import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.Platform

/** Para que o formulário abriu. */
sealed interface GameplayFormMode {
    /** Uma gameplay nova. Com [groupId], o grupo já vem escolhido e não muda. */
    data class Create(val groupId: String? = null, val groupName: String? = null) : GameplayFormMode

    data class Edit(val gameplay: Gameplay) : GameplayFormMode
}

enum class FormStep { MODE, DETAILS, PICK_GAME }

/** O picker de jogo abriu para escolher o jogo definido ou um jogo da votação. */
enum class GamePickTarget { FIXED_GAME, VOTING_GAME }

/** Onde a gameplay acontece: num grupo, ou única ([groupId] null). */
data class PlaceOption(val groupId: String?, val label: String) {
    companion object {
        val Single = PlaceOption(groupId = null, label = "Única, por convite")
    }
}

data class GameplayFormUiState(
    val isEdit: Boolean = false,
    val step: FormStep = FormStep.MODE,
    val mode: GameplayMode? = null,
    val place: PlaceOption = PlaceOption.Single,
    /** Vindo do grupo ou na edição, o lugar não muda. */
    val isPlaceLocked: Boolean = false,
    val placeOptions: List<PlaceOption> = listOf(PlaceOption.Single),
    val name: String = "",
    val description: String = "",
    /** DDMMAAAA. */
    val dateDigits: String = "",
    /** HHMM. */
    val timeDigits: String = "",
    val platform: Platform? = null,
    val fixedGame: GameChoice? = null,
    val finalists: Int = 1,
    val multiVote: Boolean = false,
    /** Na criação: os jogos que já entram na votação. */
    val votingGames: List<GameChoice> = emptyList(),
    /** Na edição: finalistas e voto múltiplo só mudam antes do primeiro voto. */
    val canConfigureVoting: Boolean = true,
    val pickTarget: GamePickTarget? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
)
