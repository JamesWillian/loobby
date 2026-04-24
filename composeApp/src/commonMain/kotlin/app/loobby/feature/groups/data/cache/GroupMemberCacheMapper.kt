package app.loobby.feature.groups.data.cache

import app.loobby.db.GroupMemberEntity
import app.loobby.feature.groups.data.model.GroupMemberResponse

fun GroupMemberEntity.toResponse(): GroupMemberResponse = GroupMemberResponse(
    userId = user_id,
    username = username,
    displayname = displayname,
    avatarUrl = avatar_url,
    isOwner = is_owner == 1L
)
