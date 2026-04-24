package app.loobby.feature.events.data.cache

import app.loobby.core.db.CacheJson
import app.loobby.db.EventEntity
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.GameplayDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.model.SportDomain
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

/**
 * DTOs espelho com `@Serializable` só para o cache. Não vazam para o domínio
 * nem para a camada remote — vivem dentro deste arquivo para que qualquer
 * evolução do domínio fique isolada aqui.
 */
@Serializable
private data class CachedGameplay(
    val gameId: String?,
    val gameName: String
) {
    fun toDomain() = GameplayDomain(gameId = gameId, gameName = gameName)

    companion object {
        fun from(g: GameplayDomain) = CachedGameplay(gameId = g.gameId, gameName = g.gameName)
    }
}

@Serializable
private data class CachedSport(
    val durationMinutes: Int,
    val arena: String?,
    val pricePerPlayer: Double,
    val maxPlayers: Int?,
    val acceptReserve: Boolean
) {
    fun toDomain() = SportDomain(
        durationMinutes = durationMinutes,
        arena = arena,
        pricePerPlayer = pricePerPlayer,
        maxPlayers = maxPlayers,
        acceptReserve = acceptReserve
    )

    companion object {
        fun from(s: SportDomain) = CachedSport(
            durationMinutes = s.durationMinutes,
            arena = s.arena,
            pricePerPlayer = s.pricePerPlayer,
            maxPlayers = s.maxPlayers,
            acceptReserve = s.acceptReserve
        )
    }
}

private val avatarListSerializer = ListSerializer(String.serializer().nullable)

/** entity → domain */
fun EventEntity.toDomain(): EventDomain {
    val gameplay = gameplay_json?.let {
        runCatching { CacheJson.decodeFromString(CachedGameplay.serializer(), it).toDomain() }
            .getOrNull()
    }
    val sport = sport_json?.let {
        runCatching { CacheJson.decodeFromString(CachedSport.serializer(), it).toDomain() }
            .getOrNull()
    }
    val avatars = confirmed_avatars_json?.let {
        runCatching { CacheJson.decodeFromString(avatarListSerializer, it) }.getOrNull()
    }
    return EventDomain(
        id = id,
        eventType = runCatching { EventType.valueOf(event_type) }.getOrDefault(EventType.SPORT),
        groupId = group_id,
        isInstant = is_instant == 1L,
        ownerId = owner_id,
        scheduledDatetime = scheduled_datetime,
        name = name,
        description = description,
        inviteCode = invite_code,
        createdAt = created_at,
        gameplay = gameplay,
        sport = sport,
        rsvpStatus = rsvp_status?.let { runCatching { RsvpStatus.valueOf(it) }.getOrNull() },
        confirmedCount = confirmed_count.toInt(),
        confirmedAvatars = avatars
    )
}

/** Helpers usados pelo repository para montar os parâmetros do `upsert`. */
fun EventDomain.gameplayJson(): String? = gameplay?.let {
    CacheJson.encodeToString(CachedGameplay.serializer(), CachedGameplay.from(it))
}

fun EventDomain.sportJson(): String? = sport?.let {
    CacheJson.encodeToString(CachedSport.serializer(), CachedSport.from(it))
}

fun EventDomain.confirmedAvatarsJson(): String? = confirmedAvatars?.let {
    CacheJson.encodeToString(avatarListSerializer, it)
}
