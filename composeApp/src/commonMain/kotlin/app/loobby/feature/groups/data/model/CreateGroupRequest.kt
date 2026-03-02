package app.loobby.feature.groups.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val imageUrl: String? = null
)