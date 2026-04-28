package app.loobby.feature.auth.domain.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.NetworkConfig.BASE_URL
import app.loobby.core.network.OfflineException
import app.loobby.core.storage.StoredTokens
import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.ChangePasswordRequest
import app.loobby.feature.auth.data.model.DeleteAccountRequest
import app.loobby.feature.auth.data.model.GoogleAuthRequest
import app.loobby.feature.auth.data.model.LoginRequest
import app.loobby.feature.auth.data.model.RefreshTokenRequest
import app.loobby.feature.auth.data.model.RegisterRequest
import app.loobby.feature.auth.data.model.UpdateProfileRequest
import app.loobby.feature.auth.data.model.UpdateUserProfileRequest
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.auth.data.remote.UserApi
import app.loobby.feature.auth.domain.model.AuthSession
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val userApi: UserApi,
    private val tokenStorage: TokenStorage,
    private val connectivity: ConnectivityObserver,
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
        val tokens = response.toTokens()
        tokenStorage.saveTokens(tokens)

        return AuthSession(
            userId = tokens.userId,
            username = tokens.username,
            roles = tokens.roles,
            isAnonymous = true
        )
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        requireOnline()
        val response = api.login(LoginRequest(email, password))
        saveResponseAsTokens(response)
        return response
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResponse {
        requireOnline()
        val response = api.loginWithGoogle(GoogleAuthRequest(idToken))
        saveResponseAsTokens(response)
        return response
    }

    override suspend fun register(email: String, password: String): AuthResponse {
        requireOnline()
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

    override suspend fun saveAnonymousToken(anonymousToken: String) {
        tokenStorage.saveAnonymousToken(anonymousToken)
    }

    override suspend fun getSavedAnonymousToken(): String? =
        tokenStorage.getAnonymousToken()

    override suspend fun clearSavedAnonymousToken() {
        tokenStorage.clearAnonymousToken()
    }

    override suspend fun logout() {
        tokenStorage.clearTokens()
    }

    override suspend fun refreshIfPossible(): AuthSession? {
        val current = tokenStorage.getTokens() ?: return null
        val response = api.refresh(RefreshTokenRequest(refreshToken = current.refreshToken))
        val tokens = response.toTokens()
        tokenStorage.saveTokens(tokens)

        return AuthSession(
            userId = tokens.userId,
            username = tokens.username,
            roles = tokens.roles,
            isAnonymous = tokens.isAnonymous
        )
    }

    override suspend fun recoverAnonymous(anonymousToken: String): AuthSession {
        val response = api.refresh(RefreshTokenRequest(anonymousToken))
        val tokens = response.toTokens()
        tokenStorage.saveTokens(tokens)

        return AuthSession(
            userId = tokens.userId,
            username = tokens.username,
            roles = tokens.roles,
            isAnonymous = tokens.isAnonymous
        )
    }

    // ─── Email verification ─────────────────────────

    override suspend fun resendVerification() {
        requireOnline()
        api.resendVerification()
    }

    // ─── Recovery password ─────────────────────────

    override suspend fun forgotPassword(email: String) {
        requireOnline()
        api.forgotPassword(email)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        requireOnline()
        userApi.changePassword(
            ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
        )
    }

    // ─── Profile ────────────────────────────────────

    override suspend fun getProfile(): UserMeResponse {
        val user = userApi.getMe()
        return user.copy(
            avatarUrl = user.avatarUrl
        )
    }

    override suspend fun updateProfile(username: String?, displayname: String?): UserProfileResponse {
        requireOnline()
        return userApi.updateProfile(
            UpdateUserProfileRequest(
                username = username,
                displayname = displayname
            )
        )
    }

    override suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): UserProfileResponse {
        requireOnline()
        return userApi.uploadAvatar(imageBytes, fileName)
    }

    override suspend fun deleteAccount(password: String) {
        requireOnline()
        userApi.deleteAccount(DeleteAccountRequest(password = password))
    }

    // ---------- Helpers ----------

    private fun AuthResponse.toTokens(): StoredTokens =
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

    /**
     * Guarda de escrita — simétrica aos demais repositories. Fluxos de inicialização
     * (initializeAnonymousIfNeeded, refreshIfPossible, recoverAnonymous) e o logout
     * local (clearTokens) não passam por aqui: precisam rodar em boot mesmo offline
     * para que o app se apoie no cache existente ou no token salvo.
     */
    private fun requireOnline() {
        if (!connectivity.isOnlineNow()) throw OfflineException()
    }
}