package app.loobby.feature.events.teams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.loobby.theme.LoobbyColors
import androidx.compose.ui.unit.dp
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.teams.domain.model.TeamDomain

// ─── Create Team Sheet ───────────────────────────────────────────────────────
// O backend exige players no request, então ao criar time mostramos um sheet
// com a lista de jogadores disponíveis para seleção.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamSheet(
    availablePlayers: List<RsvpDomain>,
    usedColors: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String?, playerIds: List<String>) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var selectedColor by remember {
        mutableStateOf(TEAM_COLORS.firstOrNull { it !in usedColors } ?: TEAM_COLORS.first())
    }
    var selectedPlayerIds by remember { mutableStateOf(setOf<String>()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Novo time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Nome do time
            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome do time") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            // Color picker
            Text(
                text = "Cor do time",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ColorPicker(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            // Player selection
            Text(
                text = "Selecione os jogadores (${selectedPlayerIds.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (availablePlayers.isEmpty()) {
                Text(
                    text = "Nenhum jogador disponível",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availablePlayers, key = { it.userId }) { player ->
                        val name = player.displayname?.takeIf { it.isNotBlank() } ?: player.username
                        val isSelected = player.userId in selectedPlayerIds

                        Card(
                            onClick = {
                                selectedPlayerIds = if (isSelected) {
                                    selectedPlayerIds - player.userId
                                } else {
                                    selectedPlayerIds + player.userId
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        selectedPlayerIds = if (isSelected) {
                                            selectedPlayerIds - player.userId
                                        } else {
                                            selectedPlayerIds + player.userId
                                        }
                                    }
                                )
                                PlayerAvatar(
                                    displayName = name,
                                    avatarUrl = player.avatarUrl,
                                    size = 32
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        onConfirm(teamName, selectedColor, selectedPlayerIds.toList())
                    },
                    modifier = Modifier.weight(1f),
                    enabled = teamName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Criar time") }
            }
        }
    }
}

// ─── Edit Team Sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTeamSheet(
    team: TeamDomain,
    onDismiss: () -> Unit,
    onSave: (name: String, color: String?) -> Unit
) {
    var teamName by remember { mutableStateOf(team.name) }
    var selectedColor by remember { mutableStateOf(team.color ?: TEAM_COLORS.first()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Editar time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Text(
                text = "Cor do time",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ColorPicker(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick = { onSave(teamName, selectedColor) },
                    modifier = Modifier.weight(1f),
                    enabled = teamName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Salvar") }
            }
        }
    }
}

// ─── Auto Generate Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoGenerateSheet(
    totalPlayers: Int,
    hasReserves: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (teamCount: Int?, teamSize: Int?, includeReserves: Boolean) -> Unit
) {
    var divideBy by remember { mutableStateOf(DivideMode.BY_TEAM_COUNT) }
    var count by remember { mutableStateOf(2) }
    var includeReserves by remember { mutableStateOf(true) }

    // skipPartiallyExpanded garante que o sheet abre na altura cheia, sem
    // cortar o checkbox de reservas ou os botões de ação.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Gerar times automaticamente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Toggle: dividir por qtd de times ou jogadores/time
            Text(
                text = "Dividir por",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = divideBy == DivideMode.BY_TEAM_COUNT,
                    onClick = {
                        divideBy = DivideMode.BY_TEAM_COUNT
                        count = 2
                    },
                    label = { Text("Qtd. de times") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
                FilterChip(
                    selected = divideBy == DivideMode.BY_TEAM_SIZE,
                    onClick = {
                        divideBy = DivideMode.BY_TEAM_SIZE
                        count = 4
                    },
                    label = { Text("Jogadores/time") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }

            // Counter
            Text(
                text = if (divideBy == DivideMode.BY_TEAM_COUNT) "Número de times" else "Jogadores por time",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledIconButton(
                    onClick = { if (count > 2) count-- },
                    enabled = count > 2,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledIconButton(
                    onClick = { count++ },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                }
                Text(
                    text = if (divideBy == DivideMode.BY_TEAM_COUNT) "times" else "por time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Preview
            val preview = if (divideBy == DivideMode.BY_TEAM_COUNT) {
                val perTeam = if (count > 0) totalPlayers / count else 0
                "$totalPlayers jogadores → ~$perTeam por time"
            } else {
                val numTeams = if (count > 0) {
                    val t = totalPlayers / count
                    if (totalPlayers % count > 0) t + 1 else t
                } else 0
                "$totalPlayers jogadores → ~$numTeams times"
            }
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Checkbox: incluir reservas ──
            // Mostra o controle apenas quando houver reservas a considerar; do
            // contrário ele não acrescenta nada à decisão do usuário.
            if (hasReserves) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeReserves = !includeReserves }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = includeReserves,
                        onCheckedChange = { includeReserves = it }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Criar time de reservas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Inclui jogadores marcados como reserva em um time separado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        // Quando não há reservas no evento, o flag é irrelevante;
                        // mandamos `false` para evitar que o backend crie o time vazio.
                        val include = hasReserves && includeReserves
                        when (divideBy) {
                            DivideMode.BY_TEAM_COUNT -> onGenerate(count, null, include)
                            DivideMode.BY_TEAM_SIZE -> onGenerate(null, count, include)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("Gerar times") }
            }
        }
    }
}

private enum class DivideMode { BY_TEAM_COUNT, BY_TEAM_SIZE }

// ─── Move Player Sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovePlayerSheet(
    playerName: String,
    currentTeamId: String,
    teams: List<TeamDomain>,
    onDismiss: () -> Unit,
    onMove: (toTeamId: String) -> Unit
) {
    val otherTeams = teams.filter { it.id != currentTeamId }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mover $playerName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            otherTeams.forEach { team ->
                val teamColor = team.color?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary
                OutlinedCard(
                    onClick = { onMove(team.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(teamColor)
                        )
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "(${team.players.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Cancelar") }
        }
    }
}

// ─── Delete Team Dialog ──────────────────────────────────────────────────────

@Composable
fun DeleteTeamDialog(
    teamName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir time?") },
        text = {
            Text("Os jogadores voltarão para a lista de sem time. Essa ação não pode ser desfeita.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Excluir") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ─── Players List Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersListSheet(
    confirmedPlayers: List<RsvpDomain>,
    teams: List<TeamDomain>,
    onDismiss: () -> Unit
) {
    val allTeamPlayerIds = teams.flatMap { t -> t.players.map { it.userId } }.toSet()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Jogadores confirmados (${confirmedPlayers.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(confirmedPlayers, key = { it.userId }) { player ->
                    val name = player.displayname?.takeIf { it.isNotBlank() } ?: player.username
                    val isAssigned = player.userId in allTeamPlayerIds
                    val teamName = if (isAssigned) {
                        teams.find { t -> t.players.any { it.userId == player.userId } }?.name
                    } else null

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlayerAvatar(
                            displayName = name,
                            avatarUrl = player.avatarUrl,
                            size = 36
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = name, style = MaterialTheme.typography.bodyMedium)
                            if (teamName != null) {
                                Text(
                                    text = teamName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                Text(
                                    text = "Sem time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Color picker ────────────────────────────────────────────────────────────

@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TEAM_COLORS) { colorHex ->
            val color = parseColor(colorHex)
            val isSelected = colorHex == selectedColor

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(LoobbyColors.OnPrimary)
                    )
                }
            }
        }
    }
}