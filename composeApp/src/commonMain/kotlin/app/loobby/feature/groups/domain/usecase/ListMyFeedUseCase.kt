package app.loobby.feature.groups.domain.usecase

import app.loobby.feature.groups.domain.model.UserFeedDomain
import app.loobby.feature.groups.domain.repository.UserFeedRepository

class ListMyFeedUseCase(
    private val repository: UserFeedRepository
) {
    suspend operator fun invoke(userId: String): List<UserFeedDomain> {
        return repository.getUserFeed(userId)
    }
}