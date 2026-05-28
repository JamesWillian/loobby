package app.loobby.feature.events.teams.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ──────────────────────────────────────────────────────────────────────────
 *  EXEMPLO 1 — Teste unitário de uma função pura.
 * ──────────────────────────────────────────────────────────────────────────
 *
 * Por que começar por aqui?
 *  - `TeamPlayerDomain.displayName` e `TeamPlayerDomain.initials` são
 *    "puros": dada a mesma entrada, sempre retornam a mesma saída,
 *    e não dependem de rede, banco, threads ou plataforma.
 *  - Não precisa de mocks, nem de coroutines, nem de Compose.
 *  - É o teste mais barato e mais confiável que existe.
 *
 * Padrão usado em cada teste: **Arrange → Act → Assert**.
 *  1. Arrange: monta a entrada (cria o objeto).
 *  2. Act:     executa a função/propriedade que está sendo testada.
 *  3. Assert:  verifica que a saída bate com o esperado.
 *
 * Convenção de nomes (recomendada): `metodoSobTeste_cenario_resultadoEsperado`
 * Em Kotlin pode-se usar `backticks` para nomes legíveis em português, mas
 * eu prefiro evitar para não atrapalhar refactors/IDE. Fica a critério.
 */
class TeamPlayerDomainTest {

    // ── displayName ────────────────────────────────────────────────────────
    // Regra: usa `displayname` se existir e não for em branco; senão, usa
    // `username`. Vamos cobrir os 3 casos.

    @Test
    fun displayName_quandoTemDisplayname_retornaDisplayname() {
        // Arrange
        val jogador = TeamPlayerDomain(
            userId = "u1",
            role = null,
            username = "joao_silva",
            displayname = "João Silva",
            avatarUrl = null
        )

        // Act
        val resultado = jogador.displayName

        // Assert
        assertEquals("João Silva", resultado)
    }

    @Test
    fun displayName_quandoDisplaynameVazio_caiNoUsername() {
        val jogador = TeamPlayerDomain(
            userId = "u1",
            role = null,
            username = "joao_silva",
            displayname = "   ", // só espaços → considerado em branco
            avatarUrl = null
        )

        assertEquals("joao_silva", jogador.displayName)
    }

    @Test
    fun displayName_quandoDisplaynameNulo_caiNoUsername() {
        val jogador = TeamPlayerDomain(
            userId = "u1",
            role = null,
            username = "joao_silva",
            displayname = null,
            avatarUrl = null
        )

        assertEquals("joao_silva", jogador.displayName)
    }

    // ── initials ───────────────────────────────────────────────────────────
    // Regra:
    //  - 2+ palavras → primeira letra da primeira + primeira da última
    //  - 1 palavra   → 2 primeiras letras
    //  - vazio       → "?"
    //  - sempre em maiúsculas

    @Test
    fun initials_nomeComDuasPalavras_retornaPrimeiraEUltima() {
        val jogador = jogadorComNome("João Silva")
        assertEquals("JS", jogador.initials)
    }

    @Test
    fun initials_nomeComTresPalavras_pegaPrimeiraEUltima() {
        val jogador = jogadorComNome("João da Silva")
        // Primeira palavra: "João" → J. Última palavra: "Silva" → S.
        assertEquals("JS", jogador.initials)
    }

    @Test
    fun initials_nomeComUmaPalavra_pegaDuasPrimeirasLetras() {
        val jogador = jogadorComNome("jose")
        assertEquals("JO", jogador.initials)
    }

    @Test
    fun initials_quandoNaoTemNomeDeExibicao_retornaInterrogacao() {
        // Username em branco + displayname nulo → displayName vira "".
        // Aí `parts` fica vazio e cai no ramo "?".
        val jogador = TeamPlayerDomain(
            userId = "u1",
            role = null,
            username = "",
            displayname = null,
            avatarUrl = null
        )
        assertEquals("?", jogador.initials)
    }

    @Test
    fun initials_sempreRetornaEmMaiusculas() {
        val jogador = jogadorComNome("maria souza")
        assertEquals("MS", jogador.initials)
    }

    // ── Helper ─────────────────────────────────────────────────────────────
    // Quando vários testes precisam montar o mesmo objeto com pequenas
    // variações, extraia um helper. Mantém o teste focado no que importa.
    private fun jogadorComNome(displayname: String) = TeamPlayerDomain(
        userId = "u1",
        role = null,
        username = "fallback",
        displayname = displayname,
        avatarUrl = null
    )
}
