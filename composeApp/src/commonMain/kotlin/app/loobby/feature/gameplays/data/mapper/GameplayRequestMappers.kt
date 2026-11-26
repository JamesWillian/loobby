package app.loobby.feature.gameplays.data.mapper

import app.loobby.feature.gameplays.data.model.CreateGameplayRequest
import app.loobby.feature.gameplays.data.model.GameChoiceRequest
import app.loobby.feature.gameplays.data.model.VotingConfigRequest
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.GameplayChanges
import app.loobby.feature.gameplays.domain.model.GameplayDraft
import app.loobby.feature.gameplays.domain.model.GameplayMode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

// Domínio → corpo das rotas /v2.

fun GameplayDraft.toRequest(): CreateGameplayRequest = CreateGameplayRequest(
    name = name,
    description = description,
    mode = mode.id,
    platforms = listOf(platform.id),
    scheduledAt = scheduledAt.toString(),
    game = game?.toRequest()?.takeIf { mode == GameplayMode.FIXED },
    voting = voting
        ?.takeIf { mode == GameplayMode.VOTING }
        ?.let { VotingConfigRequest(finalists = it.finalists, multiVote = it.multiVote) },
    games = games.map { it.toRequest() }.takeIf { mode == GameplayMode.VOTING && it.isNotEmpty() },
)

fun GameChoice.toRequest(): GameChoiceRequest = GameChoiceRequest(gameId = catalogId, name = name)

/**
 * O PATCH com só o que mudou. Na API, campo ausente não muda e null apaga (ou é
 * recusado), então nada aqui sai como null: a descrição vazia é que apaga.
 */
fun GameplayChanges.toPatchJson(): JsonObject = buildJsonObject {
    name?.let { put("name", it) }
    description?.let { put("description", it) }
    scheduledAt?.let { put("scheduledAt", it.toString()) }
    platform?.let { putJsonArray("platforms") { add(it.id) } }
    game?.let { choice ->
        putJsonObject("game") {
            choice.catalogId?.let { put("gameId", it) }
            put("name", choice.name)
        }
    }
    voting?.let { config ->
        putJsonObject("voting") {
            put("finalists", config.finalists)
            put("multiVote", config.multiVote)
        }
    }
}
