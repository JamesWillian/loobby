package app.loobby.feature.auth.data.remote

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.LoginRequest
import app.loobby.feature.auth.data.model.RefreshRequest
import app.loobby.feature.auth.data.model.RegisterRequest
import app.loobby.feature.auth.data.model.UpdateProfileRequest
import app.loobby.feature.auth.data.model.UserProfileResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthApiImpl(
    // usa o baseClient (sem Authorization automático)
    private val client: HttpClient
) : AuthApi {

    override suspend fun anonymous(): AuthResponse =
        client.post("/auth/anonymous").body()

    override suspend fun register(
        accessToken: String,
        request: RegisterRequest
    ): AuthResponse =
        client.post("/auth/register") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }.body()

    override suspend fun login(request: LoginRequest): AuthResponse =
        client.post("/auth/login") {
            setBody(request)
        }.body()

    override suspend fun refresh(refreshToken: String): AuthResponse =
        client.post("/auth/refresh") {
            setBody(RefreshRequest(refreshToken))
        }.body()

    override suspend fun getProfile(accessToken: String): UserProfileResponse =
        client.get("/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()

    override suspend fun updateProfile(
        accessToken: String,
        request: UpdateProfileRequest
    ): UserProfileResponse =
        client.patch("/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }.body()

    override suspend fun uploadAvatar(
        accessToken: String,
        fileName: String,
        bytes: ByteArray,
        contentType: String
    ): UserProfileResponse =
        withContext(Dispatchers.Default) {
            client.submitFormWithBinaryData(
                url = "/users/me/avatar",
                formData = formData {
                    append(
                        key = "file",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=$fileName")
                            append(HttpHeaders.ContentType, contentType)
                        }
                    )
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.body()
        }
}