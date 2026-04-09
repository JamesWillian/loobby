package app.loobby.feature.auth.data.remote

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.LoginRequest
import app.loobby.feature.auth.data.model.RefreshRequest
import app.loobby.feature.auth.data.model.RefreshTokenRequest
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
    private val client: HttpClient
) : AuthApi {

    override suspend fun anonymous(): AuthResponse =
        client.post("/auth/anonymous") {
            contentType(ContentType.Application.Json)
        }.body()

    override suspend fun register(request: RegisterRequest): AuthResponse =
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun login(request: LoginRequest): AuthResponse =
        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun refresh(request: RefreshTokenRequest): AuthResponse =
        client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun getProfile(): UserProfileResponse =
        client.get("/users/me") {
            contentType(ContentType.Application.Json)
        }.body()

    override suspend fun updateProfile(
        request: UpdateProfileRequest
    ): UserProfileResponse =
        client.patch("/users/me") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun uploadAvatar(
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
                contentType(ContentType.Application.Json)
            }.body()
        }

    override suspend fun resendVerification() {
        client.post("/auth/resend-verification") {
            contentType(ContentType.Application.Json)
        }
    }
}