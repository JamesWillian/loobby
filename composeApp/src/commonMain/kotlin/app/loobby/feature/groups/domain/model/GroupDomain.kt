package app.loobby.feature.groups.domain.model

data class GroupDomain(
    val id: String,
    val name: String,
    val inviteCode: String,
    val imageUrl: String? = null,
    val ownerId: String,
    val createdAt: String? = null
)
