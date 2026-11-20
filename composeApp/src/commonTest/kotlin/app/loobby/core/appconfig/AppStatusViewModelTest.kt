package app.loobby.core.appconfig

import app.loobby.core.analytics.CrashReporter
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

/**
 * Quando o app consulta o /app/config e o que ele faz com cada resultado: o cache vale
 * na abertura, falha nenhuma bloqueia quem não estava bloqueado, e o aviso fechado não
 * volta.
 */
@OptIn(ExperimentalTime::class)
class AppStatusViewModelTest {

    private val blocked = """{"android":{"minVersion":8,"storeUrl":"https://play.google.com/store/apps/details?id=app.loobby"}}"""
    private val withNotice = """{"android":{"minVersion":0,"notice":{"id":"eventos","title":"T","message":"M","maxVersion":7}}}"""

    private class FakeApi(var next: () -> String) : AppConfigApi {
        var calls = 0
        override suspend fun fetchConfig(): String {
            calls++
            return next()
        }
    }

    private class FakeStore(var last: String? = null) : AppConfigStore {
        val seen = linkedSetOf<String>()
        override fun lastConfig() = last
        override fun saveConfig(raw: String) {
            last = raw
        }
        override fun seenNoticeIds(): Set<String> = seen.toSet()
        override fun markNoticeSeen(id: String) {
            seen += id
        }
    }

    private val reported = mutableListOf<Throwable>()
    private val time = TestTimeSource()

    private fun TestScope.vm(api: AppConfigApi, store: AppConfigStore, build: Long = 7) = AppStatusViewModel(
        api = api,
        store = store,
        version = AppVersion(AppPlatform.ANDROID, build, "1.3.1"),
        crashReporter = CrashReporter { reported += it },
        timeSource = time,
        dispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun semCache_abre_eBloqueiaQuandoAApiResponde() = runTest {
        val store = FakeStore()
        val vm = vm(FakeApi { blocked }, store)
        assertNull(vm.status.value.requiredUpdate)

        advanceUntilIdle()

        assertNotNull(vm.status.value.requiredUpdate)
        assertEquals(blocked, store.last)
    }

    @Test
    fun cacheBloqueado_bloqueiaNaAbertura_mesmoSemRede() = runTest {
        val vm = vm(FakeApi { throw IOException("sem rede") }, FakeStore(last = blocked))
        assertNotNull(vm.status.value.requiredUpdate) // antes de qualquer resposta

        advanceUntilIdle()

        assertNotNull(vm.status.value.requiredUpdate)
        assertTrue(reported.isEmpty(), "falta de rede não é falha do app")
    }

    @Test
    fun cacheBloqueado_comOAppJaAtualizado_naoBloqueia() = runTest {
        val vm = vm(FakeApi { throw IOException("sem rede") }, FakeStore(last = blocked), build = 8)
        assertNull(vm.status.value.requiredUpdate)
    }

    @Test
    fun qualquerFalha_semCache_mantemOAppAberto() = runTest {
        // a API publicada antes desta rota responde 401 a ela; um bug qualquer, idem
        val vm = vm(FakeApi { throw IllegalStateException("inesperado") }, FakeStore())
        advanceUntilIdle()
        assertEquals(AppStatus(), vm.status.value)
    }

    @Test
    fun respostaIlegivel_mantemOCache_eVaiParaOCrashlytics() = runTest {
        val store = FakeStore(last = withNotice)
        val vm = vm(FakeApi { "isto não é json" }, store)
        advanceUntilIdle()

        assertEquals("eventos", vm.status.value.notice?.id)
        assertEquals(withNotice, store.last)
        assertIs<SerializationException>(reported.single())
    }

    @Test
    fun avisoFechado_naoVolta_nemComNovaConsulta() = runTest {
        val store = FakeStore()
        val vm = vm(FakeApi { withNotice }, store)
        advanceUntilIdle()
        assertEquals("eventos", vm.status.value.notice?.id)

        vm.dismissNotice("eventos")
        assertNull(vm.status.value.notice)
        assertEquals(setOf("eventos"), store.seen)

        time += 10.minutes
        vm.refresh()
        advanceUntilIdle()
        assertNull(vm.status.value.notice)
    }

    @Test
    fun refresh_esperaOIntervaloDepoisDeUmaConsultaBemSucedida() = runTest {
        val api = FakeApi { withNotice }
        val vm = vm(api, FakeStore())
        advanceUntilIdle()
        assertEquals(1, api.calls)

        vm.refresh() // a volta ao app logo depois da abertura
        advanceUntilIdle()
        assertEquals(1, api.calls)

        time += 6.minutes
        vm.refresh()
        advanceUntilIdle()
        assertEquals(2, api.calls)
    }

    @Test
    fun falha_naoContaParaOIntervalo() = runTest {
        var fail = true
        val api = FakeApi { if (fail) throw IOException("sem rede") else blocked }
        val vm = vm(api, FakeStore())
        advanceUntilIdle()
        assertNull(vm.status.value.requiredUpdate)

        fail = false
        vm.refresh() // a rede voltou: não espera os 5 minutos
        advanceUntilIdle()
        assertEquals(2, api.calls)
        assertNotNull(vm.status.value.requiredUpdate)
    }

    @Test
    fun apiBaixaAMinima_desbloqueia() = runTest {
        val unblocked = """{"android":{"minVersion":0}}"""
        val vm = vm(FakeApi { unblocked }, FakeStore(last = blocked))
        assertNotNull(vm.status.value.requiredUpdate)

        advanceUntilIdle()

        assertNull(vm.status.value.requiredUpdate)
    }
}
