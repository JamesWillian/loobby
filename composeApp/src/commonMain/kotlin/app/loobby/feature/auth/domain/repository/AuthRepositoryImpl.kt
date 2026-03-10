package app.loobby.feature.auth.domain.repository

import app.loobby.core.storage.StoredTokens
import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.LoginRequest
import app.loobby.feature.auth.data.model.RefreshTokenRequest
import app.loobby.feature.auth.data.model.RegisterRequest
import app.loobby.feature.auth.data.model.UpdateProfileRequest
import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.auth.domain.model.AuthSession
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override val storedTokensFlow: Flow<StoredTokens?> = tokenStorage.observeTokens()

    override val sessionFlow: Flow<AuthSession?> =
        storedTokensFlow.map { tokens ->
            tokens?.let {
                AuthSession(
                    userId = it.userId,
                    username = it.username,
                    roles = it.roles,
                    isAnonymous = it.isAnonymous
                )
            }
        }

    override suspend fun initializeAnonymousIfNeeded(): AuthSession {
        val current = tokenStorage.getTokens()
        if (current != null) {
            return AuthSession(
                userId = current.userId,
                username = current.username,
                roles = current.roles,
                isAnonymous = current.isAnonymous
            )
        }

        val response = api.anonymous()
        val tokens = response.toTokens(isAnonymous = true)
        tokenStorage.saveTokens(tokens)

        return AuthSession(
            userId = tokens.userId,
            username = tokens.username,
            roles = tokens.roles,
            isAnonymous = true
        )
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        val response = api.login(LoginRequest(email, password))
        saveResponseAsTokens(response)
        return response

//        return AuthSession(
//            userId = tokens.userId,
//            username = tokens.username,
//            roles = tokens.roles,
//            isAnonymous = false
//        )
    }

    override suspend fun register(email: String, password: String): AuthResponse {
        val response = api.register(RegisterRequest(email = email, password = password))
        saveResponseAsTokens(response)
        return response
    }

    override suspend fun isAnonymous(): Boolean {
        val tokens = tokenStorage.getTokens() ?: return true
        return tokens.isAnonymous
    }

    override suspend fun currentUserId(): String? =
        tokenStorage.getTokens()?.userId

    override suspend fun saveAnonymousId(anonymousUserId: String) {
        tokenStorage.saveAnonymousId(anonymousUserId)
    }

    override suspend fun getSavedAnonymousId(): String? =
        tokenStorage.getAnonymousId()

//    override suspend fun register(email: String, password: String): AuthSession {
//
//        val response =
//            callWithRefresh { accessToken ->
//                api.register(
//                    accessToken = accessToken,
//                    request = RegisterRequest(email, password)
//                )
//            }
//
//        val tokens = response.toTokens(isAnonymous = false)
//        tokenStorage.saveTokens(tokens)
//
//        return AuthSession(
//            userId = tokens.userId,
//            username = tokens.username,
//            roles = tokens.roles,
//            isAnonymous = false
//        )
//    }

    override suspend fun logout() {
        tokenStorage.clearTokens()
    }

    override suspend fun refreshIfPossible(): AuthSession? {
        val current = tokenStorage.getTokens() ?: return null
        val response = api.refresh(RefreshTokenRequest(refreshToken = current.refreshToken))
        val tokens = response.toTokens(isAnonymous = current.isAnonymous)
        tokenStorage.saveTokens(tokens)

        return AuthSession(
            userId = tokens.userId,
            username = tokens.username,
            roles = tokens.roles,
            isAnonymous = tokens.isAnonymous
        )
    }

    override suspend fun getProfile(): UserProfileResponse =
        api.getProfile()

    override suspend fun updateProfile(
        username: String?,
        displayname: String?
    ): UserProfileResponse =
        api.updateProfile(
            UpdateProfileRequest(username = username, displayname = displayname)
        )


    override suspend fun uploadAvatar(
        fileName: String,
        bytes: ByteArray,
        contentType: String
    ): UserProfileResponse =
        api.uploadAvatar(
            fileName = fileName,
            bytes = bytes,
            contentType = contentType
        )

    // ---------- Helpers ----------

    private fun AuthResponse.toTokens(isAnonymous: Boolean): StoredTokens =
        StoredTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            username = username,
            roles = roles
        )

    private suspend fun saveResponseAsTokens(response: AuthResponse) {
        val tokens = StoredTokens(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.userId,
            username = response.username,
            roles = response.roles
        )
        tokenStorage.saveTokens(tokens)
    }
}