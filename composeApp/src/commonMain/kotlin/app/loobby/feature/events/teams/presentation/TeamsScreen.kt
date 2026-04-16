package app.loobby.feature.events.teams.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.teams.domain.model.TeamDomain
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamsScreen(
    eventId: String,
    eventName: String? = null,
    onBack: () -> Unit,
    vm: TeamsViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    // Bottom sheet states
    var showCreateTeamSheet by remember { mutableStateOf(false) }
    var showAutoGenerateSheet by remember { mutableStateOf(false) }
    var showPlayersSheet by remember { mutableStateOf(false) }
    var editingTeam by remember { mutableStateOf<TeamDomain?>(null) }
    var deletingTeam by remember { mutableStateOf<TeamDomain?>(null) }
    var movingPlayer by remember { mutableStateOf<MovePlayerData?>(null) }
    var showReport by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId) { vm.load(eventId) }

    // Mostrar mensagens
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    // ── Tela de relatório (somente leitura + compartilhamento) ──
    if (showReport) {
        TeamsReportScreen(
            eventName = eventName,
            teams = state.teams,
            onBack = { showReport = false }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ──
            TopAppBar(
                title = { Text("Times") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showReport = true },
                        enabled = state.teams.isNotEmpty()
                    ) {
                        Icon(Icons.Outlined.Summarize, contentDescription = "Ver formação")
                    }
                },
                windowInsets = WindowInsets(0)
            )

            if (state.isLoading) {
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // ── Stats row ──
            StatsRow(
                teamCount = state.teams.size,
                playerCount = state.totalPlayersInTeams,
                playerReserveCount = state.totalReservedPlayers,
                unassignedCount = state.unassignedPlayers.size,
                average = state.averagePlayersPerTeam
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Action buttons ──
                item(key = "actions") {
                    ActionButtonsRow(
                        onNewTeam = { showCreateTeamSheet = true },
                        onAutoGenerate = { showAutoGenerateSheet = true },
                        onShowPlayers = { showPlayersSheet = true }
                    )
                }

                // ── Section header ──
                if (state.teams.isNotEmpty()) {
                    item(key = "header") {
                        Text(
                            text = "TIMES DO EVENTO",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }

                // ── Team cards ──
                items(state.teams, key = { it.id }) { team ->
                    TeamCard(
                        team = team,
                        confirmedPlayers = state.confirmedPlayers,
                        allTeams = state.teams,
                        onEdit = { editingTeam = team },
                        onDelete = { deletingTeam = team },
                        onAddPlayer = { userId ->
                            vm.onAddPlayer(eventId, team.id, userId)
                        },
                        onRemovePlayer = { userId ->
                            vm.onRemovePlayer(eventId, team.id, userId)
                        },
                        onMovePlayer = { userId ->
                            movingPlayer = MovePlayerData(
                                userId = userId,
                                fromTeamId = team.id,
                                playerName = team.players.find { it.userId == userId }?.displayName ?: ""
                            )
                        }
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    // ── Bottom sheets / dialogs ──

    if (showCreateTeamSheet) {
        CreateTeamSheet(
            availablePlayers = state.unassignedPlayers,
            usedColors = state.teams.mapNotNull { it.color },
            onDismiss = { showCreateTeamSheet = false },
            onConfirm = { name, color, playerIds ->
                showCreateTeamSheet = false
                vm.onCreateTeam(eventId, name, color, playerIds)
            }
        )
    }

    if (showAutoGenerateSheet) {
        AutoGenerateSheet(
            totalPlayers = state.totalConfirmedPlayers,
            onDismiss = { showAutoGenerateSheet = false },
            onGenerate = { teamCount, teamSize ->
                showAutoGenerateSheet = false
                vm.onAutoGenerate(eventId, teamCount, teamSize)
            }
        )
    }

    if (showPlayersSheet) {
        PlayersListSheet(
            confirmedPlayers = state.confirmedPlayers,
            teams = state.teams,
            onDismiss = { showPlayersSheet = false }
        )
    }

    editingTeam?.let { team ->
        EditTeamSheet(
            team = team,
            onDismiss = { editingTeam = null },
            onSave = { name, color ->
                editingTeam = null
                vm.onUpdateTeam(eventId, team.id, name, color)
            }
        )
    }

    deletingTeam?.let { team ->
        DeleteTeamDialog(
            teamName = team.name,
            onDismiss = { deletingTeam = null },
            onConfirm = {
                deletingTeam = null
                vm.onDeleteTeam(eventId, team.id)
            }
        )
    }

    movingPlayer?.let { data ->
        MovePlayerSheet(
            playerName = data.playerName,
            currentTeamId = data.fromTeamId,
            teams = state.teams,
            onDismiss = { movingPlayer = null },
            onMove = { toTeamId ->
                movingPlayer = null
                vm.onMovePlayer(eventId, data.fromTeamId, data.userId, toTeamId)
            }
        )
    }
}

/** Dados para o fluxo de mover jogador */
data class MovePlayerData(
    val userId: String,
    val fromTeamId: String,
    val playerName: String
)

// ─── Stats row ───────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    teamCount: Int,
    playerCount: Int,
    playerReserveCount: Int,
    unassignedCount: Int,
    average: Double
) {
    val cardList = listOf(
        Pair(teamCount.toString(),"Times"),
        Pair(playerCount.toString(),"Jogadores"),
        Pair(playerReserveCount.toString(),"Reservas"),
        Pair(unassignedCount.toString(),"Sem time"),
        Pair(if (average > 0) average.toString() else "0","Média/Time"),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cardList) { card ->
            if (!(card.second=="Reservas" && playerReserveCount==0))
                StatCard(value = card.first, label = card.second)
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Action buttons row ──────────────────────────────────────────────────────

@Composable
private fun ActionButtonsRow(
    onNewTeam: () -> Unit,
    onAutoGenerate: () -> Unit,
    onShowPlayers: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            icon = Icons.Outlined.Add,
            label = "Novo time",
            onClick = onNewTeam,
            modifier = Modifier.weight(1f),
            iconTint = MaterialTheme.colorScheme.primary
        )
        ActionButton(
            icon = Icons.Outlined.Refresh,
            label = "Gerar times",
            onClick = onAutoGenerate,
            modifier = Modifier.weight(1f),
            iconTint = MaterialTheme.colorScheme.tertiary
        )
        ActionButton(
            icon = Icons.Outlined.People,
            label = "Jogadores",
            onClick = onShowPlayers,
            modifier = Modifier.weight(1f),
            iconTint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = iconTint)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}