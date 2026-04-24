package app.loobby.feature.groups.data.cache

import app.loobby.db.GroupEntity
import app.loobby.feature.groups.domain.model.GroupDomain

fun GroupEntity.toDomain(): GroupDomain = GroupDomain(
    id = id,
    name = name,
    inviteCode = invite_code,
    imageUrl = image_url,
    ownerId = owner_id,
    createdAt = created_at
)
