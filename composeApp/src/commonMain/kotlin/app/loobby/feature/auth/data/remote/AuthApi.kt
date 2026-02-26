package app.loobby.feature.auth.data.remote

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.LoginRequest
import app.loobby.feature.auth.data.model.RegisterRequest
import app.loobby.feature.auth.data.model.UpdateProfileRequest
import app.loobby.feature.auth.data.model.UserProfileResponse

interface AuthApi {

    suspend fun anonymous(): AuthResponse

    suspend fun register(
        accessToken: String,
        request: RegisterRequest
    ): AuthResponse

    suspend fun login(request: LoginRequest): AuthResponse

    suspend fun refresh(refreshToken: String): AuthResponse

    suspend fun getProfile(accessToken: String): UserProfileResponse

    suspend fun updateProfile(
        accessToken: String,
        request: UpdateProfileRequest
    ): UserProfileResponse

    suspend fun uploadAvatar(
        accessToken: String,
        fileName: String,
        bytes: ByteArray,
        contentType: String
    ): UserProfileResponse
}