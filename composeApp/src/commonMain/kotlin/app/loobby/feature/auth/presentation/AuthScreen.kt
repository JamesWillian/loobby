package app.loobby.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.core.media.rememberImagePicker
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AuthScreen() {
    val viewModel: AuthViewModel = koinInject()
    val state by viewModel.uiState.collectAsState()
    val picker = rememberImagePicker()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Text(
            text = when {
                state.isLoggedIn -> "Logado como ${state.session?.username ?: state.session?.userId}"
                state.isAnonymous -> "Usuário anônimo"
                else -> "Carregando sessão..."
            }
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Senha") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.login(email, password) }) {
                Text("Login")
            }
            Button(onClick = { viewModel.register(email, password) }) {
                Text("Registrar")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { viewModel.loadProfile() }) {
            Text("Carregar perfil (/users/me)")
        }

        state.profile?.let { profile ->
            Spacer(Modifier.height(8.dp))
            Text("ID: ${profile.id}")
            Text("Username: ${profile.username}")
            Text("Display: ${profile.displayname ?: "-"}")
            Text("Avatar: ${profile.avatarUrl ?: "-"}")
        }

        var newUsername by remember { mutableStateOf("") }
        var newDisplayname by remember { mutableStateOf("") }

        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Novo username (opcional)") },
            singleLine = true
        )

        OutlinedTextField(
            value = newDisplayname,
            onValueChange = { newDisplayname = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Novo displayname (opcional)") },
            singleLine = true
        )

        Button(
            onClick = {
                viewModel.updateProfile(
                    username = newUsername.takeIf { it.isNotBlank() },
                    displayname = newDisplayname.takeIf { it.isNotBlank() }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Atualizar perfil (PATCH /users/me)")
        }

        Button(onClick = {
            scope.launch {
                val picked = picker.pickImage() ?: return@launch
                viewModel.uploadAvatar(
                    fileName = picked.fileName,
                    bytes = picked.bytes,
                    contentType = picked.contentType
                )
            }
        }) {
            Text("Upload avatar")
        }
    }
}