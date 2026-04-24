package app.loobby.feature.groups.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import app.loobby.theme.LoobbyColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        // Separa o feed em eventos instantâneos (primeiro) e grupos (depois),
        // preservando a ordem original dentro de cada seção.
        val events = feed.filter { it.entryType == FeedType.EVENT }
        val groups = feed.filter { it.entryType == FeedType.GROUP }

        // Eventos ativos são exibidos sempre; finalizados só ao expandir.
        val activeEvents = events.filter { !it.isFinished }
        val finishedEvents = events.filter { it.isFinished }

        // Se o item selecionado é um evento finalizado, abre a seção automaticamente
        // para que ele fique visível na sidebar.
        val selectedIsFinishedEvent = finishedEvents.any { it.id == selectedFeedId }
        var showFinishedEvents by remember { mutableStateOf(false) }
        LaunchedEffect(selectedIsFinishedEvent) {
            if (selectedIsFinishedEvent) showFinishedEvents = true
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            if (events.isNotEmpty()) {
                item(key = "header-events") { SidebarSectionHeader("EVENTOS") }

                items(activeEvents, key = { it.id }) { item ->
                    FeedSidebarItem(
                        item = item,
                        selected = selectedFeedId == item.id,
                        onClick = {
                            onFeedItemSelected(item.id, item.entryType)
                        }
                    )
                }

                if (showFinishedEvents) {
                    items(finishedEvents, key = { it.id }) { item ->
                        FeedSidebarItem(
                            item = item,
                            selected = selectedFeedId == item.id,
                            onClick = {
                                onFeedItemSelected(item.id, item.entryType)
                            }
                        )
                    }
                }

                if (finishedEvents.isNotEmpty()) {
                    item(key = "toggle-finished-events") {
                        ExpandToggleButton(
                            expanded = showFinishedEvents,
                            onClick = { showFinishedEvents = !showFinishedEvents }
                        )
                    }
                }
            }

            if (groups.isNotEmpty()) {
                item(key = "header-groups") { SidebarSectionHeader("GRUPOS") }
                items(groups, key = { it.id }) { item ->
                    FeedSidebarItem(
                        item = item,
                        selected = selectedFeedId == item.id,
                        onClick = {
                            onFeedItemSelected(item.id, item.entryType)
                        }
                    )
                }
            }

        }

        // Divisor fino entre a lista e o botão fixo de criar/entrar
        HorizontalDivider(
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Spacer(Modifier.height(12.dp))

        // Botão '+' fixo no rodapé da sidebar (fora da LazyColumn)
        AddSidebarButton(
            onClick = onCreateOrJoinClick,
            modifier = Modifier.size(52.dp)
        )

        Spacer(Modifier.height(12.dp))
    }
}

/**
 * Botão '+' circular usado no rodapé da sidebar para abrir o sheet
 * de criar grupo / entrar por convite / criar evento instantâneo.
 * Mesma linguagem visual do [ExpandToggleButton].
 */
@Composable
private fun AddSidebarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Criar grupo ou evento",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Botão de expandir/recolher a lista de eventos finalizados.
 * Mostra '+' quando recolhido e '−' quando expandido.
 */
@Composable
private fun ExpandToggleButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (expanded) "Ocultar eventos finalizados" else "Mostrar eventos finalizados",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Cabeçalho de seção da sidebar (ex: "EVENTOS", "GRUPO").
 * Texto pequeno, em caixa alta, centralizado na coluna estreita (72dp).
 */
@Composable
private fun SidebarSectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
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
    val borderColor = if (selected) LoobbyColors.Brand else Color.Transparent
    val containerColor = if (selected) {
        LoobbyColors.Brand.copy(alpha = 0.12f)
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

    val borderColor = if (selected) LoobbyColors.Brand else Color.Transparent
    val containerColor = if (selected) {
        LoobbyColors.Brand.copy(alpha = 0.12f)
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