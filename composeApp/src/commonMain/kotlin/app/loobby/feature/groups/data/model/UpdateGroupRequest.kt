package app.loobby.feature.groups.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGroupRequest(
    val name: String
)