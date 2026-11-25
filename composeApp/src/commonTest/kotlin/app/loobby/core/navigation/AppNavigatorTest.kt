package app.loobby.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppNavigatorTest {

    private val gameplay = AppRoute.GameplayDetail("g1")
    private val group = AppRoute.GroupHome("grp", "Turma do CS")

    @Test
    fun cadaAba_guardaAPropriaPilha() {
        val nav = AppNavigator()
        nav.navigate(gameplay)
        nav.selectTab(AppTab.PROFILE)

        assertEquals(AppRoute.Profile, nav.current.route)

        nav.selectTab(AppTab.HOME)
        assertEquals(gameplay, nav.current.route)
    }

    @Test
    fun tocarNaAbaAberta_voltaParaARaiz_eDescartaOQueEstavaPorCima() {
        val nav = AppNavigator()
        val removed = mutableListOf<AppRoute>()
        nav.onEntryRemoved = { removed += it.route }
        nav.navigate(gameplay)

        nav.selectTab(AppTab.HOME)

        assertEquals(AppRoute.Lobby, nav.current.route)
        assertEquals(listOf<AppRoute>(gameplay), removed)
    }

    @Test
    fun voltarNaRaizDeOutraAba_vaiParaOInicio() {
        val nav = AppNavigator()
        nav.selectTab(AppTab.HISTORY)

        assertTrue(nav.canGoBack)
        assertTrue(nav.goBack())
        assertEquals(AppTab.HOME, nav.currentTab)
    }

    @Test
    fun voltarNaRaizDoInicio_deixaOSistemaFecharOApp() {
        val nav = AppNavigator()

        assertFalse(nav.canGoBack)
        assertFalse(nav.goBack())
    }

    @Test
    fun trocarARaizDoInicio_descartaAPilhaAntiga() {
        val nav = AppNavigator()
        val removed = mutableListOf<AppRoute>()
        nav.onEntryRemoved = { removed += it.route }
        nav.navigate(gameplay)

        nav.replaceRoot(AppTab.HOME, group)

        assertEquals(listOf(group), nav.stack(AppTab.HOME).map { it.route })
        assertEquals(listOf(AppRoute.Lobby, gameplay), removed)
    }

    @Test
    fun push_abreAGameplayPorCimaDoGrupo_vindoDeOutraAba() {
        val nav = AppNavigator()
        nav.selectTab(AppTab.PROFILE)

        nav.openOnHome(root = group, route = gameplay)

        assertEquals(AppTab.HOME, nav.currentTab)
        assertEquals(listOf(group, gameplay), nav.stack(AppTab.HOME).map { it.route })
    }

    @Test
    fun pushComAMesmaRaiz_soLimpaOQueEstavaPorCima() {
        val nav = AppNavigator(homeRoot = group)
        nav.navigate(AppRoute.GroupDetail("grp"))
        val rootId = nav.stack(AppTab.HOME).first().id

        nav.openOnHome(root = group, route = gameplay)

        assertEquals(rootId, nav.stack(AppTab.HOME).first().id)
        assertEquals(listOf(group, gameplay), nav.stack(AppTab.HOME).map { it.route })
    }

    @Test
    fun cadaTelaAberta_recebeUmIdNovo() {
        val nav = AppNavigator()
        nav.navigate(gameplay)
        nav.goBack()
        nav.navigate(gameplay)

        val ids = AppTab.entries.flatMap { nav.stack(it) }.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(nav.current.id > 1)
    }
}
