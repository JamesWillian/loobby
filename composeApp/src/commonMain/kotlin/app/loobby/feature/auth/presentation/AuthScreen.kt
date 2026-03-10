package app.loobby.feature.auth.presentation

import androidx.compose.animation.*
import androidx.compose.runtime.*
import org.koin.compose.koinInject

/**
 * Tela host de autenticação.
 * Alterna entre LoginScreen e RegisterScreen internamente.
 *
 * @param onDismiss chamado quando o usuário quer fechar (login OK, register OK, ou "continuar sem registrar")
 */
@Composable
fun AuthScreen(
    onDismiss: () -> Unit,
    vm: AuthViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) {
            vm.resetDismiss()
            onDismiss()
        }
    }

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
        label = "auth_screen_transition"
    ) { showRegister ->
        if (showRegister) {
            RegisterScreen(
                state = state,
                onEmailChanged = vm::onRegisterEmailChanged,
                onPasswordChanged = vm::onRegisterPasswordChanged,
                onConfirmPasswordChanged = vm::onRegisterConfirmPasswordChanged,
                onRegisterClick = vm::register,
                onBackToLoginClick = vm::navigateBackToLogin,
                onContinueWithoutRegister = vm::dismiss
            )
        } else {
            LoginScreen(
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