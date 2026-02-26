package app.loobby.feature.auth.domain.repository

import app.loobby.core.storage.AuthTokens
import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.LoginRequest
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

    override val authTokensFlow: Flow<AuthTokens?> = tokenStorage.observeTokens()

    override val sessionFlow: Flow<AuthSession?> =
        authTokensFlow.map { tokens ->
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

    override suspend fun login(email: String, password: String): AuthSession {
        val response = api.login(LoginRequest(email, password))
        val tokens = response.toTokens(isAnonymous = false)
        tokenStorage.saveTokens(tokens)

        return AuthSession(
            userId = tokens.userId,
            username = tokens.username,
            roles = tokens.roles,
            isAnonymous = false
        )
    }

    override suspend fun register(email: String, password: String): AuthSession {

        val response =
            callWithRefresh { accessToken ->
                api.register(
                    accessToken = accessToken,
                    request = RegisterRequest(email, password)
                )
            }

        val tokens = response.toTokens(isAnonymous = false)
        tokenStorage.saveTokens(tokens)

        return AuthSession(
            userId = tokens.userId,
            username = tokens.username,
            roles = tokens.roles,
            isAnonymous = false
        )
    }

    override suspend fun logout() {
        tokenStorage.clearTokens()
    }

    override suspend fun refreshIfPossible(): AuthSession? {
        val current = tokenStorage.getTokens() ?: return null
        val response = api.refresh(current.refreshToken)
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
        callWithRefresh { accessToken ->
            api.getProfile(accessToken)
        }

    override suspend fun updateProfile(
        username: String?,
        displayname: String?
    ): UserProfileResponse =
        callWithRefresh { accessToken ->
            api.updateProfile(
                accessToken,
                UpdateProfileRequest(username = username, displayname = displayname)
            )
        }

    override suspend fun uploadAvatar(
        fileName: String,
        bytes: ByteArray,
        contentType: String
    ): UserProfileResponse =
        callWithRefresh { accessToken ->
            api.uploadAvatar(
                accessToken = accessToken,
                fileName = fileName,
                bytes = bytes,
                contentType = contentType
            )
        }

    // ---------- Helpers ----------

    private suspend fun <T> callWithRefresh(
        block: suspend (accessToken: String) -> T
    ): T {
        val current = tokenStorage.getTokens()
            ?: throw IllegalStateException("Usuário não autenticado")

        try {
            return block(current.accessToken)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                // tenta refresh
                val refreshedSession = refreshIfPossible()
                    ?: throw e

                val newTokens = tokenStorage.getTokens()
                    ?: throw e

                return block(newTokens.accessToken)
            } else {
                throw e
            }
        }
    }

    private fun AuthResponse.toTokens(isAnonymous: Boolean): AuthTokens =
        AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            username = username,
            roles = roles,
            isAnonymous = isAnonymous
        )
}