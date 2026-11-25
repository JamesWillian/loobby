package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.clickableNoIndication
import app.loobby.feature.gameplays.presentation.form.PlaceOption

/** Onde: um dos grupos da pessoa ou "Única, por convite". Travado, só mostra. */
@Composable
fun PlaceSelector(
    selected: PlaceOption,
    options: List<PlaceOption>,
    isLocked: Boolean,
    onSelect: (PlaceOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            enabled = !isLocked,
            label = { Text("Onde") },
            trailingIcon = { if (!isLocked) Icon(Icons.Outlined.ExpandMore, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!isLocked) {
            // O campo é só leitura: uma camada por cima recebe o toque e abre o menu.
            Box(modifier = Modifier.matchParentSize().clickableNoIndication { open = true })
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        open = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
