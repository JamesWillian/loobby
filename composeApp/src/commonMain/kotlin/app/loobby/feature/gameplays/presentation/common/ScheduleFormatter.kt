package app.loobby.feature.gameplays.presentation.common

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Datas e horas como as telas mostram, no fuso do aparelho. */
object ScheduleFormatter {

    /** "Hoje, 21:00", "Amanhã, 21:00", "Sex, 28/11 · 21:00". */
    fun schedule(
        instant: Instant,
        now: Instant = Clock.System.now(),
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val local = instant.toLocalDateTime(zone)
        val today = now.toLocalDateTime(zone).date
        val time = local.hourMinute()
        return when (local.date) {
            today -> "Hoje, $time"
            today.plus(DatePeriod(days = 1)) -> "Amanhã, $time"
            today.minus(DatePeriod(days = 1)) -> "Ontem, $time"
            else -> "${weekday(local.dayOfWeek)}, ${local.dayMonth(withYear = local.year != today.year)} · $time"
        }
    }

    /** "28/11, 22:10": para quando encerrou ou vai encerrar. */
    fun dayAndTime(
        instant: Instant,
        now: Instant = Clock.System.now(),
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val local = instant.toLocalDateTime(zone)
        val sameYear = local.year == now.toLocalDateTime(zone).year
        return "${local.dayMonth(withYear = !sameYear)}, ${local.hourMinute()}"
    }

    /** "agora", "há 5 min", "há 3 h", "ontem", "28/11": a hora de um comentário. */
    fun relative(
        instant: Instant,
        now: Instant = Clock.System.now(),
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val elapsed = now - instant
        val local = instant.toLocalDateTime(zone)
        val today = now.toLocalDateTime(zone).date
        return when {
            elapsed < 1.minutes -> "agora"
            elapsed < 1.hours -> "há ${elapsed.inWholeMinutes} min"
            local.date == today -> "há ${elapsed.inWholeHours} h"
            local.date == today.minus(DatePeriod(days = 1)) -> "ontem"
            else -> local.dayMonth(withYear = local.year != today.year)
        }
    }

    private fun LocalDateTime.hourMinute(): String =
        hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')

    private fun LocalDateTime.dayMonth(withYear: Boolean): String {
        val dayMonth = day.toString().padStart(2, '0') + "/" + month.number.toString().padStart(2, '0')
        return if (withYear) "$dayMonth/$year" else dayMonth
    }

    private fun weekday(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Seg"
        DayOfWeek.TUESDAY -> "Ter"
        DayOfWeek.WEDNESDAY -> "Qua"
        DayOfWeek.THURSDAY -> "Qui"
        DayOfWeek.FRIDAY -> "Sex"
        DayOfWeek.SATURDAY -> "Sáb"
        DayOfWeek.SUNDAY -> "Dom"
    }
}
