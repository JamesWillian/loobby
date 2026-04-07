package app.loobby.feature.groups.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserFeedResponse(
    val id: String,
    val userId: String,
    val name: String,
    val imageUrl: String? = null,
    val entryType: String,
    val isFinished: Boolean
)