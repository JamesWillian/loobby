package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.core.network.LocalIsOnline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreateGroup: (name: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var hasAttempted by remember { mutableStateOf(false) }

    val nameError = hasAttempted && name.isBlank()
    val isOnline = LocalIsOnline.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = "Criar Grupo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Dê um nome ao seu grupo. Você poderá adicionar uma foto depois.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome do grupo") },
                placeholder = { Text("Ex: Vôleiarteiros") },
                singleLine = true,
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("O nome é obrigatório") }
                } else null,
                enabled = !isLoading
            )

            Button(
                onClick = {
                    hasAttempted = true
                    if (name.isNotBlank()) {
                        onCreateGroup(name.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading && isOnline,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isOnline) "Criar Grupo" else "Você está offline",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}