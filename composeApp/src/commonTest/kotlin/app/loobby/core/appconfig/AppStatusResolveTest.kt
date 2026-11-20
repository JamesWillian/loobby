package app.loobby.core.appconfig

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * A regra que decide bloqueio e aviso. O que mais importa aqui é o lado "na dúvida, o
 * app abre": um engano nesta função trava quem pode usar o app, sem saída.
 */
class AppStatusResolveTest {

    private val store = "https://play.google.com/store/apps/details?id=app.loobby"

    private fun android(build: Long) = AppVersion(AppPlatform.ANDROID, build, "x")

    private fun config(
        minVersion: Long = 0,
        storeUrl: String? = store,
        update: AppMessageResponse? = null,
        notice: AppNoticeResponse? = null,
    ) = AppConfigResponse(
        android = PlatformConfigResponse(minVersion, storeUrl, update, notice),
        ios = null,
    )

    private val eventsNotice = AppNoticeResponse(
        id = "eventos-descontinuados", title = "O Loobby vai mudar", message = "texto", maxVersion = 7,
    )

    // ── Bloqueio ──────────────────────────────────────────────────────────

    @Test
    fun abaixoDaMinima_bloqueia() {
        val update = assertNotNull(config(minVersion = 8).resolve(android(7), emptySet()).requiredUpdate)
        assertEquals(store, update.storeUrl)
        assertNull(update.title) // sem texto da API: a tela usa o dela
    }

    @Test
    fun naMinimaOuAcima_naoBloqueia() {
        assertNull(config(minVersion = 7).resolve(android(7), emptySet()).requiredUpdate)
        assertNull(config(minVersion = 7).resolve(android(9), emptySet()).requiredUpdate)
    }

    @Test
    fun minimaZero_naoBloqueia() {
        assertNull(config(minVersion = 0).resolve(android(1), emptySet()).requiredUpdate)
    }

    @Test
    fun buildDesconhecido_naoBloqueia() {
        assertNull(config(minVersion = 8).resolve(android(0), emptySet()).requiredUpdate)
    }

    @Test
    fun semAParteDaPlataforma_naoBloqueiaNemAvisa() {
        val status = config(minVersion = 99, notice = eventsNotice)
            .resolve(AppVersion(AppPlatform.IOS, 1, "1.0"), emptySet())
        assertEquals(AppStatus(), status)
    }

    @Test
    fun textoDaTelaDeAtualizar_vemDaApi_quandoPreenchido() {
        val update = config(minVersion = 8, update = AppMessageResponse("Chegou a 2.0", "Atualize"))
            .resolve(android(7), emptySet()).requiredUpdate
        assertEquals("Chegou a 2.0", update?.title)
        assertEquals("Atualize", update?.message)

        val blank = config(minVersion = 8, update = AppMessageResponse(" ", ""))
            .resolve(android(7), emptySet()).requiredUpdate
        assertNull(blank?.title)
        assertNull(blank?.message)
    }

    @Test
    fun linkQueNaoEDeLoja_naoViraBotao() {
        for (url in listOf("javascript:alert(1)", "http://inseguro.com", "intent://x", "")) {
            val update = config(minVersion = 8, storeUrl = url).resolve(android(7), emptySet()).requiredUpdate
            assertNotNull(update, url)
            assertNull(update.storeUrl, url)
        }
        assertEquals(
            "market://details?id=app.loobby",
            config(minVersion = 8, storeUrl = "market://details?id=app.loobby")
                .resolve(android(7), emptySet()).requiredUpdate?.storeUrl,
        )
    }

    // ── Aviso ─────────────────────────────────────────────────────────────

    @Test
    fun aviso_apareceAteAMaxVersion() {
        assertEquals("eventos-descontinuados", config(notice = eventsNotice).resolve(android(7), emptySet()).notice?.id)
        assertEquals("eventos-descontinuados", config(notice = eventsNotice).resolve(android(6), emptySet()).notice?.id)
        // a 2.0 não vê o aviso da 1.x, mesmo que ele fique na API
        assertNull(config(notice = eventsNotice).resolve(android(8), emptySet()).notice)
    }

    @Test
    fun aviso_semMaxVersion_valeParaTodas() {
        val notice = eventsNotice.copy(maxVersion = 0)
        assertNotNull(config(notice = notice).resolve(android(500), emptySet()).notice)
        assertNotNull(config(notice = notice).resolve(android(0), emptySet()).notice)
    }

    @Test
    fun aviso_limitadoAVersoes_naoApareceComBuildDesconhecido() {
        assertNull(config(notice = eventsNotice).resolve(android(0), emptySet()).notice)
    }

    @Test
    fun aviso_jaFechado_naoVolta() {
        assertNull(config(notice = eventsNotice).resolve(android(7), setOf("eventos-descontinuados")).notice)
        // outro id é outro aviso
        assertNotNull(config(notice = eventsNotice.copy(id = "outro")).resolve(android(7), setOf("eventos-descontinuados")).notice)
    }

    @Test
    fun aviso_semIdOuSemTexto_eIgnorado() {
        assertNull(config(notice = eventsNotice.copy(id = " ")).resolve(android(7), emptySet()).notice)
        assertNull(config(notice = eventsNotice.copy(message = "")).resolve(android(7), emptySet()).notice)
    }

    @Test
    fun aviso_semTitulo_usaOPadrao() {
        val notice = config(notice = eventsNotice.copy(title = "")).resolve(android(7), emptySet()).notice
        assertEquals(DEFAULT_NOTICE_TITLE, notice?.title)
    }

    // ── Formato ───────────────────────────────────────────────────────────

    /** A resposta que a API Go monta hoje, campo a campo (api-go/internal/appconfig). */
    @Test
    fun leARespostaDaApi() {
        val raw = """{"android":{"minVersion":0,""" +
            """"storeUrl":"https://play.google.com/store/apps/details?id=app.loobby","update":null,""" +
            """"notice":{"id":"eventos-descontinuados","title":"O Loobby vai mudar","message":"Em breve…","maxVersion":7}},""" +
            """"ios":{"minVersion":0,"storeUrl":null,"update":null,"notice":null}}"""
        val status = AppConfigJson.decodeFromString<AppConfigResponse>(raw).resolve(android(7), emptySet())
        assertNull(status.requiredUpdate)
        assertEquals(AppNotice("eventos-descontinuados", "O Loobby vai mudar", "Em breve…"), status.notice)
    }

    @Test
    fun toleraCampoNovoNuloOuAusente() {
        val raw = """{"android":{"minVersion":null,"novoCampo":{"a":1},"notice":{"id":"n","message":"m"}},"outra":1}"""
        val parsed = AppConfigJson.decodeFromString<AppConfigResponse>(raw)
        assertEquals(0L, parsed.android?.minVersion)
        assertEquals("n", parsed.resolve(android(7), emptySet()).notice?.id)
    }
}
