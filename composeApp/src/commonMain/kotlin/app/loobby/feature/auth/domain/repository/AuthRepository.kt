package app.loobby.feature.auth.domain.repository

import app.loobby.core.storage.AuthTokens
import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authTokensFlow: Flow<AuthTokens?>
    val sessionFlow: Flow<AuthSession?>

    suspend fun initializeAnonymousIfNeeded(): AuthSession

    suspend fun login(email: String, password: String): AuthSession

    suspend fun register(email: String, password: String): AuthSession

    suspend fun logout()

    suspend fun refreshIfPossible(): AuthSession?

    suspend fun getProfile(): UserProfileResponse

    suspend fun updateProfile(
        username: String?,
        displayname: String?
    ): UserProfileResponse

    suspend fun uploadAvatar(
        fileName: String,
        bytes: ByteArray,
        contentType: String
    ): UserProfileResponse
}