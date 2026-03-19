package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun buildShareText(
    event: EventDomain,
    rsvps: List<RsvpDomain>,
    includeRsvpList: Boolean
): String = buildString {

    // ── Cabeçalho ─────────────────────────────────────────────────────────────
    val typeEmoji = when (event.eventType) {
        EventType.SPORT    -> "🏐"
        EventType.GAMEPLAY -> "🎮"
    }
    appendLine("$typeEmoji *${event.name}*")

    // ── Detalhes do evento ────────────────────────────────────────────────────
    event.description?.takeIf { it.isNotBlank() }?.let {
        appendLine(it)
    }

    appendLine()

    appendLine("📅 ${event.scheduledDatetime.formatShareDate()}")

    event.sport?.let { sport ->
        sport.arena?.let { appendLine("📍 $it") }
        if (sport.durationMinutes > 0) appendLine("🕐 ${sport.durationMinutes} min.")
        sport.maxPlayers?.let { appendLine("👥 $it jogadores máx.") }
        if (sport.pricePerPlayer > 0.toDouble()) {
            val whole = sport.pricePerPlayer.toLong()
            val cents = ((sport.pricePerPlayer - whole) * 100).toLong()
            appendLine("💰 R$ $whole,${cents.toString().padStart(2, '0')} por pessoa")
        }
    }

    event.gameplay?.let { gameplay ->
        appendLine("🎮 ${gameplay.gameName}")
    }

    // ── Confirmados ───────────────────────────────────────────────────────────
//    val confirmed = rsvps.filter { it.status == RsvpStatus.YES }
//    if (confirmed.isNotEmpty()) {
//        appendLine()
//        appendLine("✅ *${confirmed.size} confirmado${if (confirmed.size > 1) "s" else ""}*")
//    }

    if (!includeRsvpList) return@buildString

    // ── Lista de RSVP por status ──────────────────────────────────────────────
    appendLine()

    val order = listOf(
        RsvpStatus.YES     to "✅ Confirmados",
        RsvpStatus.RESERVE to "🔄 Reservas",
        RsvpStatus.MAYBE   to "🤔 Talvez",
        RsvpStatus.NO      to "❌ Não vão",
        RsvpStatus.PENDING to "⏳ Pendentes"
    )

    order.forEach { (status, header) ->
        val list = rsvps.filter { it.status == status }
        if (list.isEmpty()) return@forEach

        appendLine("*$header:*")
        list.forEachIndexed { index, rsvp ->

            val position = index.plus(1).toString().padStart(2,'0')

            val name = rsvp.displayname ?: rsvp.username

            // ✅ verde na frente se já pagou
            val paidMark = if (rsvp.isPaid) " : 🆗" else ""

            appendLine(" $position • $name$paidMark")

            // Observação como subtópico
            rsvp.obs?.takeIf { it.isNotBlank() }?.let { obs ->
                appendLine("      (_${obs}_)")
            }
        }
        appendLine()
    }
}

private fun String.formatShareDate(): String {
    return runCatching {
        val instant = Instant.parse(this)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = local.hour.toString().padStart(2, '0')
        val min = local.minute.toString().padStart(2, '0')
        val day = local.dayOfMonth.toString().padStart(2, '0')
        val month = local.monthNumber.toString().padStart(2, '0')
        val dayOfWeek = when (local.dayOfWeek.ordinal) {
            0 -> "Seg"
            1 -> "Ter"
            2 -> "Qua"
            3 -> "Qui"
            4 -> "Sex"
            5 -> "Sáb"
            else -> "Dom"
        }
        "$dayOfWeek, $day/$month às $hour:$min"
    }.getOrDefault(this)
}