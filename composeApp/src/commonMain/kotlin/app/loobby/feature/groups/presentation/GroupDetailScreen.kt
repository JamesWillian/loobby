package app.loobby.feature.groups.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.core.util.rememberCopyToClipboard
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.groupImagePlaceholder
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onLeaveGroup: () -> Unit,
    vm: GroupsViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    val group = state.selectedGroup
    val members = state.members

    LaunchedEffect(groupId) {
        vm.loadMembers(groupId)
    }

    val copyToClipboard = rememberCopyToClipboard()

    var showLeaveDialog by remember { mutableStateOf(false) }
    var copiedSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(copiedSnackbar) {
        if (copiedSnackbar) {
            snackbarHostState.showSnackbar("Código copiado!")
            copiedSnackbar = false
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Sair do grupo?") },
            text = { Text("Tem certeza que deseja sair de \"${group?.name}\"? Você precisará de um novo convite para voltar.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        vm.leave(groupId)
                        onLeaveGroup()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sair", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Detalhes do Grupo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )

            if (group == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // ── Group image + name ────────────────────────────────────────
                item {
                    GroupDetailHeader(group = group)
                }

                // ── Info cards ────────────────────────────────────────────────
                item {
                    GroupInfoSection(
                        group = group,
                        ownerName = members.find { it.isOwner }?.displayname
                            ?: members.find { it.isOwner }?.username,
                        memberCount = members.size,
                        onCopyInviteCode = {
                            copyToClipboard(group.inviteCode)
                            copiedSnackbar = true
                        }
                    )
                }

                // ── Members header ────────────────────────────────────────────
                item {
                    Text(
                        text = "Membros (${members.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                // ── Members list ──────────────────────────────────────────────
                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                } else {
                    items(members, key = { it.userId }) { member ->
                        MemberRow(member = member)
                    }
                }

                // ── Leave button ──────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { showLeaveDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sair do Grupo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GroupDetailHeader(group: GroupDomain) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = group.imageUrl ?: groupImagePlaceholder(group.name),
            contentDescription = group.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Text(
            text = group.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

// ── Info section ──────────────────────────────────────────────────────────────

@Composable
private fun GroupInfoSection(
    group: GroupDomain,
    ownerName: String?,
    memberCount: Int,
    onCopyInviteCode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoRow(label = "Membros", value = "$memberCount membro${if (memberCount != 1) "s" else ""}")

            if (!ownerName.isNullOrBlank()) {
                InfoRow(label = "Criado por", value = ownerName)
            }

            group.createdAt?.let { raw ->
                InfoRow(label = "Criado em", value = raw.formatCreatedAt())
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Invite code row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Código de convite",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = group.inviteCode,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onCopyInviteCode) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar código",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

// ── Member row ────────────────────────────────────────────────────────────────

@Composable
private fun MemberRow(member: GroupMemberResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = member.avatarUrl
                    ?: userAvatarPlaceholder(),
                contentDescription = member.username,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = member.displayname ?: member.username,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                if (member.isOwner) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "👑 Dono",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = "@${member.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Date helper ───────────────────────────────────────────────────────────────

private fun String.formatCreatedAt(): String {
    return runCatching {
        val instant = kotlin.time.Instant.parse(this)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = local.day.toString().padStart(2, '0')
        val month = local.month.number.toString().padStart(2, '0')
        val year = local.year
        "$day/$month/$year"
    }.getOrDefault(this)
}