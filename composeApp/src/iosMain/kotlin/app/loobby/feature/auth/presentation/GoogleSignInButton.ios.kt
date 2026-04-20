package app.loobby.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.feature.auth.domain.GoogleSignInProvider
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun GoogleSignInButton(
    onSuccess: (idToken: String) -> Unit,
    onError: (message: String) -> Unit,
    enabled: Boolean
) {
    val provider: GoogleSignInProvider = koinInject()
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            scope.launch {
                try {
                    val token = provider.signIn()
                    onSuccess(token)
                } catch (e: Throwable) {
                    onError(e.message ?: "Erro desconhecido")
                }
            }
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Continuar com Google",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}