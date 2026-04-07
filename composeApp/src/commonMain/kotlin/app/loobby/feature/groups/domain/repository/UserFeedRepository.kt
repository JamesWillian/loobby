package app.loobby.feature.groups.domain.repository

import app.loobby.feature.groups.domain.model.UserFeedDomain

interface UserFeedRepository {
    suspend fun getUserFeed(userId: String): List<UserFeedDomain>
}