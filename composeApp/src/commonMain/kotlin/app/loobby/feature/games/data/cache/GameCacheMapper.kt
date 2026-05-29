package app.loobby.feature.games.data.cache

import app.loobby.db.GameEntity
import app.loobby.feature.games.domain.model.GameDomain

/** Linha do SQLDelight (GameEntity) -> domínio. */
fun GameEntity.toDomain(): GameDomain = GameDomain(
    id = id,
    slug = slug,
    name = name,
    backgroundImage = background_image,
    released = released,
    rating = rating,
    metacritic = metacritic?.toInt(),
)
