package app.loobby.feature.library.presentation.mine

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.SectionTitle
import app.loobby.feature.library.presentation.components.gamesCountLabel

/** "Biblioteca · 12 jogos", com o Adicionar. */
@Composable
fun LibrarySectionHeader(
    title: String,
    count: Int,
    canAdd: Boolean,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionTitle(
        text = title,
        detail = gamesCountLabel(count),
        modifier = modifier,
        trailing = {
            TextButton(onClick = onAdd, enabled = canAdd) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Adicionar")
            }
        },
    )
}
