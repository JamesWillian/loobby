package app.loobby.feature.auth.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * BottomSheet de autenticação.
 * Alterna entre LoginScreen e RegisterScreen internamente.
 *
 * @param onDismiss chamado quando o usuário quer fechar (login OK, register OK, ou "continuar sem registrar")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    onDismiss: () -> Unit,
    vm: AuthViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) {
            vm.resetDismiss()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 2.dp
    ) {
        AnimatedContent(
            targetState = state.showRegisterScreen,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "auth_sheet_transition"
        ) { showRegister ->
            if (showRegister) {
                RegisterSheetContent(
                    state = state,
                    onEmailChanged = vm::onRegisterEmailChanged,
                    onPasswordChanged = vm::onRegisterPasswordChanged,
                    onConfirmPasswordChanged = vm::onRegisterConfirmPasswordChanged,
                    onRegisterClick = vm::register,
                    onBackToLoginClick = vm::navigateBackToLogin,
                    onContinueWithoutRegister = vm::dismiss
                )
            } else {
                LoginSheetContent(
                    state = state,
                    onEmailChanged = vm::onLoginEmailChanged,
                    onPasswordChanged = vm::onLoginPasswordChanged,
                    onLoginClick = vm::login,
                    onRegisterClick = vm::navigateToRegister,
                    onContinueWithoutRegister = vm::dismiss
                )
            }
        }
    }
}