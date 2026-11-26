package app.loobby.feature.library.data.mapper

import app.loobby.feature.gameplays.domain.model.GameplayUser
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.library.data.model.GroupLibraryGameResponse
import app.loobby.feature.library.data.model.LibraryGameResponse
import app.loobby.feature.library.domain.model.GroupLibraryGame
import app.loobby.feature.library.domain.model.LibraryGame
import kotlin.time.Instant

// Resposta da API → domínio. Plataforma que o app não conhece é descartada.

fun LibraryGameResponse.toDomain(): LibraryGame = LibraryGame(
    catalogId = gameId,
    name = name,
    coverUrl = coverUrl,
    availablePlatforms = availablePlatforms.toPlatforms(),
    ownedPlatforms = ownedPlatforms.toPlatforms().toSet(),
    addedAt = Instant.parse(addedAt),
)

fun GroupLibraryGameResponse.toDomain(): GroupLibraryGame = GroupLibraryGame(
    catalogId = gameId,
    name = name,
    coverUrl = coverUrl,
    availablePlatforms = availablePlatforms.toPlatforms(),
    addedBy = addedBy?.let {
        GameplayUser(id = it.id, username = it.username, displayname = it.displayname, avatarUrl = it.avatarUrl)
    },
    addedAt = Instant.parse(addedAt),
)

/** O "tenho o jogo" como a API pede: na ordem das plataformas do app. */
fun Set<Platform>.toRequest(): List<String> = Platform.entries.filter { it in this }.map { it.id }

private fun List<String>.toPlatforms(): List<Platform> = mapNotNull(Platform::fromId)
