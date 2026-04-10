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
 * Tela ativa dentro do AuthBottomSheet.
 */
private enum class AuthScreen { LOGIN, REGISTER, FORGOT_PASSWORD }

/**
 * BottomSheet de autenticação.
 * Alterna entre Login, Register e ForgotPassword internamente.
 *
 * @param onDismiss chamado quando o usuário quer fechar
 * @param welcomeName nome de boas-vindas (se anônimo com nickname personalizado)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    onDismiss: () -> Unit,
    vm: AuthViewModel = koinInject(),
    welcomeName: String? = null
) {
    val state by vm.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Determinar qual tela mostrar
    val currentScreen = when {
        state.showForgotPassword -> AuthScreen.FORGOT_PASSWORD
        state.showRegisterScreen -> AuthScreen.REGISTER
        else -> AuthScreen.LOGIN
    }

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
            targetState = currentScreen,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "auth_sheet_transition"
        ) { screen ->
            when (screen) {
                AuthScreen.REGISTER -> {
                    RegisterSheetContent(
                        state = state,
                        onEmailChanged = vm::onRegisterEmailChanged,
                        onPasswordChanged = vm::onRegisterPasswordChanged,
                        onConfirmPasswordChanged = vm::onRegisterConfirmPasswordChanged,
                        onRegisterClick = vm::register,
                        onBackToLoginClick = vm::navigateBackToLogin,
                        onContinueWithoutRegister = vm::dismiss
                    )
                }
                AuthScreen.LOGIN -> {
                    LoginSheetContent(
                        state = state,
                        onEmailChanged = vm::onLoginEmailChanged,
                        onPasswordChanged = vm::onLoginPasswordChanged,
                        onLoginClick = vm::login,
                        onRegisterClick = vm::navigateToRegister,
                        onContinueWithoutRegister = vm::dismiss,
                        welcomeName = welcomeName,
                        onForgotPasswordClick = vm::showForgotPassword
                    )
                }
                AuthScreen.FORGOT_PASSWORD -> {
                    ForgotPasswordSheetContent(
                        state = state,
                        onEmailChanged = vm::onForgotPasswordEmailChanged,
                        onSendClick = vm::sendPasswordResetEmail,
                        onBackToLogin = vm::hideForgotPassword
                    )
                }
            }
        }
    }
}