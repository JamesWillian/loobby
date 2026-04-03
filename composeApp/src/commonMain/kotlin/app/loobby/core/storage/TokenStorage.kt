package app.loobby.core.storage

import kotlinx.coroutines.flow.Flow

interface TokenStorage {

    fun getTokens(): StoredTokens?

    suspend fun saveTokens(tokens: StoredTokens)

    suspend fun clearTokens()

    fun observeTokens(): Flow<StoredTokens?>

    suspend fun saveAnonymousToken(anonymousToken: String)

    suspend fun getAnonymousToken(): String?
}