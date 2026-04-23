package app.loobby.feature.notifications.data.remote

import app.loobby.feature.notifications.data.model.RegisterDeviceRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class DeviceTokenApiImpl(
    private val client: HttpClient
) : DeviceTokenApi {

    override suspend fun register(request: RegisterDeviceRequest) {
        client.post("/devices/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun unregister(token: String) {
        client.delete("/devices/$token")
    }
}
