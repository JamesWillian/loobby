package app.loobby.feature.games.domain.repository

import app.loobby.feature.games.domain.model.GameDomain

interface GamesRepository {

    /**
     * Busca no catálogo. Exige rede (não há catálogo local e a lista de busca
     * não é persistida). Lança OfflineException se estiver offline.
     */
    suspend fun search(query: String, page: Int = 1): List<GameDomain>

    /**
     * Detalhe de um jogo com read-through:
     *  - online: busca no backend, atualiza o cache local e retorna o fresco;
     *    em falha de rede, cai no cache local.
     *  - offline: retorna o cache local (ou null se o jogo nunca foi salvo).
     */
    suspend fun getGame(id: String): GameDomain?

    /**
     * Persiste o jogo escolhido para um evento (chamado pelo fluxo de criação/
     * edição de evento de gameplay). Guardamos apenas o jogo usado, não a busca.
     */
    suspend fun saveGame(game: GameDomain)

    /** Leitura síncrona só do cache local (sem rede). */
    fun cachedGame(id: String): GameDomain?
}
