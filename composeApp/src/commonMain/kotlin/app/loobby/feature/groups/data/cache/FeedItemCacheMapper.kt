package app.loobby.feature.groups.data.cache

import app.loobby.db.FeedItemEntity
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.domain.model.UserFeedDomain

/** Converte a linha cacheada para o tipo de domínio. */
fun FeedItemEntity.toDomain(): UserFeedDomain = UserFeedDomain(
    id = id,
    userId = user_id,
    name = name,
    imageUrl = image_url,
    entryType = runCatching { FeedType.valueOf(entry_type) }.getOrDefault(FeedType.GROUP),
    isFinished = is_finished == 1L
)
