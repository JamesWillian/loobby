package app.loobby.feature.groups.data.repository

import app.loobby.feature.groups.data.mapper.toDomain
import app.loobby.feature.groups.data.remote.UserFeedApi
import app.loobby.feature.groups.domain.model.UserFeedDomain
import app.loobby.feature.groups.domain.repository.UserFeedRepository

class UserFeedRepositoryImpl(
    private val api: UserFeedApi
): UserFeedRepository {
    override suspend fun getUserFeed(userId: String): List<UserFeedDomain> {
        return api.getUserFeed(userId).map {
            it.toDomain()
        }
    }
}