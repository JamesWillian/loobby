package app.loobby.feature.groups.data.mapper

import app.loobby.core.network.NetworkConfig.BASE_URL
import app.loobby.feature.groups.data.model.GroupResponse
import app.loobby.feature.groups.domain.model.GroupDomain

fun GroupResponse.toDomain() : GroupDomain {
    return GroupDomain(
        id = id,
        name = name,
        inviteCode = inviteCode,
        imageUrl = imageUrl?.let
        {
            if (it.startsWith('/')) "$BASE_URL$it"
            else it
        },
        ownerId = ownerId,
        createdAt = createdAt
    )
}