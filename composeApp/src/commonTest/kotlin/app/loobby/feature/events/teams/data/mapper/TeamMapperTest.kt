package app.loobby.feature.events.teams.data.mapper

import app.loobby.feature.events.teams.data.model.EventTeamResponse
import app.loobby.feature.events.teams.data.model.TeamPlayerResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * ──────────────────────────────────────────────────────────────────────────
 *  EXEMPLO 2 — Teste unitário de um mapper (DTO → Domain).
 * ──────────────────────────────────────────────────────────────────────────
 *
 * Mapper é o tipo de código que parece "óbvio demais para testar". Mas é
 * exatamente onde mais entra bug bobo: campo trocado, esquecer de mapear
 * uma lista aninhada, perder um valor default. Os testes aqui são rápidos
 * de escrever e pegam regressão de verdade quando o DTO muda.
 *
 * O que queremos cobrir:
 *  1. Caso "completo" — todos os campos preenchidos chegam intactos.
 *  2. Caso com lista vazia de jogadores — fronteira comum.
 *  3. Caso com nullables (cor, role, displayname, avatar) preservados.
 *  4. Mapeamento aninhado: `players` precisa virar `TeamPlayerDomain`.
 */
class TeamMapperTest {

    @Test
    fun toDomain_mapeiaTodosOsCamposCorretamente() {
        // Arrange — monta um response "cheio", do jeito que vem do backend.
        val response = EventTeamResponse(
            id = "team-1",
            eventId = "event-42",
            order = 0,
            name = "Time A",
            color = "#FF0000",
            players = listOf(
                TeamPlayerResponse(
                    userId = "u1",
                    role = "GOLEIRO",
                    username = "joao",
                    displayname = "João",
                    avatarUrl = "https://cdn/joao.png"
                )
            )
        )

        // Act
        val domain = response.toDomain()

        // Assert — verifica campo a campo. Se você prefere uma única
        // asserção, dá pra montar o `TeamDomain` esperado e comparar com
        // `assertEquals(esperado, atual)` — data classes têm equals/hashCode.
        assertEquals("team-1", domain.id)
        assertEquals("event-42", domain.eventId)
        assertEquals(0, domain.order)
        assertEquals("Time A", domain.name)
        assertEquals("#FF0000", domain.color)
        assertEquals(1, domain.players.size)

        val jogador = domain.players.first()
        assertEquals("u1", jogador.userId)
        assertEquals("GOLEIRO", jogador.role)
        assertEquals("joao", jogador.username)
        assertEquals("João", jogador.displayname)
        assertEquals("https://cdn/joao.png", jogador.avatarUrl)
    }

    @Test
    fun toDomain_quandoTimeSemJogadores_retornaListaVazia() {
        // Casos de fronteira (vazio, nulo, zero) são onde mais aparece bug.
        val response = EventTeamResponse(
            id = "t",
            eventId = "e",
            order = 1,
            name = "Vazio",
            color = null,
            players = emptyList()
        )

        val domain = response.toDomain()

        assertTrue(domain.players.isEmpty(), "esperado nenhum jogador")
        assertEquals(null, domain.color)
    }

    @Test
    fun toDomain_preservaCamposNulos() {
        // Garante que nullables continuam nulos depois do mapeamento.
        // Bug comum: alguém troca `?: ""` no mapper e some o null.
        val response = EventTeamResponse(
            id = "t",
            eventId = "e",
            order = 0,
            name = "X",
            color = null,
            players = listOf(
                TeamPlayerResponse(
                    userId = "u1",
                    role = null,
                    username = "anon",
                    displayname = null,
                    avatarUrl = null
                )
            )
        )

        val jogador = response.toDomain().players.single()

        assertEquals(null, jogador.role)
        assertEquals(null, jogador.displayname)
        assertEquals(null, jogador.avatarUrl)
    }
}
