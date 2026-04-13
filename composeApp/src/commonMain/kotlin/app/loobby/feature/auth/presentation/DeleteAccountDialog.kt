package app.loobby.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun DeleteAccountDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?,
    loading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(text = "Excluir conta")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Essa ação é permanente e irreversível. Grupos e eventos dos quais você é responsável serão transferidos ou excluídos.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Confirme sua senha") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = if (error != null) {
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = if (passwordVisible)
                                    "Ocultar senha"
                                else
                                    "Mostrar senha"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                    enabled = !loading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Excluir conta")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !loading
            ) {
                Text("Cancelar")
            }
        }
    )
}