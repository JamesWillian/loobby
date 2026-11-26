package app.loobby.feature.library.domain.repository

import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.library.domain.model.GroupLibraryGame
import app.loobby.feature.library.domain.model.LibraryGame

/**
 * As bibliotecas de jogos da 2.0 (/v2/me/library e /v2/groups/{id}/library). As
 * leituras caem no cache quando não há rede; as escritas exigem rede e lançam
 * OfflineException sem ela. As listas saem pelo nome do jogo.
 */
interface LibraryRepository {

    suspend fun myLibrary(): List<LibraryGame>

    /** Adiciona o jogo à biblioteca, ou troca o "tenho o jogo" ([owned] vazio: só na biblioteca). */
    suspend fun saveToMyLibrary(catalogId: String, owned: Set<Platform>): LibraryGame

    suspend fun removeFromMyLibrary(catalogId: String)

    suspend fun groupLibrary(groupId: String): List<GroupLibraryGame>

    /** Repetir não muda nada: o jogo continua com quem o adicionou primeiro. */
    suspend fun addToGroupLibrary(groupId: String, catalogId: String): GroupLibraryGame

    suspend fun removeFromGroupLibrary(groupId: String, catalogId: String)
}
