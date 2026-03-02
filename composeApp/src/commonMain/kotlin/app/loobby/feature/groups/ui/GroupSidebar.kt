package app.loobby.feature.groups.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun GroupSidebar(
    onProfileClick: () -> Unit,
    onGroupSelected: (String) -> Unit,
) {

    val groups = listOf(
        "1" to "Vôleiarteiros",
        "2" to "Amigos da Quadra",
        "3" to "Beach Volley"
    )

    Column(
        modifier = Modifier
            .width(90.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        // Perfil
        Box(
            modifier = Modifier
                .size(56.dp)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("👤")
        }

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(groups) { group ->
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clickable { onGroupSelected(group.first) },
                    contentAlignment = Alignment.Center
                ) {
                    Card { Text("🏐") }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { /* criar grupo */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+")
                }
            }
        }
    }
}