package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.GameplayPushOpened
import app.loobby.core.lifecycle.OnResumeEffect
import app.loobby.core.navigation.AppNavigator
import app.loobby.core.navigation.AppRoute
import app.loobby.core.navigation.AppTab
import app.loobby.core.navigation.DeepLinkCoordinator
import app.loobby.core.navigation.NavEntryHost
import app.loobby.core.navigation.PlatformBackHandler
import app.loobby.core.navigation.isHomeRoot
import app.loobby.core.navigation.rememberAppNavigator
import app.loobby.core.navigation.rememberNavEntryStates
import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.network.OfflineBanner
import app.loobby.core.ui.LocalAppSnackbar
import app.loobby.feature.auth.presentation.AuthUiState
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.auth.presentation.EmailVerificationBanner
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.home.domain.HomeSelection
import app.loobby.feature.home.presentation.HomeUiState
import app.loobby.feature.home.presentation.HomeViewModel
import app.loobby.feature.home.presentation.components.AppBottomBar
import app.loobby.feature.home.presentation.components.HomeRail
import app.loobby.feature.home.presentation.shell.HomeRailLayout
import app.loobby.feature.home.presentation.shell.ShellSheet
import app.loobby.feature.home.presentation.shell.ShellSheetCallbacks
import app.loobby.feature.home.presentation.shell.SessionChangeEffect
import app.loobby.feature.home.presentation.shell.ShellSheetHost
import app.loobby.theme.Spacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * O esqueleto do app na 2.0: a barra de baixo (Início, Histórico, Perfil), uma pilha
 * de telas por aba, a barra lateral nas raízes do Início, os sheets que abrem por
 * cima de tudo, o push que abre uma gameplay e os avisos de e-mail e de rede.
 */
@Composable
fun AppShell(
    authVm: AuthViewModel = koinInject(),
    homeVm: HomeViewModel = koinViewModel(),
    deepLinks: DeepLinkCoordinator = koinInject(),
    connectivity: ConnectivityObserver = koinInject(),
    analytics: AnalyticsTracker = koinInject(),
) {
    val authState by authVm.uiState.collectAsState()
    val homeState by homeVm.state.collectAsState()
    val isOnline by connectivity.isOnline.collectAsState(initial = true)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val navigator = rememberAppNavigator { homeVm.initialHomeRoot() }
    val entryStates = rememberNavEntryStates(navigator)
    var sheet by remember { mutableStateOf<ShellSheet?>(null) }

    val controller = remember(navigator, homeVm, scope, snackbar) {
        ShellController(navigator, homeVm, scope, snackbar) { sheet = it }
    }
    SideEffect { controller.authState = authState }

    PlatformBackHandler(enabled = navigator.canGoBack) { navigator.goBack() }
    OnResumeEffect { if (authState.needsEmailVerification) authVm.checkEmailVerification() }
    GameplayDeepLinkEffect(deepLinks, controller, analytics)
    SessionChangeEffect(userId = authState.profile?.id, onUserChanged = controller::resetForNewUser)
    LeftGroupEffect(homeState, navigator, controller)

    CompositionLocalProvider(LocalIsOnline provides isOnline, LocalAppSnackbar provides snackbar) {
        ShellSheetHost(sheet = sheet, callbacks = controller)

        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                AppBottomBar(
                    currentTab = navigator.currentTab,
                    avatarUrl = authState.profile?.avatarUrl,
                    onSelect = navigator::selectTab,
                )
            },
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val entry = navigator.current
                    HomeRailLayout(
                        showRail = navigator.currentTab == AppTab.HOME && entry.route.isHomeRoot && homeState.isRailOpen,
                        rail = {
                            HomeRail(
                                groups = homeState.groups,
                                current = navigator.homeRoot,
                                onSelectLobby = { controller.selectHome(AppRoute.Lobby) },
                                onSelectGroup = { controller.selectHome(AppRoute.GroupHome(it.id, it.name)) },
                                onAdd = { sheet = ShellSheet.CreateOrJoin },
                            )
                        },
                    ) {
                        NavEntryHost(states = entryStates, entry = entry) { route ->
                            AppContent(
                                route = route,
                                state = AppContentState(
                                    isRailOpen = homeState.isRailOpen,
                                    isAnonymous = authState.isAnonymous,
                                    profile = authState.profile,
                                    groups = homeState.groups,
                                ),
                                actions = controller,
                            )
                        }
                    }
                }
                EmailVerificationBanner(
                    visible = authState.needsEmailVerification,
                    email = authState.profile?.email,
                    isResending = authState.isResendingVerification,
                    cooldownSeconds = authState.resendCooldownSeconds,
                    message = authState.verificationMessage,
                    onResendClick = { authVm.resendVerificationEmail() },
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
                OfflineBanner(
                    visible = !isOnline,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
            }
        }
    }
}

/**
 * As ações das telas e dos sheets, num lugar só: navegar, abrir sheet, pedir conta.
 * O estado da conta é atualizado a cada recomposição do shell.
 */
private class ShellController(
    private val navigator: AppNavigator,
    private val homeVm: HomeViewModel,
    private val scope: CoroutineScope,
    private val snackbar: SnackbarHostState,
    private val showSheet: (ShellSheet?) -> Unit,
) : AppContentActions, ShellSheetCallbacks {

    var authState: AuthUiState = AuthUiState()

    /**
     * Troca a raiz do Início pelo Lobby ou por um grupo, lembrando a escolha. Tocar
     * no item que já está escolhido só volta para a raiz, sem recarregar a tela.
     */
    fun selectHome(root: AppRoute) {
        homeVm.select(root.toSelection())
        if (root.sameHomeRootAs(navigator.homeRoot)) {
            if (navigator.currentTab == AppTab.HOME) navigator.selectTab(AppTab.HOME)
        } else {
            navigator.replaceRoot(AppTab.HOME, root)
        }
        if (navigator.currentTab != AppTab.HOME) navigator.selectTab(AppTab.HOME)
    }

    /** Outra conta entrou: nada da anterior fica aberto. */
    fun resetForNewUser() {
        homeVm.select(HomeSelection.Lobby)
        navigator.reset(AppRoute.Lobby)
    }

    /** Abre uma gameplay por cima do grupo dela (ou do Lobby), como faz o push. */
    fun openGameplayOnHome(gameplayId: String, groupId: String?) {
        val root = groupId?.let { AppRoute.GroupHome(it) } ?: AppRoute.Lobby
        homeVm.select(root.toSelection())
        navigator.openOnHome(root = root, route = AppRoute.GameplayDetail(gameplayId))
    }

    // ── AppContentActions ────────────────────────────────────────────────

    override fun toggleRail() = homeVm.toggleRail()
    override fun openGameplay(gameplayId: String) = navigator.navigate(AppRoute.GameplayDetail(gameplayId))
    override fun openGroupDetail(groupId: String) = navigator.navigate(AppRoute.GroupDetail(groupId))
    override fun newGameplay(groupId: String?, groupName: String?) =
        requireFullAccess { showSheet(ShellSheet.NewGameplay(groupId, groupName)) }
    override fun joinInvite() = showSheet(ShellSheet.JoinInvite)
    override fun createGroup() = requireFullAccess { showSheet(ShellSheet.CreateGroup) }
    override fun login() = showSheet(ShellSheet.Auth())
    override fun goBack() {
        navigator.goBack()
    }

    /** O Perfil: o sheet de conta para quem tem conta; apelido ou login para o anônimo. */
    override fun editProfile() {
        val name = authState.profile?.displayname
        when {
            !authState.isAnonymous -> showSheet(ShellSheet.Profile)
            name.isNullOrBlank() || name.startsWith("user_") -> showSheet(ShellSheet.Nickname)
            else -> showSheet(ShellSheet.Auth(welcomeName = name))
        }
    }

    override fun leftGroup() {
        homeVm.refreshGroups()
        selectHome(AppRoute.Lobby)
    }

    // ── ShellSheetCallbacks ──────────────────────────────────────────────

    override fun open(sheet: ShellSheet) = showSheet(sheet)
    override fun close() = showSheet(null)

    override fun requireFullAccess(action: () -> Unit) {
        when {
            authState.hasFullAccess -> action()
            authState.isAnonymous -> showSheet(ShellSheet.Auth())
            authState.needsEmailVerification -> {
                showSheet(null)
                scope.launch { snackbar.showSnackbar("Confirme seu e-mail para liberar esta ação.") }
            }
        }
    }

    override fun onGroupCreatedOrJoined(groupId: String, groupName: String) {
        showSheet(null)
        homeVm.refreshGroups()
        selectHome(AppRoute.GroupHome(groupId, groupName))
    }

    override fun onGameplayReady(gameplay: Gameplay) {
        showSheet(null)
        openGameplayOnHome(gameplay.id, gameplay.groupId)
    }

    override fun onGameplayJoined(gameplayId: String) {
        showSheet(null)
        openGameplayOnHome(gameplayId, groupId = null)
    }

    override fun onAuthClosed() {
        showSheet(null)
        homeVm.refreshGroups()
    }

    private fun AppRoute.toSelection(): HomeSelection =
        (this as? AppRoute.GroupHome)?.let { HomeSelection.Group(it.groupId) } ?: HomeSelection.Lobby

    /** O nome do grupo pode chegar depois do id: as raízes valem pelo grupo. */
    private fun AppRoute.sameHomeRootAs(other: AppRoute): Boolean = when (this) {
        is AppRoute.GroupHome -> other is AppRoute.GroupHome && other.groupId == groupId
        else -> this == other
    }
}

/** Tocar num push da 2.0 abre a gameplay por cima do grupo dela, ou do Lobby. */
@Composable
private fun GameplayDeepLinkEffect(
    deepLinks: DeepLinkCoordinator,
    controller: ShellController,
    analytics: AnalyticsTracker,
) {
    val pending by deepLinks.pending.collectAsState()
    LaunchedEffect(pending) {
        val link = pending ?: return@LaunchedEffect
        analytics.track(GameplayPushOpened(link.pushType))
        controller.openGameplayOnHome(link.gameplayId, link.groupId)
        deepLinks.consume()
    }
}

/**
 * A pessoa saiu ou foi tirada do grupo que é a raiz do Início (aqui ou noutro
 * aparelho): o Início volta para o Lobby quando a lista de grupos chegar sem ele.
 */
@Composable
private fun LeftGroupEffect(homeState: HomeUiState, navigator: AppNavigator, controller: ShellController) {
    LaunchedEffect(homeState.groups, homeState.hasLoadedGroups) {
        val root = navigator.homeRoot as? AppRoute.GroupHome ?: return@LaunchedEffect
        if (homeState.hasLoadedGroups && homeState.groups.none { it.id == root.groupId }) {
            controller.selectHome(AppRoute.Lobby)
        }
    }
}
