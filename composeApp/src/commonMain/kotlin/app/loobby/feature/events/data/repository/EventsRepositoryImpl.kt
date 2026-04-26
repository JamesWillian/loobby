package app.loobby.feature.events.data.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.OfflineException
import app.loobby.db.LoobbyDatabase
import app.loobby.feature.events.data.cache.confirmedAvatarsJson
import app.loobby.feature.events.data.cache.gameplayJson
import app.loobby.feature.events.data.cache.sportJson
import app.loobby.feature.events.data.cache.toDomain
import app.loobby.feature.events.data.mapper.toDomain
import app.loobby.feature.events.data.mapper.toRequest
import app.loobby.feature.events.data.mapper.toUpdateRequest
import app.loobby.feature.events.data.model.RsvpRequest
import app.loobby.feature.events.data.remote.EventsApi
import app.loobby.feature.events.domain.model.CreateEventInput
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.model.UpdateEventInput
import app.loobby.feature.events.domain.repository.EventsRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Cache de eventos + RSVPs. Mesma lógica de leitura dos demais:
 *  - online: API → upsert cache → retorna fresco.
 *  - offline (ou API falhando e havendo cache): retorna do cache.
 *
 * Para listas de eventos por grupo e listas de RSVP por evento, a estratégia
 * é "substituição" — apaga e reinsere na ordem que veio da rede para manter
 * o `position` fiel ao backend. Para getById/getByInvite apenas atualiza.
 */
class EventsRepositoryImpl(
    private val api: EventsApi,
    private val db: LoobbyDatabase,
    private val connectivity: ConnectivityObserver,
) : EventsRepository {

    private val eventQueries = db.eventQueries
    private val rsvpQueries = db.rsvpQueries

    override suspend fun getGroupEvents(groupId: String): List<EventDomain> {
        if (!connectivity.isOnlineNow()) return cachedEventsByGroup(groupId)
        return try {
            val fresh = api.getGroupEvents(groupId).map { it.toDomain() }
            replaceEventsForGroup(groupId, fresh)
            fresh
        } catch (t: Throwable) {
            cachedEventsByGroup(groupId).ifEmpty { throw t }
        }
    }

    override suspend fun upsertRsvp(
        eventId: String,
        status: RsvpStatus,
        isPaid: Boolean,
        obs: String?
    ): RsvpDomain {
        requireOnline()
        val result = api.upsertRsvp(
            eventId = eventId,
            request = RsvpRequest(status = status.name, isPaid = isPaid, obs = obs)
        ).toDomain()
        upsertRsvpRow(result, position = -1L)
        return result
    }

    override suspend fun createGroupEvent(groupId: String, input: CreateEventInput): EventDomain {
        requireOnline()
        val created = api.createGroupEvent(groupId, input.toRequest()).toDomain()
        upsertEvent(created, position = Long.MAX_VALUE)
        return created
    }

    override suspend fun createInstantEvent(input: CreateEventInput): EventDomain {
        requireOnline()
        val created = api.createInstantEvent(input.toRequest()).toDomain()
        upsertEvent(created, position = -1L)
        return created
    }

    override suspend fun getEventById(eventId: String): EventDomain {
        if (!connectivity.isOnlineNow()) return cachedEventById(eventId)
        return try {
            val fresh = api.getEventById(eventId).toDomain()
            upsertEvent(fresh)
            fresh
        } catch (t: Throwable) {
            runCatching { cachedEventById(eventId) }.getOrNull() ?: throw t
        }
    }

    override suspend fun getEventByInvite(inviteCode: String): EventDomain {
        if (!connectivity.isOnlineNow()) return cachedEventByInvite(inviteCode)
        return try {
            val fresh = api.getEventByInvite(inviteCode).toDomain()
            upsertEvent(fresh)
            fresh
        } catch (t: Throwable) {
            runCatching { cachedEventByInvite(inviteCode) }.getOrNull() ?: throw t
        }
    }

    override suspend fun listRsvps(eventId: String): List<RsvpDomain> {
        if (!connectivity.isOnlineNow()) return cachedRsvpsByEvent(eventId)
        return try {
            val fresh = api.listRsvps(eventId).map { it.toDomain() }
            replaceRsvpsForEvent(eventId, fresh)
            fresh
        } catch (t: Throwable) {
            cachedRsvpsByEvent(eventId).ifEmpty { throw t }
        }
    }

    override suspend fun getMyRsvp(eventId: String): RsvpDomain? {
        // Este endpoint pode retornar 404; ao cair no cache, retornamos null
        // silenciosamente se offline ou se o cache não tiver o registro.
        if (!connectivity.isOnlineNow()) return cachedMyRsvp(eventId)
        return try {
            val fresh = api.getMyRsvp(eventId)?.toDomain()
            if (fresh != null) upsertRsvpRow(fresh, position = -1L)
            fresh
        } catch (t: Throwable) {
            cachedMyRsvp(eventId)
        }
    }

    override suspend fun updateEvent(eventId: String, input: UpdateEventInput): EventDomain {
        requireOnline()
        val updated = api.updateEvent(eventId, input.toUpdateRequest()).toDomain()
        upsertEvent(updated)
        return updated
    }

    override suspend fun deleteEvent(eventId: String) {
        requireOnline()
        api.deleteEvent(eventId)
        eventQueries.deleteById(eventId)
        rsvpQueries.clearByEvent(eventId)
    }

    override suspend fun deleteMyRsvp(eventId: String, userId: String) {
        requireOnline()
        api.deleteMyRsvp(eventId)
        // Apaga apenas o registro do usuário no cache. O `rsvp_status` no
        // EventEntity é refrescado pelo getEventById subsequente do ViewModel.
        rsvpQueries.deleteMy(eventId, userId)
    }

    // ── helpers de cache ───────────────────────────────────────────────

    private fun cachedEventsByGroup(groupId: String): List<EventDomain> =
        eventQueries.selectByGroup(groupId).executeAsList().map { it.toDomain() }

    private fun cachedEventById(eventId: String): EventDomain =
        eventQueries.selectById(eventId).executeAsOne().toDomain()

    private fun cachedEventByInvite(code: String): EventDomain =
        eventQueries.selectByInvite(code).executeAsOne().toDomain()

    private fun cachedRsvpsByEvent(eventId: String): List<RsvpDomain> =
        rsvpQueries.selectByEvent(eventId).executeAsList().map { it.toDomain() }

    private fun cachedMyRsvp(eventId: String): RsvpDomain? =
        rsvpQueries.selectByEvent(eventId).executeAsList()
            .map { it.toDomain() }
            .firstOrNull() // sem userId aqui; o backend já filtra

    private fun replaceEventsForGroup(groupId: String, events: List<EventDomain>) {
        val now = nowMillis()
        db.transaction {
            // Para preservar o cache de eventos consultados por id/invite que
            // também pertencem ao grupo, só limpamos os que estão vinculados
            // a este grupo. `clearByGroup` já faz isso por `group_id`.
            eventQueries.clearByGroup(groupId)
            events.forEachIndexed { index, e ->
                upsertEventInternal(e, position = index.toLong(), now = now)
            }
        }
    }

    private fun replaceRsvpsForEvent(eventId: String, rsvps: List<RsvpDomain>) {
        val now = nowMillis()
        db.transaction {
            rsvpQueries.clearByEvent(eventId)
            rsvps.forEachIndexed { index, r ->
                upsertRsvpInternal(r, position = index.toLong(), now = now)
            }
        }
    }

    private fun upsertEvent(e: EventDomain, position: Long = -1L) {
        val existing = eventQueries.selectById(e.id).executeAsOneOrNull()
        upsertEventInternal(
            e = e,
            position = existing?.position ?: position,
            now = nowMillis()
        )
    }

    private fun upsertRsvpRow(r: RsvpDomain, position: Long) {
        val existing = rsvpQueries.selectMy(r.eventId, r.userId).executeAsOneOrNull()
        upsertRsvpInternal(r, position = existing?.position ?: position, now = nowMillis())
    }

    private fun upsertEventInternal(e: EventDomain, position: Long, now: Long) {
        eventQueries.upsert(
            id = e.id,
            event_type = e.eventType.name,
            group_id = e.groupId,
            is_instant = if (e.isInstant) 1L else 0L,
            owner_id = e.ownerId,
            scheduled_datetime = e.scheduledDatetime,
            name = e.name,
            description = e.description,
            invite_code = e.inviteCode,
            created_at = e.createdAt,
            gameplay_json = e.gameplayJson(),
            sport_json = e.sportJson(),
            rsvp_status = e.rsvpStatus?.name,
            confirmed_count = e.confirmedCount.toLong(),
            confirmed_avatars_json = e.confirmedAvatarsJson(),
            position = position,
            cached_at = now
        )
    }

    private fun upsertRsvpInternal(r: RsvpDomain, position: Long, now: Long) {
        rsvpQueries.upsert(
            event_id = r.eventId,
            user_id = r.userId,
            status = r.status.name,
            is_paid = if (r.isPaid) 1L else 0L,
            obs = r.obs,
            username = r.username,
            displayname = r.displayname,
            avatar_url = r.avatarUrl,
            is_owner = if (r.isOwner) 1L else 0L,
            position = position,
            cached_at = now
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * Guarda de escrita — bloqueio duro quando offline. Simétrico ao do
     * GroupsRepositoryImpl; mantemos a checagem em ambas as camadas (repo +
     * UI) porque o estado de conectividade pode mudar entre o clique do
     * usuário e a execução da request.
     */
    private fun requireOnline() {
        if (!connectivity.isOnlineNow()) throw OfflineException()
    }
}
