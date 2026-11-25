package app.loobby.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** O texto da exceção é o que o cliente autenticado monta: "HTTP <status>: <corpo>". */
class ApiErrorMessageTest {

    private fun raw(status: Int, body: String) =
        """Client request(PUT https://api.loobby.app/v2/gameplays/1/votes/me) invalid: $status. Text: "HTTP $status: $body""""

    @Test
    fun conflito_usaAMensagemDaApi() {
        val body = """{"timestamp":"2026-11-25T10:00:00Z","status":409,"error":"Conflict","message":"Quem marcou Não vou não vota","path":"/v2/gameplays/1/votes/me"}"""

        assertEquals("Quem marcou Não vou não vota", ApiErrorMessage.from(409, raw(409, body)))
    }

    @Test
    fun validacao_usaAMensagemDoPrimeiroCampo() {
        val body = """{"status":400,"message":"Dados inválidos","fieldErrors":[{"field":"platforms","message":"Escolha a plataforma"},{"field":"name","message":"Informe o nome"}]}"""

        assertEquals("Escolha a plataforma", ApiErrorMessage.from(400, raw(400, body)))
    }

    @Test
    fun erroDoServidor_naoMostraOTextoCru() {
        val body = """{"status":500,"message":"pq: deadlock detected"}"""

        assertEquals(ApiErrorMessage.SERVER_ERROR, ApiErrorMessage.from(500, raw(500, body)))
    }

    @Test
    fun corpoQueNaoEJson_naoTemMensagem() {
        assertNull(ApiErrorMessage.from(404, raw(404, "<html>Not Found</html>")))
        assertNull(ApiErrorMessage.from(404, null))
    }

    @Test
    fun mensagemEmBranco_naoServe() {
        val body = """{"status":403,"message":"  ","fieldErrors":null}"""

        assertNull(ApiErrorMessage.from(403, raw(403, body)))
    }

    @Test
    fun semConexao_eSemRede_viramTextosFixos() {
        assertEquals(ApiErrorMessage.NO_CONNECTION, kotlinx.io.IOException("reset").toUserMessage("x"))
        assertEquals("Você precisa estar online para realizar esta ação.", OfflineException().toUserMessage("x"))
        assertEquals("x", IllegalStateException("bug interno").toUserMessage("x"))
    }
}
