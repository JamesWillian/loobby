package app.loobby.core.storage

import kotlinx.coroutines.flow.Flow

interface TokenStorage {

    fun getTokens(): StoredTokens?

    suspend fun saveTokens(tokens: StoredTokens)

    suspend fun clearTokens()

    suspend fun saveAnonymousId(anonymousUserId: String)

    fun observeTokens(): Flow<StoredTokens?>

    suspend fun getAnonymousId(): String?
}