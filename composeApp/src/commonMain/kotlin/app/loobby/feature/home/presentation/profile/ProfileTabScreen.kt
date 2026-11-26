package app.loobby.feature.home.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.loobby.core.analytics.AppScreen
import app.loobby.core.lifecycle.OnResumeEffect
import app.loobby.core.media.rememberImagePicker
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.ui.LocalAppSnackbar
import app.loobby.core.ui.components.ScreenHeader
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.auth.presentation.ProfileViewModel
import app.loobby.feature.home.presentation.profile.components.AccountCard
import app.loobby.feature.home.presentation.profile.components.AccountDialogs
import app.loobby.feature.home.presentation.profile.components.AccountLoading
import app.loobby.feature.home.presentation.profile.components.AccountMenu
import app.loobby.feature.home.presentation.profile.components.AnonymousAccountCard
import app.loobby.feature.home.presentation.profile.components.EditProfileSheet
import app.loobby.feature.library.presentation.components.AddLibraryGameSheet
import app.loobby.feature.library.presentation.mine.LibraryGameSheet
import app.loobby.feature.library.presentation.mine.MyLibraryViewModel
import app.loobby.feature.library.presentation.mine.myLibraryItems
import app.loobby.theme.Spacing
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * A aba Perfil: a conta (foto, nome, @, e-mail, senha, sair e excluir) e a biblioteca
 * de jogos. O anônimo vê o apelido, o caminho para a conta e a biblioteca dele, que
 * funciona igual.
 */
@Composable
fun ProfileTabScreen(
    isAnonymous: Boolean,
    onLogin: () -> Unit,
    onChooseNickname: () -> Unit,
    modifier: Modifier = Modifier,
    profileVm: ProfileViewModel = koinInject(),
    authVm: AuthViewModel = koinInject(),
    libraryVm: MyLibraryViewModel = koinViewModel(),
) {
    val profileState by profileVm.uiState.collectAsState()
    val authState by authVm.uiState.collectAsState()
    val library by libraryVm.state.collectAsState()
    val isOnline = LocalIsOnline.current
    val snackbar = LocalAppSnackbar.current
    val imagePicker = rememberImagePicker()
    val scope = rememberCoroutineScope()

    var pendingAvatar by remember { mutableStateOf<ByteArray?>(null) }
    var showChangePassword by remember { mutableStateOf(false) }
    var isAddingGame by remember { mutableStateOf(false) }

    val profile = profileState.profile ?: authState.profile
    val canChangeAvatar = authState.hasFullAccess && isOnline

    LaunchedEffect(Unit) { libraryVm.refresh() }
    OnResumeEffect { libraryVm.refresh() }
    LaunchedEffect(libraryVm) { libraryVm.messages.collect { snackbar.showSnackbar(it) } }
    // Sair e excluir a conta terminam pedindo para "fechar": aqui não há o que fechar.
    LaunchedEffect(profileState.shouldDismiss) { if (profileState.shouldDismiss) profileVm.resetDismiss() }
    // O ProfileViewModel é um só no app: ao sair da aba, a edição aberta não fica para depois.
    DisposableEffect(Unit) { onDispose { profileVm.resetSheetState() } }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Perfil",
            actions = {
                if (!isAnonymous && profile != null) {
                    IconButton(onClick = profileVm::startEditing, enabled = isOnline) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar perfil")
                    }
                    AccountMenu(
                        expanded = profileState.showMoreOptionsMenu,
                        hasFullAccess = authState.hasFullAccess,
                        isOnline = isOnline,
                        onExpand = profileVm::onMoreOptionsClick,
                        onDismiss = profileVm::onMoreOptionsDismiss,
                        onChangePassword = { showChangePassword = true },
                        onLogout = profileVm::requestLogout,
                        onDeleteAccount = profileVm::onDeleteAccountClick,
                    )
                }
            },
        )
        LazyColumn(
            contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "account") {
                when {
                    isAnonymous -> AnonymousAccountCard(
                        profile = profile,
                        onLogin = onLogin,
                        onChooseNickname = onChooseNickname,
                    )
                    profile != null -> AccountCard(
                        profile = profile,
                        canChangeAvatar = canChangeAvatar,
                        isUploadingAvatar = profileState.isUploadingAvatar,
                        message = profileState.successMessage,
                        onAvatarClick = {
                            if (canChangeAvatar) {
                                scope.launch { imagePicker.pickImage()?.let { pendingAvatar = it.bytes } }
                            }
                        },
                    )
                    else -> AccountLoading()
                }
            }
            myLibraryItems(
                state = library,
                isOnline = isOnline,
                onAdd = { isAddingGame = true },
                onFilter = libraryVm::setFilter,
                onOpenGame = libraryVm::openGame,
            )
        }
    }

    if (profileState.isEditing) {
        EditProfileSheet(
            initialDisplayname = profileState.editDisplayname,
            initialUsername = profileState.editUsername,
            canEditUsername = authState.hasFullAccess,
            isSaving = profileState.isSaving,
            isOnline = isOnline,
            errorMessage = profileState.errorMessage,
            onSave = { name, username ->
                profileVm.onDisplaynameChanged(name)
                profileVm.onUsernameChanged(username)
                profileVm.saveProfile()
            },
            onDismiss = profileVm::cancelEditing,
        )
    }
    AccountDialogs(
        state = profileState,
        pendingAvatar = pendingAvatar,
        showChangePassword = showChangePassword,
        onConfirmLogout = profileVm::confirmLogout,
        onCancelLogout = profileVm::cancelLogout,
        onDeletePasswordChange = profileVm::onDeleteAccountPasswordChange,
        onConfirmDelete = profileVm::onDeleteAccountConfirm,
        onDismissDelete = profileVm::onDeleteAccountDialogDismiss,
        onAvatarCropped = { cropped ->
            pendingAvatar = null
            profileVm.uploadAvatar(cropped.bytes, cropped.fileName)
        },
        onDismissCrop = { pendingAvatar = null },
        onDismissChangePassword = { showChangePassword = false },
    )
    if (isAddingGame) {
        AddLibraryGameSheet(
            title = "Adicionar à biblioteca",
            screen = AppScreen.ADD_TO_LIBRARY,
            onDismiss = { isAddingGame = false },
            onPick = { game -> isAddingGame = false; libraryVm.add(game) },
        )
    }
    library.openGame?.let { game ->
        LibraryGameSheet(
            game = game,
            canEdit = isOnline && !library.isSaving,
            onToggle = { platform -> libraryVm.toggleOwned(game, platform) },
            onRemove = { libraryVm.remove(game) },
            onDismiss = libraryVm::closeGame,
        )
    }
}
