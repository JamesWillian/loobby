package app.loobby.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * A navegação do app: uma pilha de telas por aba da barra de baixo.
 *
 * - Trocar de aba guarda a pilha da anterior como estava.
 * - Tocar na aba que já está aberta volta para a raiz dela.
 * - Voltar desempilha a aba atual; na raiz de outra aba, volta para o Início.
 *
 * Toda tela que sai de uma pilha é descartada ([NavEntry.dispose]) e avisada a
 * [onEntryRemoved], para quem guarda o estado dela também esquecer.
 */
class AppNavigator(homeRoot: AppRoute = AppRoute.Lobby) {

    private var nextId = 0L

    private val stacks: Map<AppTab, SnapshotStateList<NavEntry>> =
        AppTab.entries.associateWith { tab ->
            mutableStateListOf(newEntry(if (tab == AppTab.HOME) homeRoot else tab.root))
        }

    /** Quem guarda o estado das telas se inscreve aqui para limpar o que saiu. */
    var onEntryRemoved: (NavEntry) -> Unit = {}

    var currentTab by mutableStateOf(AppTab.HOME)
        private set

    /** A tela no topo da aba atual. */
    val current: NavEntry get() = stack(currentTab).last()

    /** A raiz do Início: o Lobby ou um grupo. */
    val homeRoot: AppRoute get() = stack(AppTab.HOME).first().route

    /** Há para onde voltar sem sair do app. */
    val canGoBack: Boolean get() = stack(currentTab).size > 1 || currentTab != AppTab.HOME

    fun stack(tab: AppTab): List<NavEntry> = stacks.getValue(tab)

    fun selectTab(tab: AppTab) {
        if (tab == currentTab) popToRoot(tab) else currentTab = tab
    }

    /** Empilha [route] na aba atual. */
    fun navigate(route: AppRoute) {
        mutableStack(currentTab).add(newEntry(route))
    }

    /** Volta uma tela. Devolve false quando já está na raiz do Início. */
    fun goBack(): Boolean {
        val stack = mutableStack(currentTab)
        return when {
            stack.size > 1 -> {
                remove(stack.removeAt(stack.lastIndex))
                true
            }
            currentTab != AppTab.HOME -> {
                currentTab = AppTab.HOME
                true
            }
            else -> false
        }
    }

    /** Troca a raiz de [tab], descartando tudo o que estava empilhado nela. */
    fun replaceRoot(tab: AppTab, route: AppRoute) {
        val stack = mutableStack(tab)
        val old = stack.toList()
        stack.clear()
        stack.add(newEntry(route))
        old.forEach(::remove)
    }

    /**
     * Abre [route] por cima de [root] no Início, o caminho de um push: a seta de
     * voltar leva ao grupo ou ao Lobby.
     */
    fun openOnHome(root: AppRoute, route: AppRoute) {
        if (homeRoot != root) replaceRoot(AppTab.HOME, root) else popToRoot(AppTab.HOME)
        currentTab = AppTab.HOME
        navigate(route)
    }

    /** Volta tudo ao começo, no Início: para quando outra conta entra. */
    fun reset(homeRoot: AppRoute = AppRoute.Lobby) {
        AppTab.entries.forEach { tab -> replaceRoot(tab, if (tab == AppTab.HOME) homeRoot else tab.root) }
        currentTab = AppTab.HOME
    }

    /** Descarta todas as telas. Para quando o próprio navegador deixa de existir. */
    fun disposeAll() {
        stacks.values.forEach { stack -> stack.forEach(NavEntry::dispose) }
    }

    private fun popToRoot(tab: AppTab) {
        val stack = mutableStack(tab)
        while (stack.size > 1) remove(stack.removeAt(stack.lastIndex))
    }

    private fun remove(entry: NavEntry) {
        entry.dispose()
        onEntryRemoved(entry)
    }

    private fun mutableStack(tab: AppTab): SnapshotStateList<NavEntry> = stacks.getValue(tab)

    private fun newEntry(route: AppRoute) = NavEntry(id = nextId++, route = route)
}

@Composable
fun rememberAppNavigator(homeRoot: () -> AppRoute): AppNavigator =
    remember { AppNavigator(homeRoot()) }
