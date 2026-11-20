package app.loobby.core.appconfig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties

/**
 * O aviso do /app/config. Some com "Entendi" ou com o voltar do aparelho, e não volta;
 * o toque fora do diálogo não o fecha, para ninguém perder o aviso sem querer.
 */
@Composable
fun AppNoticeDialog(
    notice: AppNotice,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        icon = {
            Icon(
                imageVector = Icons.Outlined.Campaign,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = notice.title, textAlign = TextAlign.Center)
        },
        text = {
            // O texto vem da API e pode crescer: rola em vez de empurrar o botão para fora.
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = notice.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Entendi")
            }
        }
    )
}
