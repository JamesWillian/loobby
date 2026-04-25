package app.loobby.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.loobby.core.media.CropAvatarSheet
import app.loobby.core.media.rememberImagePicker
import app.loobby.core.network.LocalIsOnline
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Mantém apenas caracteres válidos para um username:
 * letras minúsculas (letras maiúsculas digitadas são convertidas), dígitos
 * e underline. Underlines no início são removidos — o username não pode
 * começar com `_`.
 */
private fun sanitizeUsername(raw: String): String {
    val filtered = raw
        .lowercase()
        .filter { it in 'a'..'z' || it in '0'..'9' || it == '_' }
    return filtered.trimStart('_')
}

/**
 * BottomSheet de perfil do usuário.
 * Exibe avatar, dados, modo edição e upload de foto — tudo dentro de um ModalBottomSheet.
 *
 * Restrições para quem não verificou email:
 *  - NÃO pode alterar username
 *  - NÃO pode alterar avatar
 *  - PODE alterar displayname
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    onDismiss: () -> Unit,
    vm: ProfileViewModel = koinInject(),
    authVm: AuthViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val authState by authVm.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imagePicker = rememberImagePicker()
    val coroutineScope = rememberCoroutineScope()

    // Edições de perfil, troca de avatar, alteração de senha e exclusão de conta
    // são escritas. Logout continua disponível offline (é apenas clearTokens local).
    val isOnline = LocalIsOnline.current

    // ── Estado local para o fluxo de crop ──
    var pendingCropBytes by remember { mutableStateOf<ByteArray?>(null) }

    var showChangePasswordSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) {
            vm.resetDismiss()
            onDismiss()
        }
    }

    // Ao fechar o sheet (por qualquer caminho — swipe, back, click fora,
    // logout, exclusão de conta, etc.) resetamos o estado efêmero para
    // garantir que ao reabrir o usuário sempre veja a visualização de
    // perfil — nunca o modo Editar deixado aberto. O ProfileViewModel é
    // singleton, então sem esse reset `isEditing = true` persistiria.
    DisposableEffect(Unit) {
        onDispose {
            vm.resetSheetState()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 2.dp
    ) {
        ProfileSheetContent(
            state = state,
            hasFullAccess = authState.hasFullAccess,
            isOnline = isOnline,
            needsEmailVerification = authState.needsEmailVerification,
            verificationEmail = authState.profile?.email,
            isResendingVerification = authState.isResendingVerification,
            resendCooldownSeconds = authState.resendCooldownSeconds,
            verificationMessage = authState.verificationMessage,
            onResendVerification = { authVm.resendVerificationEmail() },
            onUsernameChanged = vm::onUsernameChanged,
            onDisplaynameChanged = vm::onDisplaynameChanged,
            onStartEditing = vm::startEditing,
            onCancelEditing = vm::cancelEditing,
            onSaveProfile = vm::saveProfile,
            onAvatarClick = {
                // hasFullAccess exige email verificado; isOnline exige rede.
                if (authState.hasFullAccess && isOnline) {
                    coroutineScope.launch {
                        val picked = imagePicker.pickImage() ?: return@launch
                        pendingCropBytes = picked.bytes
                    }
                }
            },
            onChangePasswordClick = { showChangePasswordSheet = true },
            onMoreOptionsClick = vm::onMoreOptionsClick,
            onMoreOptionsDismiss = vm::onMoreOptionsDismiss,
            onDeleteAccountClick = vm::onDeleteAccountClick,
            onLogoutClick = vm::requestLogout
        )
    }

    if (state.showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = vm::cancelLogout,
            title = { Text("Sair da conta") },
            text = { Text("Tem certeza que deseja sair? Você voltará como usuário anônimo.") },
            confirmButton = {
                TextButton(
                    onClick = vm::confirmLogout,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = vm::cancelLogout) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Dialog de exclusão de conta ──
    if (state.showDeleteAccountDialog) {
        DeleteAccountDialog(
            password = state.deleteAccountPassword,
            onPasswordChange = vm::onDeleteAccountPasswordChange,
            error = state.deleteAccountError,
            loading = state.isDeletingAccount,
            onConfirm = vm::onDeleteAccountConfirm,
            onDismiss = vm::onDeleteAccountDialogDismiss,
        )
    }

    // ── CropAvatarSheet: exibida quando há bytes pendentes ──
    if (pendingCropBytes != null) {
        CropAvatarSheet(
            imageBytes = pendingCropBytes!!,
            onConfirm = { cropped ->
                pendingCropBytes = null
                vm.uploadAvatar(cropped.bytes, cropped.fileName)
            },
            onDismiss = { pendingCropBytes = null }
        )
    }

    if (showChangePasswordSheet) {
        ChangePasswordSheet(
            onDismiss = { showChangePasswordSheet = false }
        )
    }
}

@Composable
private fun ProfileSheetContent(
    state: ProfileUiState,
    hasFullAccess: Boolean,
    isOnline: Boolean,
    needsEmailVerification: Boolean,
    verificationEmail: String?,
    isResendingVerification: Boolean,
    resendCooldownSeconds: Int,
    verificationMessage: String?,
    onResendVerification: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onDisplaynameChanged: (String) -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSaveProfile: () -> Unit,
    onAvatarClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    onMoreOptionsDismiss: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    if (state.isLoading) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val profile = state.profile
    if (profile == null) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.errorMessage ?: "Não foi possível carregar o perfil",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ─── Email verification banner (dentro do perfil) ─────
        EmailVerificationBanner(
            visible = needsEmailVerification,
            email = verificationEmail,
            isResending = isResendingVerification,
            cooldownSeconds = resendCooldownSeconds,
            message = verificationMessage,
            onResendClick = onResendVerification,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ─── Header com botão de edição ─────────
        if (!state.isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão Editar — só escritas; desabilitado offline.
                IconButton(onClick = onStartEditing, enabled = isOnline) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                }

                // Botão Mais Opções — DropdownMenu ancorado dentro do Box.
                // Exclusão de conta é escrita; desabilitamos o menu offline.
                Box {
                    IconButton(onClick = onMoreOptionsClick, enabled = isOnline) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(
                        expanded = state.showMoreOptionsMenu,
                        onDismissRequest = onMoreOptionsDismiss,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Excluir conta",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = onDeleteAccountClick
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
        }

        // ─── Avatar ─────────────────────────────
        // Trocar avatar é escrita; precisa estar online E com email verificado.
        val canEditAvatar = hasFullAccess && isOnline
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = profile.avatarUrl ?: userAvatarPlaceholder(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .then(
                        if (canEditAvatar) Modifier.clickable(onClick = onAvatarClick)
                        else Modifier
                    )
            )

            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .then(
                        if (canEditAvatar) Modifier.clickable(onClick = onAvatarClick)
                        else Modifier
                    ),
                color = if (canEditAvatar) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surfaceVariant,  // visual desabilitado
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (state.isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Trocar foto",
                            modifier = Modifier.size(16.dp),
                            tint = if (canEditAvatar) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Modo visualização ──────────────────
        if (!state.isEditing) {
            Text(
                text = profile.displayname ?: profile.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (profile.email != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ─── Success message ────────────────
            if (state.successMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.successMessage,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            ProfileInfoRow("Membro desde", profile.createdAt?.take(10) ?: "—")
            Spacer(Modifier.height(8.dp))
            ProfileInfoRow(
                "Tipo de conta",
                when {
                    profile.emailVerified -> "Registrado"
                    else -> "Registrado (email pendente)"
                }
            )
            ProfileInfoRow("Roles", profile.roles.joinToString(", "))

            Spacer(Modifier.height(24.dp))

            if (hasFullAccess) {
                OutlinedButton(
                    onClick = onChangePasswordClick,
                    enabled = isOnline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Alterar senha", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { onLogoutClick() },
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
                Text("Sair da Conta", fontWeight = FontWeight.Bold)
            }
        }

        // ─── Modo edição ────────────────────────
        if (state.isEditing) {
            Spacer(Modifier.height(8.dp))

            // Displayname — SEMPRE editável
            OutlinedTextField(
                value = state.editDisplayname,
                onValueChange = onDisplaynameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome de exibição") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Username — BLOQUEADO se não verificou email
            OutlinedTextField(
                value = state.editUsername,
                onValueChange = { raw -> onUsernameChanged(sanitizeUsername(raw)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
                enabled = hasFullAccess,
                supportingText = {
                    Text(
                        if (hasFullAccess) "Mínimo 3 caracteres, máximo 30"
                        else "Verifique seu email para alterar o username"
                    )
                },
                shape = MaterialTheme.shapes.medium
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelEditing,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = onSaveProfile,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving && isOnline,
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Salvar")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}