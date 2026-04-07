package app.loobby.feature.groups.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.domain.model.UserFeedDomain
import app.loobby.groupImagePlaceholder
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage

@Composable
fun GroupSidebar(
    isLoading: Boolean,
    feed: List<UserFeedDomain>,             // ← alterado: recebe feed ao invés de groups
    selectedFeedId: String?,                // ← alterado: id genérico do item selecionado
    userAvatarUrl: String? = null,
    onProfileClick: () -> Unit,
    onFeedItemSelected: (id: String, type: FeedType) -> Unit,  // ← alterado: callback genérico
    onCreateOrJoinClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        // Perfil
        RoundSidebarButton(
            imageUrl = userAvatarUrl ?: userAvatarPlaceholder(),
            onClick = onProfileClick
        )

        Spacer(Modifier.height(14.dp))

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {

            items(feed, key = { it.id }) { item ->
                FeedSidebarItem(
                    item = item,
                    selected = selectedFeedId == item.id,
                    onClick = {
                        onFeedItemSelected(item.id, item.entryType)
                    }
                )
            }

            item {
                Spacer(Modifier.height(6.dp))

                RoundSidebarButton(
                    imageUrl = "https://cdn-icons-png.flaticon.com/512/8922/8922789.png",
                    onClick = onCreateOrJoinClick,
                    modifier = Modifier.size(52.dp)
                )
            }
        }
    }
}

/**
 * Item do feed na sidebar.
 * - GROUP → shape quadrado arredondado (como antes)
 * - EVENT → shape circular para diferenciar visualmente
 * - Eventos finalizados → opacidade reduzida
 */
@Composable
fun FeedSidebarItem(
    item: UserFeedDomain,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
    }

    val size = 54.dp
    // Eventos finalizados ficam com opacidade reduzida
    val itemAlpha = if (item.isFinished) 0.45f else 1f
    // Eventos instantâneos usam shape circular; grupos usam shape arredondado
    val shape = if (item.entryType == FeedType.EVENT) CircleShape else ShapeDefaults.Large

    val imageUrl = item.imageUrl ?: groupImagePlaceholder(item.name)

    Card(
        modifier = modifier
            .size(size)
            .alpha(itemAlpha)
            .clickable(onClick = onClick),
        shape = shape,
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        SidebarItem(imageUrl)
    }
}

@Composable
fun GroupSidebarItem(imageUrl: String,
                     selected: Boolean,
                     onClick: () -> Unit,
                     modifier: Modifier = Modifier
) {

    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
    }

    val size = 54.dp

    Card(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
        shape = ShapeDefaults.Large,
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        SidebarItem(imageUrl)
    }
}

@Composable
fun SidebarItem(
    imageUrl: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        )
    }
}

@Composable
private fun RoundSidebarButton(
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val size = 54.dp

    Card(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
        shape = CircleShape,
    ) {
        SidebarItem(imageUrl)
    }
}

data class SidebarGroupItem(
    val id: String,
    val name: String,
    val imageUrl: String
)