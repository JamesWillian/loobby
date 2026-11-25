package app.loobby.feature.gameplays.presentation.form

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import app.loobby.core.ui.collectAsImmediateState
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.core.navigation.ScopedViewModelStore
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.ui.components.AppSheet
import app.loobby.core.ui.components.ConfirmDialog
import app.loobby.feature.games.presentation.GameSearchPicker
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.presentation.form.components.DetailsStep
import app.loobby.feature.gameplays.presentation.form.components.ModeStep
import app.loobby.feature.gameplays.presentation.model.toGameChoice
import app.loobby.feature.gameplays.presentation.model.typedGameChoice
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Criar ou editar gameplay, num sheet. O rascunho vive só enquanto o sheet está
 * aberto; fechar com alterações pede confirmação.
 */
@Composable
fun GameplayFormSheet(
    mode: GameplayFormMode,
    onDismiss: () -> Unit,
    onSaved: (Gameplay) -> Unit,
) {
    ScopedViewModelStore {
        GameplayFormSheetContent(mode = mode, onDismiss = onDismiss, onSaved = onSaved)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameplayFormSheetContent(
    mode: GameplayFormMode,
    onDismiss: () -> Unit,
    onSaved: (Gameplay) -> Unit,
    vm: GameplayFormViewModel = koinViewModel { parametersOf(mode) },
) {
    val state by vm.state.collectAsImmediateState()
    val isOnline = LocalIsOnline.current
    TrackScreen(if (state.isEdit) AppScreen.EDIT_GAMEPLAY else AppScreen.CREATE_GAMEPLAY)

    LaunchedEffect(vm) { vm.saved.collect(onSaved) }

    var confirmDiscard by remember { mutableStateOf(false) }
    val isDirty by rememberUpdatedState(state.isDirty)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            if (value == SheetValue.Hidden && isDirty) {
                confirmDiscard = true
                false
            } else {
                true
            }
        },
    )
    if (confirmDiscard) {
        ConfirmDialog(
            title = "Descartar a gameplay?",
            message = "O que você preencheu vai se perder.",
            confirmLabel = "Descartar",
            dismissLabel = "Continuar editando",
            destructive = true,
            onConfirm = { confirmDiscard = false; onDismiss() },
            onDismiss = { confirmDiscard = false },
        )
    }

    AppSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        title = if (state.isEdit) "Editar gameplay" else "Nova gameplay",
    ) {
        when (state.step) {
            FormStep.MODE -> ModeStep(onSelect = vm::selectMode)
            FormStep.DETAILS -> DetailsStep(state = state, actions = vm, isOnline = isOnline)
            FormStep.PICK_GAME -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = vm::closeGamePicker) { Text("← Voltar") }
                    Text(
                        text = "Escolha o jogo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                GameSearchPicker(
                    onPickCatalogGame = { vm.onGamePicked(it.toGameChoice()) },
                    onPickTypedName = { vm.onGamePicked(typedGameChoice(it)) },
                )
            }
        }
    }
}
