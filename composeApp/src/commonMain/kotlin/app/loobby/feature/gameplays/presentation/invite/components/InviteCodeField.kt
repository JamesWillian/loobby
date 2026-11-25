package app.loobby.feature.gameplays.presentation.invite.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.util.rememberPasteFromClipboard
import app.loobby.feature.gameplays.domain.model.InviteCode

/**
 * O código de convite. O "$-" fica fixo à esquerda, fora do texto: a pessoa não
 * precisa digitar nem consegue apagar. Colar um código inteiro também funciona.
 */
@Composable
fun InviteCodeField(
    code: String,
    enabled: Boolean,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val paste = rememberPasteFromClipboard()
    OutlinedTextField(
        value = code,
        onValueChange = onCodeChange,
        label = { Text("Código de convite") },
        placeholder = { Text("ABC123 ou AB12CD34") },
        prefix = { Text(InviteCode.PREFIX) },
        singleLine = true,
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = { paste()?.let(onCodeChange) }, enabled = enabled) {
                Icon(Icons.Outlined.ContentPaste, contentDescription = "Colar código")
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}
