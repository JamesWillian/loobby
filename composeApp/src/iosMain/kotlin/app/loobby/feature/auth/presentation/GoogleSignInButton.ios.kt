package app.loobby.feature.auth.presentation

import androidx.compose.runtime.Composable

@Composable
actual fun GoogleSignInButton(
    onSuccess: (idToken: String) -> Unit,
    onError: (message: String) -> Unit,
    enabled: Boolean
) {
    // iOS: implementar com GoogleSignIn-iOS via SPM quando chegar na Fase 4 iOS
}