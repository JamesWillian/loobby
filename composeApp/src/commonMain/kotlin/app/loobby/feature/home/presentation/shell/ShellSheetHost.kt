package app.loobby.feature.home.presentation.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.feature.auth.presentation.AnonNicknameSheet
import app.loobby.feature.auth.presentation.AuthBottomSheet
import app.loobby.feature.auth.presentation.ProfileBottomSheet
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.presentation.form.GameplayFormMode
import app.loobby.feature.gameplays.presentation.form.GameplayFormSheet
import app.loobby.feature.gameplays.presentation.invite.InviteJoined
import app.loobby.feature.gameplays.presentation.invite.JoinInviteSheet
import app.loobby.feature.groups.presentation.GroupsViewModel
import app.loobby.feature.groups.ui.CreateGroupSheet
import app.loobby.feature.home.presentation.components.CreateOrJoinSheet
import org.koin.compose.koinInject

/** O que os sheets do shell pedem ao app depois de concluídos. */
interface ShellSheetCallbacks {
    fun open(sheet: ShellSheet)
    fun close()
    fun requireFullAccess(action: () -> Unit)
    fun onGroupCreatedOrJoined(groupId: String, groupName: String)
    fun onGameplayReady(gameplay: Gameplay)
    fun onGameplayJoined(gameplayId: String)
    fun onAuthClosed()
}

/** Mostra o [sheet] da vez. */
@Composable
fun ShellSheetHost(
    sheet: ShellSheet?,
    callbacks: ShellSheetCallbacks,
    groupsVm: GroupsViewModel = koinInject(),
) {
    when (sheet) {
        null -> Unit
        ShellSheet.CreateOrJoin -> CreateOrJoinSheet(
            onDismiss = callbacks::close,
            onNewGameplay = { callbacks.requireFullAccess { callbacks.open(ShellSheet.NewGameplay()) } },
            onCreateGroup = { callbacks.requireFullAccess { callbacks.open(ShellSheet.CreateGroup) } },
            onJoinInvite = { callbacks.open(ShellSheet.JoinInvite) },
        )
        ShellSheet.CreateGroup -> {
            val groupsState by groupsVm.uiState.collectAsState()
            TrackScreen(AppScreen.CREATE_GROUP)
            CreateGroupSheet(
                isLoading = groupsState.isCreatingGroup,
                onDismiss = callbacks::close,
                onCreateGroup = { name -> groupsVm.createNewGroup(name, callbacks::onGroupCreatedOrJoined) },
            )
        }
        ShellSheet.JoinInvite -> JoinInviteSheet(
            onDismiss = callbacks::close,
            onJoined = { joined ->
                when (joined) {
                    is InviteJoined.Group -> callbacks.onGroupCreatedOrJoined(joined.groupId, joined.groupName)
                    is InviteJoined.Gameplay -> callbacks.onGameplayJoined(joined.gameplayId)
                }
            },
        )
        is ShellSheet.NewGameplay -> GameplayFormSheet(
            mode = GameplayFormMode.Create(groupId = sheet.groupId, groupName = sheet.groupName),
            onDismiss = callbacks::close,
            onSaved = callbacks::onGameplayReady,
        )
        is ShellSheet.Auth -> {
            TrackScreen(AppScreen.AUTH)
            AuthBottomSheet(welcomeName = sheet.welcomeName, onDismiss = callbacks::onAuthClosed)
        }
        ShellSheet.Profile -> {
            TrackScreen(AppScreen.PROFILE)
            ProfileBottomSheet(onDismiss = callbacks::close)
        }
        ShellSheet.Nickname -> {
            TrackScreen(AppScreen.NICKNAME)
            AnonNicknameSheet(
                onDismiss = callbacks::close,
                onOpenAuth = { callbacks.open(ShellSheet.Auth()) },
            )
        }
    }
}
