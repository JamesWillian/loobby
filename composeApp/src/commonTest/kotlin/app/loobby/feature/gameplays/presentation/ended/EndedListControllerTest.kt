package app.loobby.feature.gameplays.presentation.ended

import app.loobby.core.analytics.CrashReporter
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.GameplayStatus
import app.loobby.feature.gameplays.domain.model.GameplaySummary
import app.loobby.feature.gameplays.domain.model.GameplayUser
import app.loobby.feature.gameplays.domain.model.GameplaysPage
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

/** As páginas do Histórico e das Finalizadas: a primeira, as seguintes e as falhas. */
class EndedListControllerTest {

    private fun summary(id: String) = GameplaySummary(
        id = id, groupId = null, groupName = null, name = "Gameplay $id", mode = GameplayMode.FREE,
        platforms = emptyList(), scheduledAt = Instant.parse("2026-11-20T00:00:00Z"),
        autoEndAt = Instant.parse("2026-11-21T00:00:00Z"), status = GameplayStatus.ENDED, endedAt = null,
        creator = GameplayUser("u", "u", "", null), myStatus = ParticipationStatus.GOING, goingCount = 1,
        goingAvatars = listOf(null), game = null, leading = emptyList(), tie = false,
    )

    /** Uma "API" de 5 encerradas, em páginas de 2. */
    private class FakePages(private val all: List<String>, var failNext: Boolean = false) {
        val calls = mutableListOf<String?>()
        fun page(before: String?, make: (String) -> GameplaySummary): GameplaysPage {
            calls += before
            if (failNext) {
                failNext = false
                throw IOException("sem rede")
            }
            val start = before?.let { all.indexOf(it) + 1 } ?: 0
            val ids = all.drop(start).take(2)
            return GameplaysPage(ids.map(make), mayHaveMore = ids.size == 2)
        }
    }

    private fun TestScope.controller(pages: FakePages) = EndedListController(
        scope = this,
        crashReporter = CrashReporter { },
        errorMessage = "Não deu.",
        load = { before -> pages.page(before, ::summary) },
    )

    @Test
    fun primeiraPagina_eAsSeguintes_ateOFim() = runTest {
        val pages = FakePages(listOf("e", "d", "c", "b", "a"))
        val list = controller(pages)

        list.refresh()
        advanceUntilIdle()
        assertEquals(listOf("e", "d"), list.state.value.gameplays.map { it.id })
        assertTrue(list.state.value.canLoadMore)

        list.loadMore()
        advanceUntilIdle()
        list.loadMore()
        advanceUntilIdle()
        assertEquals(listOf("e", "d", "c", "b", "a"), list.state.value.gameplays.map { it.id })
        assertFalse(list.state.value.mayHaveMore) // a última página veio com 1
        assertEquals(listOf(null, "d", "b"), pages.calls)

        list.loadMore() // não há mais: nada é pedido
        advanceUntilIdle()
        assertEquals(3, pages.calls.size)
    }

    @Test
    fun paginaSeguinteFalha_oFimOfereceTentarDeNovo() = runTest {
        val pages = FakePages(listOf("c", "b", "a"))
        val list = controller(pages)
        list.refresh()
        advanceUntilIdle()

        pages.failNext = true
        list.loadMore()
        advanceUntilIdle()
        assertTrue(list.state.value.loadMoreFailed)
        assertFalse(list.state.value.canLoadMore) // o efeito de rolar não insiste sozinho
        assertEquals(listOf("c", "b"), list.state.value.gameplays.map { it.id })

        list.loadMore() // o botão Tentar de novo
        advanceUntilIdle()
        assertFalse(list.state.value.loadMoreFailed)
        assertEquals(listOf("c", "b", "a"), list.state.value.gameplays.map { it.id })
    }

    @Test
    fun remove_tiraAGameplayNaHora_eAPaginaSeguinteContinuaDaUltima() = runTest {
        val server = mutableListOf("e", "d", "c", "b", "a")
        val pages = FakePages(server)
        val list = controller(pages)
        list.refresh()
        advanceUntilIdle()

        server.remove("d") // excluída na API...
        list.remove("d") // ...e na tela, sem recarregar
        assertEquals(listOf("e"), list.state.value.gameplays.map { it.id })
        list.remove("x") // uma que não está na lista não muda nada
        assertEquals(listOf("e"), list.state.value.gameplays.map { it.id })

        list.loadMore()
        advanceUntilIdle()
        assertEquals("e", pages.calls.last()) // a página seguinte parte da que ficou por último
        assertEquals(listOf("e", "c", "b"), list.state.value.gameplays.map { it.id })
    }

    @Test
    fun primeiraPaginaFalha_mostraOErro() = runTest {
        val pages = FakePages(listOf("a"), failNext = true)
        val list = controller(pages)

        list.refresh()
        advanceUntilIdle()

        assertFalse(list.state.value.isLoading)
        assertTrue(list.state.value.gameplays.isEmpty())
        assertEquals("Sem conexão com a internet. Confira a rede e tente de novo.", list.state.value.errorMessage)
    }
}
