package app.loobby.feature.groups.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.core.media.CropAvatarSheet
import app.loobby.core.media.rememberImagePicker
import app.loobby.core.util.rememberCopyToClipboard
import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.groupImagePlaceholder
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
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
    val isOwner = state.isOwner

    LaunchedEffect(groupId) {
        vm.loadMembers(groupId)
    }

    LaunchedEffect(state.deleteGroupSuccess) {
        if (state.deleteGroupSuccess) {
            vm.clearDeleteGroupSuccess()
            onLeaveGroup() // volta para a tela principal
        }
    }

    val copyToClipboard = rememberCopyToClipboard()
    val imagePicker = rememberImagePicker()
    val coroutineScope = rememberCoroutineScope()

    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteGroupDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var copiedSnackbar by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<GroupMemberResponse?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var pendingCropBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(copiedSnackbar) {
        if (copiedSnackbar) {
            snackbarHostState.showSnackbar("Código copiado!")
            copiedSnackbar = false
        }
    }

    LaunchedEffect(state.groupActionMessage) {
        state.groupActionMessage?.let {
            snackbarHostState.showSnackbar(it)
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

    if (showDeleteGroupDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isDeletingGroup) showDeleteGroupDialog = false },
            title = { Text("Excluir grupo?") },
            text = {
                Text("Tem certeza que deseja excluir \"${group?.name}\"? Essa ação não pode ser desfeita. Todos os eventos, membros e dados do grupo serão removidos permanentemente.")
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.deleteGroup(groupId) },
                    enabled = !state.isDeletingGroup,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (state.isDeletingGroup) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Excluir", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteGroupDialog = false },
                    enabled = !state.isDeletingGroup
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isUpdatingGroup) showRenameDialog = false },
            title = { Text("Renomear grupo") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Novo nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            vm.updateGroupName(groupId, renameText.trim())
                            showRenameDialog = false
                        }
                    },
                    enabled = renameText.isNotBlank() && !state.isUpdatingGroup
                ) {
                    if (state.isUpdatingGroup) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (memberToRemove != null) {
        val member = memberToRemove!!
        AlertDialog(
            onDismissRequest = { if (!state.isRemovingMember) memberToRemove = null },
            title = { Text("Remover membro?") },
            text = {
                Text("Tem certeza que deseja remover \"${member.displayname ?: member.username}\" do grupo?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.removeMember(groupId, member.userId)
                        memberToRemove = null
                    },
                    enabled = !state.isRemovingMember,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (state.isRemovingMember) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Remover", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { memberToRemove = null },
                    enabled = !state.isRemovingMember
                ) {
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
                    GroupDetailHeader(
                        group = group,
                        isOwner = isOwner,
                        isUploading = state.isUpdatingGroup,
                        onImageClick = {
                            if (isOwner) {
                                coroutineScope.launch {
                                    val picked = imagePicker.pickImage() ?: return@launch
                                    pendingCropBytes = picked.bytes
                                }
                            }
                        },
                        onNameClick = {
                            // CHANGED: apenas dono pode renomear
                            if (isOwner) {
                                renameText = group.name
                                showRenameDialog = true
                            }
                        }
                    )
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
                        MemberRow(
                            member = member,
                            showRemoveAction = isOwner && !member.isOwner,
                            onRemoveClick = { memberToRemove = member }
                        )
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

                if (isOwner) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showDeleteGroupDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Excluir Grupo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (pendingCropBytes != null) {
        CropAvatarSheet(
            imageBytes = pendingCropBytes!!,
            onConfirm = { cropped ->
                pendingCropBytes = null
                vm.uploadGroupImage(groupId, cropped.bytes, cropped.fileName)
            },
            onDismiss = { pendingCropBytes = null }
        )
    }
}


// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GroupDetailHeader(
    group: GroupDomain,
    isOwner: Boolean,
    isUploading: Boolean,
    onImageClick: () -> Unit,
    onNameClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = group.imageUrl ?: groupImagePlaceholder(group.name),
                contentDescription = group.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .then(if (isOwner) Modifier.clickable(onClick = onImageClick) else Modifier)
            )
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Alterar imagem",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .then(if (isOwner) Modifier.clickable(onClick = onNameClick) else Modifier)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            if (isOwner) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Editar nome",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
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
private fun MemberRow(
    member: GroupMemberResponse,
    showRemoveAction: Boolean = false,
    onRemoveClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

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
        if (showRemoveAction) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Opções",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Remover do grupo",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRemoveClick()
                        }
                    )
                }
            }
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