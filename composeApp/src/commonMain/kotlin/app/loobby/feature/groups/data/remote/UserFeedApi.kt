package app.loobby.feature.groups.data.remote

import app.loobby.feature.groups.data.model.UserFeedResponse

interface UserFeedApi {

    suspend fun getUserFeed(userId: String): List<UserFeedResponse>
}