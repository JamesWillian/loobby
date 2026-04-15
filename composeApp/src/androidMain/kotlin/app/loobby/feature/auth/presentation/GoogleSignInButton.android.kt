package app.loobby.feature.auth.presentation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import app.loobby.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
actual fun GoogleSignInButton(
    onSuccess: (idToken: String) -> Unit,
    onError: (message: String) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    OutlinedButton(
        onClick = {
            scope.launch {
                val result = performGoogleSignIn(context, credentialManager)
                if (result != null) {
                    onSuccess(result)
                } else {
                    onError("Token não disponível")
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

suspend fun performGoogleSignIn(
    context: Context,
    credentialManager: CredentialManager
): String? {
    val webClientId = context.getString(R.string.web_client_id)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setNonce("qualquer_string_aleatoria_para_teste")
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleIdTokenCredential.idToken
        } else {
            null
        }
    } catch (e: GetCredentialException) {
        Log.e("Auth", "Tipo de Erro: ${e::class.java.simpleName}")
        Log.e("Auth", "Mensagem: ${e.message}")
        null
    }
}
