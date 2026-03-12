package app.loobby.feature.auth.data.remote

import app.loobby.feature.auth.data.model.UpdateUserProfileRequest
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.data.model.UserProfileResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class UserApiImpl(
    private val client: HttpClient
) : UserApi {

    override suspend fun getMe(): UserMeResponse =
        client.get("/users/me").body()

    override suspend fun updateProfile(request: UpdateUserProfileRequest): UserProfileResponse =
        client.patch("/users/me") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): UserProfileResponse =
        client.submitFormWithBinaryData(
            url = "/users/me/avatar",
            formData = formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    append(HttpHeaders.ContentType, contentTypeForFile(fileName))
                })
            }
        ).body()

    private fun contentTypeForFile(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }
    }
}