package app.loobby.feature.auth.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun GoogleSignInButton(
    onSuccess: (idToken: String) -> Unit,
    onError: (message: String) -> Unit,
    enabled: Boolean = true
)