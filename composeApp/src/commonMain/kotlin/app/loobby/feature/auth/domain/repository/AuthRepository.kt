package app.loobby.feature.auth.domain.repository

import app.loobby.core.storage.StoredTokens
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val storedTokensFlow: Flow<StoredTokens?>
    val sessionFlow: Flow<AuthSession?>

    // ─── Auth ───────────────────────────────────────
    suspend fun initializeAnonymousIfNeeded(): AuthSession

    /** Login com credenciais. Retorna tokens + dados do usuário. */
    suspend fun login(email: String, password: String): AuthResponse

    /** Registro de novo usuário. */
    suspend fun register(email: String, password: String): AuthResponse

    /** Verifica se o usuário atual é anônimo (role ANONYMOUS). */
    suspend fun isAnonymous(): Boolean

    /** Retorna o userId salvo atualmente, ou null se não autenticado. */
    suspend fun currentUserId(): String?

    /** Salva o refreshToken do usuário anônimo antes de trocar para conta definitiva. */
    suspend fun saveAnonymousToken(anonymousToken: String)

    /** Recupera o refreshToken anônimo salvo (para eventual migração de dados). */
    suspend fun getSavedAnonymousToken(): String?

    /** Faz logout local (limpa tokens, mas preserva anonymousId salvo). */
    suspend fun logout()

    suspend fun refreshIfPossible(): AuthSession?

    suspend fun recoverAnonymous(anonymousToken: String): AuthSession

    // ─── Email verification ─────────────────────────
    suspend fun resendVerification()

    // ─── Profile ────────────────────────────────────
    suspend fun getProfile(): UserMeResponse
    suspend fun updateProfile(username: String?, displayname: String?): UserProfileResponse
    suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): UserProfileResponse
}