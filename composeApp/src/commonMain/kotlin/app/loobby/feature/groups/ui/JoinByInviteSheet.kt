package app.loobby.feature.groups.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.core.util.rememberPasteFromClipboard
import app.loobby.feature.groups.domain.model.InvitePreview
import app.loobby.groupImagePlaceholder
import coil3.compose.AsyncImage

// Prefixo fixo dos códigos de convite. Fica renderizado no TextField via
// `prefix = { Text(INVITE_PREFIX) }` (fora do value), então o usuário não
// consegue apagar nem precisa digitar. Ao buscar, concatenamos esse prefixo
// com o que o usuário digitou para reconstituir o código completo.
private const val INVITE_PREFIX = "$-"

/**
 * Remove o prefixo [INVITE_PREFIX] do início de [raw], caso presente.
 * Usado tanto quando o usuário cola via OS (long-press → paste) quanto
 * pelo botão "Colar" do TextField, para evitar que o código apareça como
 * "$-$-ABC123" quando o que foi colado já tinha o prefixo.
 */
private fun stripInvitePrefix(raw: String): String =
    raw.trim().removePrefix(INVITE_PREFIX)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinByInviteSheet(
    isLoading: Boolean,
    invitePreview: InvitePreview?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSearchInvite: (code: String) -> Unit,
    onConfirmJoin: () -> Unit,
    onClearPreview: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // `code` guarda apenas o sufixo (depois do "$-"). O prefixo é renderizado
    // pelo próprio TextField e não faz parte do value.
    var code by remember { mutableStateOf("") }
    val pasteFromClipboard = rememberPasteFromClipboard()

    ModalBottomSheet(
        onDismissRequest = {
            onClearPreview()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Entrar com Convite",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Cole ou digite o código de convite para entrar em um grupo ou evento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = code,
                onValueChange = { raw ->
                    // Remove o prefixo se o usuário colar via OS um código completo
                    // (ex.: "$-ABC123" → "ABC123"), evitando a duplicação "$-$-ABC123".
                    code = stripInvitePrefix(raw)
                    if (invitePreview != null) onClearPreview()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Código de convite") },
                placeholder = { Text("Ex: ABC123 ou A1B2-C3D4") },
                // Prefixo fixo, fora do value. O usuário não consegue apagar.
                prefix = { Text(INVITE_PREFIX) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val pasted = pasteFromClipboard()
                            if (!pasted.isNullOrBlank()) {
                                code = stripInvitePrefix(pasted)
                                if (invitePreview != null) onClearPreview()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Outlined.ContentPaste,
                            contentDescription = "Colar código"
                        )
                    }
                },
                singleLine = true,
                enabled = !isLoading
            )

            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Preview card when invite is found
            if (invitePreview != null) {
                InvitePreviewCard(preview = invitePreview)

                Button(
                    onClick = onConfirmJoin,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Entrar", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Search button
                Button(
                    onClick = {
                        val suffix = code.trim()
                        if (suffix.isNotBlank()) {
                            // A VM espera o código completo. Reconstruímos
                            // concatenando o prefixo fixo com o que o usuário
                            // digitou/colou (que já está sem prefixo no state).
                            onSearchInvite(INVITE_PREFIX + suffix)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isLoading && code.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Buscar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitePreviewCard(preview: InvitePreview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (preview) {
                is InvitePreview.GroupPreview -> {

                    AsyncImage(
                        model = preview.imageUrl ?: groupImagePlaceholder(preview.name),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Text(
                        text = preview.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Grupo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is InvitePreview.EventPreview -> {
                    val emoji = preview.emoji ?: "📅"
                    Text(
                        text = emoji,
                        fontSize = 40.sp
                    )

                    Text(
                        text = preview.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Evento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}