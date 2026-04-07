package app.loobby.feature.groups.domain.model

data class UserFeedDomain(
    val id: String,
    val userId: String,
    val name: String,
    val imageUrl: String? = null,
    val entryType: FeedType,
    val isFinished: Boolean
)
