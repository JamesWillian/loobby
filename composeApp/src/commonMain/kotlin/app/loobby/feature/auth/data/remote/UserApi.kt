package app.loobby.feature.auth.data.remote

import app.loobby.feature.auth.data.model.ChangePasswordRequest
import app.loobby.feature.auth.data.model.UpdateUserProfileRequest
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.data.model.UserProfileResponse

/**
 * API de perfil de usuário — espelha o UsersController do backend.
 *
 * Endpoints (todos autenticados):
 *   GET    /users/me           → UserMeResponse
 *   PATCH  /users/me           → UserProfileResponse
 *   POST   /users/me/avatar    → UserProfileResponse (multipart)
 */
interface UserApi {

    /** Busca dados completos do usuário logado. */
    suspend fun getMe(): UserMeResponse

    /** Atualiza username e/ou displayname. */
    suspend fun updateProfile(request: UpdateUserProfileRequest): UserProfileResponse

    /** Upload de avatar. Recebe os bytes da imagem e o nome do arquivo. */
    suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): UserProfileResponse

    /** Altera a senha do usuário logado. */
    suspend fun changePassword(request: ChangePasswordRequest)
}