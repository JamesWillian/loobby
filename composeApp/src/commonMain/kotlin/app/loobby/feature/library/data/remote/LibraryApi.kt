package app.loobby.feature.library.data.remote

import app.loobby.feature.library.data.model.GroupLibraryGameResponse
import app.loobby.feature.library.data.model.LibraryGameResponse

/** As 6 rotas de biblioteca da /v2. [gameId] é sempre o id do catálogo (RAWG). */
interface LibraryApi {
    suspend fun myLibrary(): List<LibraryGameResponse>
    suspend fun saveMine(gameId: String, ownedPlatforms: List<String>): LibraryGameResponse
    suspend fun removeMine(gameId: String)
    suspend fun groupLibrary(groupId: String): List<GroupLibraryGameResponse>
    suspend fun addToGroup(groupId: String, gameId: String): GroupLibraryGameResponse
    suspend fun removeFromGroup(groupId: String, gameId: String)
}
