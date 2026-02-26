package app.loobby.core.storage

import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun clearTokens()
    fun getTokens(): AuthTokens?
    fun observeTokens(): Flow<AuthTokens?>
}