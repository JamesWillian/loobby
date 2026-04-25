package app.loobby.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.loobby.core.network.LocalIsOnline
import org.koin.compose.koinInject

/**
 * BottomSheet para alterar a senha.
 * Acessível a partir do ProfileBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(
    onDismiss: () -> Unit,
    vm: ProfileViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isOnline = LocalIsOnline.current

    // Fecha automaticamente após sucesso
    LaunchedEffect(state.changePasswordSuccess) {
        if (state.changePasswordSuccess) {
            kotlinx.coroutines.delay(1500)
            vm.hideChangePassword()
            onDismiss()
        }
    }

    // Garante que ao sair de composição (swipe, back, click fora, sucesso
    // ou qualquer outro caminho) os campos de senha sejam limpos. O
    // ProfileViewModel é singleton — sem esse reset, os valores
    // persistiriam até o próximo logout/recompose.
    DisposableEffect(Unit) {
        onDispose {
            vm.hideChangePassword()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            vm.hideChangePassword()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 2.dp
    ) {
        ChangePasswordContent(
            currentPassword = state.currentPassword,
            newPassword = state.newPassword,
            confirmPassword = state.confirmNewPassword,
            isLoading = state.isChangingPassword,
            isOnline = isOnline,
            message = state.changePasswordMessage,
            isSuccess = state.changePasswordSuccess,
            onCurrentPasswordChanged = vm::onCurrentPasswordChanged,
            onNewPasswordChanged = vm::onNewPasswordChanged,
            onConfirmPasswordChanged = vm::onConfirmNewPasswordChanged,
            onSubmit = vm::changePassword
        )
    }
}

@Composable
private fun ChangePasswordContent(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    isOnline: Boolean,
    message: String?,
    isSuccess: Boolean,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Alterar senha",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Digite sua senha atual e escolha uma nova senha.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Senha atual
        OutlinedTextField(
            value = currentPassword,
            onValueChange = onCurrentPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Senha atual") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { currentVisible = !currentVisible }) {
                    Icon(
                        if (currentVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(Modifier.height(16.dp))

        // Nova senha
        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nova senha") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { newVisible = !newVisible }) {
                    Icon(
                        if (newVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            supportingText = { Text("Mínimo 6 caracteres") },
            shape = MaterialTheme.shapes.medium
        )

        Spacer(Modifier.height(12.dp))

        // Confirmar nova senha
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirmar nova senha") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        if (confirmVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }
            ),
            shape = MaterialTheme.shapes.medium
        )

        // Mensagem de sucesso/erro
        if (message != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                color = if (isSuccess) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && !isSuccess && isOnline,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Alterar senha", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}