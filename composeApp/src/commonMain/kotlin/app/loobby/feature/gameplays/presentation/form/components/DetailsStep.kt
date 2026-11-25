package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.loobby.core.ui.components.InlineError
import app.loobby.core.ui.components.LoadingButton
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.presentation.common.label
import app.loobby.feature.gameplays.presentation.form.GamePickTarget
import app.loobby.feature.gameplays.presentation.form.GameplayFormUiState
import app.loobby.feature.gameplays.presentation.form.GameplayFormActions
import app.loobby.theme.Spacing

/** O formulário da gameplay: onde, nome, quando, plataforma e o que o modo pede. */
@Composable
fun DetailsStep(
    state: GameplayFormUiState,
    actions: GameplayFormActions,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    val mode = state.mode ?: return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        if (!state.isEdit) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = actions::backToModes) { Text("← Modo") }
                Text(text = mode.label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }
        PlaceSelector(
            selected = state.place,
            options = state.placeOptions,
            isLocked = state.isPlaceLocked,
            onSelect = actions::onPlaceChange,
        )
        OutlinedTextField(
            value = state.name,
            onValueChange = actions::onNameChange,
            label = { Text("Nome *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = actions::onDescriptionChange,
            label = { Text("Descrição") },
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        ScheduleFields(
            dateDigits = state.dateDigits,
            timeDigits = state.timeDigits,
            onDateChange = actions::onDateChange,
            onTimeChange = actions::onTimeChange,
            onStartNow = actions::startNow,
        )
        PlatformSelector(selected = state.platform, onSelect = actions::onPlatformChange)

        ModeFields(mode = mode, state = state, actions = actions)

        InlineError(message = state.errorMessage)
        LoadingButton(
            text = if (state.isEdit) "Salvar" else "Criar gameplay",
            onClick = actions::submit,
            isLoading = state.isSaving,
            enabled = isOnline,
        )
    }
}

@Composable
private fun ModeFields(mode: GameplayMode, state: GameplayFormUiState, actions: GameplayFormActions) {
    when (mode) {
        GameplayMode.FIXED -> ChosenGameCard(
            game = state.fixedGame,
            onChoose = { actions.openGamePicker(GamePickTarget.FIXED_GAME) },
        )
        GameplayMode.VOTING -> {
            HorizontalDivider()
            VotingConfigFields(
                finalists = state.finalists,
                multiVote = state.multiVote,
                enabled = state.canConfigureVoting,
                onFinalistsChange = actions::onFinalistsChange,
                onMultiVoteChange = actions::onMultiVoteChange,
            )
            // Na edição, os jogos da votação mudam na tela da gameplay.
            if (!state.isEdit) {
                VotingGamesEditor(
                    games = state.votingGames,
                    onAdd = { actions.openGamePicker(GamePickTarget.VOTING_GAME) },
                    onRemove = actions::removeVotingGame,
                )
            }
        }
        GameplayMode.FREE -> Text(
            text = "Sem jogo definido: vocês decidem na hora.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
