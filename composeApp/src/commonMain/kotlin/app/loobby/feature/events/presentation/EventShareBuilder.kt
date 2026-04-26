package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Monta o texto de compartilhamento do evento conforme as preferências do
 * usuário escolhidas no diálogo de compartilhamento.
 *
 * @param useEmoji  Quando `true`, usa emojis para identificar os campos
 *                  (📅, 🕐, 📍, 💰, 👥) e prefixa o título com o ícone do tipo
 *                  do evento. Quando `false`, usa rótulos em texto
 *                  (Data, Horário, Local, Valor, Vagas) e omite o ícone do
 *                  título.
 * @param includedStatuses  Conjunto de status de RSVP a incluir na lista de
 *                          presença. Se vazio, a lista de presença não é
 *                          adicionada ao texto.
 * @param includePaymentConfirm  Se `true`, indica os participantes que já
 *                               pagaram (`🆗` em modo emoji ou ` ✓` em
 *                               modo texto). Se `false`, omite essa marca.
 * @param includeObservations  Se `true`, exibe os comentários (campo `obs`)
 *                             dos participantes como subtópico abaixo do
 *                             nome. Se `false`, omite os comentários.
 */
fun buildShareText(
    event: EventDomain,
    rsvps: List<RsvpDomain>,
    useEmoji: Boolean = true,
    includedStatuses: Set<RsvpStatus> = setOf(
        RsvpStatus.YES, RsvpStatus.RESERVE, RsvpStatus.MAYBE, RsvpStatus.NO
    ),
    includePaymentConfirm: Boolean = false,
    includeObservations: Boolean = false
): String = buildString {

    // ── Cabeçalho (título + descrição) ────────────────────────────────────────
    // Por especificação, título e descrição não recebem rótulo de identificação.
    // No modo emoji, o título recebe o ícone do tipo de evento como decoração.
    if (useEmoji) {
        val typeEmoji = when (event.eventType) {
            EventType.SPORT    -> "🏐"
            EventType.GAMEPLAY -> "🎮"
        }
        appendLine("$typeEmoji *${event.name}*")
    } else {
        appendLine("*${event.name}*")
    }

    event.description?.takeIf { it.isNotBlank() }?.let {
        appendLine(it)
    }

    appendLine()

    // ── Detalhes do evento (com identificação por emoji ou texto) ─────────────
    val date = event.scheduledDatetime.formatShareDateOnly()
    appendLine(labeledLine(useEmoji, emoji = "📅", textLabel = "*Data*", value = date))

    val durationMinutes = event.sport?.durationMinutes ?: 0
    val timeRange = event.scheduledDatetime.formatShareTimeRange(durationMinutes)
    appendLine(labeledLine(useEmoji, emoji = "🕐", textLabel = "*Horário*", value = timeRange))

    event.sport?.let { sport ->
        sport.arena?.takeIf { it.isNotBlank() }?.let { arena ->
            appendLine(labeledLine(useEmoji, emoji = "📍", textLabel = "*Local*", value = arena))
        }
        if (sport.pricePerPlayer > 0.toDouble()) {
            val whole = sport.pricePerPlayer.toLong()
            val cents = ((sport.pricePerPlayer - whole) * 100).toLong()
            val priceStr = "R$ $whole,${cents.toString().padStart(2, '0')}"
            appendLine(labeledLine(useEmoji, emoji = "💰", textLabel = "*Valor*", value = priceStr))
        }
        sport.maxPlayers?.let { max ->
            val vagasValue = if (useEmoji) "$max vagas" else "$max pessoas"
            appendLine(labeledLine(useEmoji, emoji = "👥", textLabel = "*Vagas*", value = vagasValue))
        }
    }

    event.gameplay?.let { gameplay ->
        // Para eventos do tipo gameplay, identificamos o jogo. Não está na
        // lista de campos especificados, mas mantemos coerência com o
        // comportamento anterior.
        appendLine(labeledLine(useEmoji, emoji = "🎮", textLabel = "Jogo", value = gameplay.gameName))
    }

    // ── Lista de presença ─────────────────────────────────────────────────────
    if (includedStatuses.isEmpty()) return@buildString

    appendLine()

    val order = listOf(
        Triple(RsvpStatus.YES,     "✅", "Confirmados"),
        Triple(RsvpStatus.NO,      "❌", "Não vão"),
        Triple(RsvpStatus.RESERVE, "🔄", "Reservas"),
        Triple(RsvpStatus.MAYBE,   "🤔", "Talvez"),
        Triple(RsvpStatus.PENDING, "⏳", "Pendentes")
    )

    order.forEach { (status, emoji, label) ->
        if (status !in includedStatuses) return@forEach
        val list = rsvps.filter { it.status == status }
        if (list.isEmpty()) return@forEach

        val header = if (useEmoji) "$emoji $label" else label
        appendLine("*$header:*")
        list.forEachIndexed { index, rsvp ->
            val position = index.plus(1).toString().padStart(2, '0')
            val name = rsvp.displayname ?: rsvp.username

            val paidMark = if (rsvp.isPaid && includePaymentConfirm) {
                if (useEmoji) " : 🆗" else " ✓"
            } else ""

            appendLine(" $position • $name$paidMark")

            // Observação como subtópico (somente se o usuário pediu)
            if (includeObservations) {
                rsvp.obs?.takeIf { it.isNotBlank() }?.let { obs ->
                    appendLine("      (_${obs}_)")
                }
            }
        }
        appendLine()
    }
}

/** Monta uma linha "rótulo + valor" usando emoji ou texto. */
private fun labeledLine(useEmoji: Boolean, emoji: String, textLabel: String, value: String): String =
    if (useEmoji) "$emoji $value" else "$textLabel: $value"

/** Formata a data no padrão "Segunda, 27/04". */
private fun String.formatShareDateOnly(): String {
    return runCatching {
        val instant = Instant.parse(this)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = local.dayOfMonth.toString().padStart(2, '0')
        val month = local.monthNumber.toString().padStart(2, '0')
        val dayOfWeek = when (local.dayOfWeek.ordinal) {
            0 -> "Segunda"
            1 -> "Terça"
            2 -> "Quarta"
            3 -> "Quinta"
            4 -> "Sexta"
            5 -> "Sábado"
            else -> "Domingo"
        }
        "$dayOfWeek, $day/$month"
    }.getOrDefault(this)
}

/**
 * Formata o intervalo de horário no padrão "hh:mm às hh:mm", calculando o
 * horário final a partir do horário inicial somado à duração informada.
 * Se a duração for 0, exibe apenas o horário inicial.
 */
private fun String.formatShareTimeRange(durationMinutes: Int): String {
    return runCatching {
        val instant = Instant.parse(this)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val startHour = local.hour.toString().padStart(2, '0')
        val startMin = local.minute.toString().padStart(2, '0')
        if (durationMinutes <= 0) {
            "$startHour:$startMin"
        } else {
            val endEpochSeconds = instant.epochSeconds + durationMinutes.toLong() * 60L
            val endInstant = Instant.fromEpochSeconds(endEpochSeconds)
            val endLocal = endInstant.toLocalDateTime(TimeZone.currentSystemDefault())
            val endHour = endLocal.hour.toString().padStart(2, '0')
            val endMin = endLocal.minute.toString().padStart(2, '0')
            "$startHour:$startMin às $endHour:$endMin"
        }
    }.getOrDefault(this)
}
