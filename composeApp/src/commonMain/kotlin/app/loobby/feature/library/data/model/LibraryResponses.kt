package app.loobby.feature.library.data.model

import app.loobby.feature.gameplays.data.model.UserRefResponse
import kotlinx.serialization.Serializable

// As respostas das rotas de biblioteca da /v2, como a API manda. Ver "Rotas da 2.0:
// bibliotecas" no README do loobby-api.

/** Um jogo da biblioteca da pessoa. */
@Serializable
data class LibraryGameResponse(
    val gameId: String,
    val name: String,
    val coverUrl: String? = null,
    val availablePlatforms: List<String> = emptyList(),
    val ownedPlatforms: List<String> = emptyList(),
    val addedAt: String,
    val updatedAt: String? = null,
)

/** Um jogo da biblioteca do grupo. */
@Serializable
data class GroupLibraryGameResponse(
    val gameId: String,
    val name: String,
    val coverUrl: String? = null,
    val availablePlatforms: List<String> = emptyList(),
    val addedBy: UserRefResponse? = null,
    val addedAt: String,
)

/** O corpo do PUT /v2/me/library/{gameId}. */
@Serializable
data class OwnedPlatformsRequest(val ownedPlatforms: List<String>)
