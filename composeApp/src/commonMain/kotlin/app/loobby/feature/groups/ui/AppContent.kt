package app.loobby.feature.groups.ui

import androidx.compose.runtime.Composable
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.core.navigation.AppRoute
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.gameplays.presentation.detail.GameplayDetailScreen
import app.loobby.feature.gameplays.presentation.ended.EndedGameplaysScreen
import app.loobby.feature.gameplays.presentation.grouphome.GroupHomeScreen
import app.loobby.feature.gameplays.presentation.lobby.LobbyScreen
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.presentation.GroupDetailScreen
import app.loobby.feature.home.presentation.history.HistoryScreen
import app.loobby.feature.home.presentation.profile.ProfileTabScreen

/** O que as telas pedem ao shell: navegar, abrir sheets, pedir login. */
interface AppContentActions {
    fun toggleRail()
    fun openGameplay(gameplayId: String)
    fun openGroupDetail(groupId: String)
    fun openEndedGameplays(groupId: String, groupName: String)
    fun newGameplay(groupId: String?, groupName: String?)
    fun joinInvite()
    fun createGroup()
    fun login()
    fun chooseNickname()
    fun goBack()
    fun leftGroup()
}

/** O que as telas leem do shell. */
data class AppContentState(
    val isRailOpen: Boolean,
    val isAnonymous: Boolean,
    val profile: UserMeResponse?,
    val groups: List<GroupDomain>,
)

/** A tela de cada rota, e a contagem dela no Analytics. */
@Composable
fun AppContent(route: AppRoute, state: AppContentState, actions: AppContentActions) {
    // A chave leva o id só para distinguir um grupo ou gameplay do outro; ela não
    // sai do aparelho.
    TrackScreen(screen = route.screen, key = route.screenKey)

    when (route) {
        AppRoute.Lobby -> LobbyScreen(
            isRailOpen = state.isRailOpen,
            isAnonymous = state.isAnonymous,
            onToggleRail = actions::toggleRail,
            onOpenGameplay = actions::openGameplay,
            onNewGameplay = { actions.newGameplay(groupId = null, groupName = null) },
            onJoinInvite = actions::joinInvite,
            onCreateGroup = actions::createGroup,
            onLogin = actions::login,
        )
        is AppRoute.GroupHome -> {
            val groupName = route.groupName.ifBlank {
                state.groups.firstOrNull { it.id == route.groupId }?.name.orEmpty()
            }
            GroupHomeScreen(
                groupId = route.groupId,
                groupName = groupName,
                isRailOpen = state.isRailOpen,
                canCreate = !state.isAnonymous,
                onToggleRail = actions::toggleRail,
                onOpenGroupDetail = { actions.openGroupDetail(route.groupId) },
                onOpenEnded = { actions.openEndedGameplays(route.groupId, groupName) },
                onOpenGameplay = actions::openGameplay,
                onNewGameplay = { actions.newGameplay(route.groupId, groupName) },
            )
        }
        is AppRoute.GroupDetail -> GroupDetailScreen(
            groupId = route.groupId,
            isAnonymous = state.isAnonymous,
            onBack = actions::goBack,
            onLeaveGroup = actions::leftGroup,
        )
        is AppRoute.EndedGameplays -> EndedGameplaysScreen(
            groupId = route.groupId,
            groupName = route.groupName.ifBlank {
                state.groups.firstOrNull { it.id == route.groupId }?.name.orEmpty()
            },
            onBack = actions::goBack,
            onOpenGameplay = actions::openGameplay,
        )
        is AppRoute.GameplayDetail -> GameplayDetailScreen(
            gameplayId = route.gameplayId,
            isAnonymous = state.isAnonymous,
            currentUserId = state.profile?.id,
            onBack = actions::goBack,
            onLeft = actions::goBack,
            onLogin = actions::login,
        )
        AppRoute.History -> HistoryScreen(
            isAnonymous = state.isAnonymous,
            onLogin = actions::login,
            onOpenGameplay = actions::openGameplay,
        )
        AppRoute.Profile -> ProfileTabScreen(
            isAnonymous = state.isAnonymous,
            onLogin = actions::login,
            onChooseNickname = actions::chooseNickname,
        )
    }
}

private val AppRoute.screen: AppScreen
    get() = when (this) {
        AppRoute.Lobby -> AppScreen.LOBBY
        is AppRoute.GroupHome -> AppScreen.GROUP_HOME
        is AppRoute.GroupDetail -> AppScreen.GROUP_DETAIL
        is AppRoute.EndedGameplays -> AppScreen.GROUP_ENDED_GAMEPLAYS
        is AppRoute.GameplayDetail -> AppScreen.GAMEPLAY_DETAIL
        AppRoute.History -> AppScreen.HISTORY
        AppRoute.Profile -> AppScreen.PROFILE_TAB
    }

/** Identifica a abertura da tela. O nome do grupo fica de fora: ele chega depois do id. */
private val AppRoute.screenKey: String
    get() = when (this) {
        is AppRoute.GroupHome -> "${screen.id}:$groupId"
        is AppRoute.GroupDetail -> "${screen.id}:$groupId"
        is AppRoute.EndedGameplays -> "${screen.id}:$groupId"
        is AppRoute.GameplayDetail -> "${screen.id}:$gameplayId"
        else -> screen.id
    }
