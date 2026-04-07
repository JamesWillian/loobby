package app.loobby.feature.groups.data.remote

import app.loobby.feature.groups.data.model.UserFeedResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class UserFeedApiImpl(
    private val client: HttpClient
) : UserFeedApi {

    override suspend fun getUserFeed(userId: String): List<UserFeedResponse> {
        return client.get("/users/feed").body()
    }
}