package app.loobby.feature.events.teams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.core.share.shareText
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.model.TeamPlayerDomain
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsReportScreen(
    eventName: String?,
    teams: List<TeamDomain>,
    onBack: () -> Unit
) {
    val shareText = buildTeamsShareText(eventName = eventName, teams = teams)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Formação dos Times") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { shareText(shareText) }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Compartilhar")
                    }
                },
                windowInsets = WindowInsets(0)
            )
        }
    ) { padding ->
        if (teams.isEmpty()) {
            // ── Estado vazio ────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum time criado ainda.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Resumo ──────────────────────────────────────────────────
                item(key = "summary") {
                    val totalPlayers = teams.sumOf { it.players.size }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ReportStat(value = teams.size.toString(), label = "Times")
                            ReportStat(value = totalPlayers.toString(), label = "Jogadores")
                        }
                    }
                }

                // ── Cards de time (somente leitura) ─────────────────────────
                items(teams, key = { it.id }) { team ->
                    ReadOnlyTeamCard(team = team)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─── Stat do resumo ─────────────────────────────────────────────────────────

@Composable
private fun ReportStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// ─── Card de time (somente leitura) ─────────────────────────────────────────

@Composable
private fun ReadOnlyTeamCard(team: TeamDomain) {
    val teamColor = team.color?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            // ── Header do time ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(teamColor.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(teamColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${team.players.size} jogadores",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Lista de jogadores ───────────────────────────────────────
            if (team.players.isEmpty()) {
                Text(
                    text = "Sem jogadores",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    team.players.forEachIndexed { index, player ->
                        ReportPlayerRow(
                            index = index + 1,
                            player = player
                        )
                        if (index < team.players.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Linha de jogador ───────────────────────────────────────────────────────

@Composable
private fun ReportPlayerRow(index: Int, player: TeamPlayerDomain) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Número
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp)
        )

        // Avatar
        AsyncImage(
            model = player.avatarUrl ?: userAvatarPlaceholder(),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(10.dp))

        // Nome
        Text(
            text = player.displayName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}