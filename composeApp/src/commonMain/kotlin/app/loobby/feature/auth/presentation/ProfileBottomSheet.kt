package app.loobby.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * BottomSheet de perfil do usuário.
 * Exibe avatar, dados, modo edição e upload de foto — tudo dentro de um ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    onDismiss: () -> Unit,
    vm: ProfileViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imagePicker = rememberImagePicker()
    val coroutineScope = rememberCoroutineScope()

    // ── Estado local para o fluxo de crop ──
    var pendingCropBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) {
            vm.resetDismiss()
            onDismiss()
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
            onUsernameChanged = vm::onUsernameChanged,
            onDisplaynameChanged = vm::onDisplaynameChanged,
            onStartEditing = vm::startEditing,
            onCancelEditing = vm::cancelEditing,
            onSaveProfile = vm::saveProfile,
            onAvatarClick = {
                coroutineScope.launch {
                    val picked = imagePicker.pickImage() ?: return@launch
                    pendingCropBytes = picked.bytes
                }
            }
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
}

@Composable
private fun ProfileSheetContent(
    state: ProfileUiState,
    onUsernameChanged: (String) -> Unit,
    onDisplaynameChanged: (String) -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSaveProfile: () -> Unit,
    onAvatarClick: () -> Unit
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

        // ─── Header com botão de edição ─────────
        if (!state.isEditing && state.profile != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onStartEditing) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
        }

        // ─── Avatar ─────────────────────────────
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = profile.avatarUrl ?: userAvatarPlaceholder(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick)
            )

            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (state.isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Trocar foto",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
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

            if (profile.isAnonymous) {
                Spacer(Modifier.height(12.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text("Conta anônima") }
                )
            }

            // ─── Success message ────────────────
            if (state.successMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.successMessage,
                    color = MaterialTheme.colorScheme.primary,
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
                if (profile.isAnonymous) "Anônimo" else "Registrado"
            )
            ProfileInfoRow("Roles", profile.roles.joinToString(", "))
        }

        // ─── Modo edição ────────────────────────
        if (state.isEditing) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.editDisplayname,
                onValueChange = onDisplaynameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome de exibição") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.editUsername,
                onValueChange = onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
                supportingText = { Text("Mínimo 3 caracteres, máximo 30") },
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
                    enabled = !state.isSaving,
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