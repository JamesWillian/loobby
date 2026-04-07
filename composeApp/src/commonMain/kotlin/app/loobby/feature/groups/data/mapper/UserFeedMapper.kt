package app.loobby.feature.groups.data.mapper

import app.loobby.feature.groups.data.model.UserFeedResponse
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.domain.model.UserFeedDomain

fun UserFeedResponse.toDomain(): UserFeedDomain {
    return UserFeedDomain(
        id = id,
        userId = userId,
        name = name,
        imageUrl = imageUrl,
        entryType = when (entryType) {
            "GROUP" -> FeedType.GROUP
            "EVENT" -> FeedType.EVENT
            else -> FeedType.GROUP
        },
        isFinished = isFinished
    )
}
