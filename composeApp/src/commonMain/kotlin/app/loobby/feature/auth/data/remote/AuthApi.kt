package app.loobby.feature.auth.data.remote

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.LoginRequest
import app.loobby.feature.auth.data.model.RefreshTokenRequest
import app.loobby.feature.auth.data.model.RegisterRequest
import app.loobby.feature.auth.data.model.UpdateProfileRequest
import app.loobby.feature.auth.data.model.UserProfileResponse

/**
 * API de autenticação — espelha o AuthController do backend.
 *
 * Endpoints:
 *   POST /auth/anonymous  → público, sem body                 → AuthResponse
 *   POST /auth/login      → público, body LoginRequest        → AuthResponse
 *   POST /auth/register   → AUTENTICADO (Bearer do anônimo)   → AuthResponse
 *   POST /auth/refresh    → público, body RefreshTokenRequest → AuthResponse
 *   POST /auth/resend-verification  → AUTENTICADO             → Unit
 */
interface AuthApi {

    suspend fun anonymous(): AuthResponse

    suspend fun register(request: RegisterRequest): AuthResponse

    suspend fun login(request: LoginRequest): AuthResponse

    suspend fun refresh(request: RefreshTokenRequest): AuthResponse

    suspend fun getProfile(): UserProfileResponse

    suspend fun updateProfile(
        request: UpdateProfileRequest
    ): UserProfileResponse

    suspend fun uploadAvatar(
        fileName: String,
        bytes: ByteArray,
        contentType: String
    ): UserProfileResponse

    suspend fun resendVerification()

    suspend fun forgotPassword(email: String)
}