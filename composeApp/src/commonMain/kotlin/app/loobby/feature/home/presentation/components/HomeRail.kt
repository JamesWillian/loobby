package app.loobby.feature.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.loobby.core.navigation.AppRoute
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.groupImagePlaceholder
import app.loobby.resources.Res
import app.loobby.resources.app_icon
import app.loobby.theme.Spacing
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource

/**
 * A barra lateral do Início: o ícone do app (o Lobby) no topo, os grupos e o "+"
 * fixo embaixo. [current] é a raiz do Início, para marcar o item escolhido.
 */
@Composable
fun HomeRail(
    groups: List<GroupDomain>,
    current: AppRoute,
    onSelectLobby: () -> Unit,
    onSelectGroup: (GroupDomain) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        RailItem(selected = current is AppRoute.Lobby, onClick = onSelectLobby, description = "Lobby") {
            Image(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        HorizontalDivider(modifier = Modifier.width(32.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(bottom = Spacing.sm),
        ) {
            items(groups, key = { it.id }) { group ->
                RailItem(
                    selected = (current as? AppRoute.GroupHome)?.groupId == group.id,
                    onClick = { onSelectGroup(group) },
                    description = group.name,
                ) {
                    AsyncImage(
                        model = group.imageUrl ?: groupImagePlaceholder(group.name),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        RailAddButton(onClick = onAdd)
    }
}

/** O "+" da barra: nova gameplay, criar grupo ou entrar com convite. */
@Composable
private fun RailAddButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "Nova gameplay, criar grupo ou entrar com convite",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
