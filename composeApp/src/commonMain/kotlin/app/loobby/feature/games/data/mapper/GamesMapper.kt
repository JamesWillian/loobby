package app.loobby.feature.games.data.mapper

import app.loobby.feature.games.data.model.GameDetailsResponse
import app.loobby.feature.games.data.model.GameSummaryResponse
import app.loobby.feature.games.domain.model.GameDomain

/** DTOs da API -> domínio. */

fun GameSummaryResponse.toDomain(): GameDomain = GameDomain(
    id = id,
    slug = slug,
    name = name,
    backgroundImage = backgroundImage,
    released = released,
    rating = rating,
    metacritic = metacritic,
)

fun GameDetailsResponse.toDomain(): GameDomain = GameDomain(
    id = id,
    slug = slug,
    name = name,
    backgroundImage = backgroundImage,
    released = released,
    rating = rating,
    metacritic = metacritic,
)
