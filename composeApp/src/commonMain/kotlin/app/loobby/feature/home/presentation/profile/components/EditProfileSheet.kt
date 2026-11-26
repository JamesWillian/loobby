package app.loobby.feature.home.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.core.ui.components.AppSheet
import app.loobby.core.ui.components.InlineError
import app.loobby.core.ui.components.LoadingButton
import app.loobby.theme.Spacing

/**
 * Editar o nome de exibição e o @. O @ só muda com o e-mail confirmado
 * ([canEditUsername]); o nome, sempre.
 *
 * O rascunho fica aqui, com o cursor: o ProfileViewModel só recebe os valores ao
 * salvar. Passar cada tecla por ele deixava o cursor para trás, e as letras
 * digitadas depressa caíam fora de ordem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    initialDisplayname: String,
    initialUsername: String,
    canEditUsername: Boolean,
    isSaving: Boolean,
    isOnline: Boolean,
    errorMessage: String?,
    onSave: (displayname: String, username: String) -> Unit,
    onDismiss: () -> Unit,
) {
    TrackScreen(AppScreen.PROFILE)
    var displayname by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(atEnd(initialDisplayname)) }
    var username by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(atEnd(initialUsername)) }

    AppSheet(onDismiss = onDismiss, title = "Editar perfil") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            OutlinedTextField(
                value = displayname,
                onValueChange = { displayname = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome de exibição") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            OutlinedTextField(
                value = username,
                onValueChange = { typed ->
                    val clean = sanitizeUsername(typed.text)
                    username = if (clean == typed.text) typed else atEnd(clean)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
                enabled = canEditUsername,
                supportingText = {
                    Text(
                        if (canEditUsername) "Mínimo 3 caracteres, máximo 30"
                        else "Confirme seu e-mail para alterar o username",
                    )
                },
            )
            InlineError(message = errorMessage)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                LoadingButton(
                    text = "Salvar",
                    onClick = { onSave(displayname.text, username.text) },
                    isLoading = isSaving,
                    enabled = isOnline && !isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "Os outros veem seu nome nas gameplays e nos grupos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** O texto com o cursor no fim. */
private fun atEnd(text: String) = TextFieldValue(text, TextRange(text.length))

/**
 * Só o que vale num username: letras minúsculas (as maiúsculas viram minúsculas),
 * dígitos e underline, sem underline no começo.
 */
private fun sanitizeUsername(raw: String): String =
    raw.lowercase().filter { it in 'a'..'z' || it in '0'..'9' || it == '_' }.trimStart('_')
