package app.loobby.feature.events.teams.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import app.loobby.theme.LoobbyColors
import androidx.compose.ui.unit.dp
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage

@Composable
fun TeamCard(
    team: TeamDomain,
    confirmedPlayers: List<RsvpDomain>,
    allTeams: List<TeamDomain>,
    // Todas as ações deste card são escritas; quando offline desabilitamos os
    // ícones e o botão de adicionar jogador. O card continua expansível para
    // consultar a lista de jogadores a partir do cache.
    isOnline: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddPlayer: (userId: String) -> Unit,
    onRemovePlayer: (userId: String) -> Unit,
    onMovePlayer: (userId: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlayer by remember { mutableStateOf<RsvpDomain?>(null) }

    val teamColor = team.color?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary

    // Jogadores disponíveis para adicionar (não estão em nenhum time)
    val allTeamPlayerIds = allTeams.flatMap { t -> t.players.map { it.userId } }.toSet()
    val availablePlayers = confirmedPlayers.filter { it.userId !in allTeamPlayerIds }

    // Filtro de busca
    val filteredPlayers = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        availablePlayers.filter { player ->
            val name = player.displayname?.takeIf { it.isNotBlank() } ?: player.username
            name.contains(searchQuery, ignoreCase = true)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(teamColor)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${team.players.size} jogadores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action icons
                IconButton(onClick = onEdit, enabled = isOnline, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, enabled = isOnline, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Excluir",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Expanded content ──
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Player list
                    team.players.forEach { player ->
                        PlayerRow(
                            displayName = player.displayName,
                            avatarUrl = player.avatarUrl,
                            enabled = isOnline,
                            onMove = { onMovePlayer(player.userId) },
                            onRemove = { onRemovePlayer(player.userId) }
                        )
                    }

                    // ── Add player field ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (selectedPlayer != null) {
                                selectedPlayer?.displayname?.takeIf { it.isNotBlank() }
                                    ?: selectedPlayer?.username ?: ""
                            } else searchQuery,
                            onValueChange = {
                                searchQuery = it
                                selectedPlayer = null
                            },
                            enabled = isOnline,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nome do jogador...") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        FilledIconButton(
                            onClick = {
                                selectedPlayer?.let { player ->
                                    onAddPlayer(player.userId)
                                    searchQuery = ""
                                    selectedPlayer = null
                                }
                            },
                            enabled = selectedPlayer != null && isOnline,
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar jogador")
                        }
                    }

                    // ── Search results dropdown ──
                    if (searchQuery.isNotBlank() && filteredPlayers.isNotEmpty() && selectedPlayer == null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                filteredPlayers.take(5).forEach { player ->
                                    val name = player.displayname?.takeIf { it.isNotBlank() } ?: player.username
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedPlayer = player
                                                searchQuery = name
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        PlayerAvatar(
                                            displayName = name,
                                            avatarUrl = player.avatarUrl,
                                            size = 28
                                        )
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Nenhum resultado
                    if (searchQuery.isNotBlank() && filteredPlayers.isEmpty() && selectedPlayer == null) {
                        Text(
                            text = "Nenhum jogador disponível encontrado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Player row ──────────────────────────────────────────────────────────────

@Composable
private fun PlayerRow(
    displayName: String,
    avatarUrl: String?,
    enabled: Boolean = true,
    onMove: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayerAvatar(
                displayName = displayName,
                avatarUrl = avatarUrl,
                size = 32
            )

            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onMove, enabled = enabled, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Outlined.SwapHoriz,
                    contentDescription = "Mover",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove, enabled = enabled, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remover",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Player avatar ───────────────────────────────────────────────────────────

@Composable
fun PlayerAvatar(
    displayName: String,
    avatarUrl: String?,
    size: Int = 32
) {
    AsyncImage(
        model = avatarUrl ?: userAvatarPlaceholder(),
        contentDescription = displayName,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
    )
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

fun parseColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        Color(("FF$cleanHex").toLong(16))
    } catch (_: Exception) {
        LoobbyColors.TeamColorFallback
    }
}