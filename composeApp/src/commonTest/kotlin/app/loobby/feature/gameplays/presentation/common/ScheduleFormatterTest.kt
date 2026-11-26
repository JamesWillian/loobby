package app.loobby.feature.gameplays.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/** Em São Paulo (UTC-3), numa quarta, 25/11/2026, às 10h. */
class ScheduleFormatterTest {

    private val zone = TimeZone.of("America/Sao_Paulo")
    private val now = Instant.parse("2026-11-25T13:00:00Z")

    @Test
    fun hojeEAmanha_temNome() {
        assertEquals("Hoje, 21:00", ScheduleFormatter.schedule(Instant.parse("2026-11-26T00:00:00Z"), now, zone))
        assertEquals("Amanhã, 21:00", ScheduleFormatter.schedule(Instant.parse("2026-11-27T00:00:00Z"), now, zone))
        assertEquals("Ontem, 21:00", ScheduleFormatter.schedule(Instant.parse("2026-11-25T00:00:00Z"), now, zone))
    }

    @Test
    fun outroDia_temDiaDaSemanaEData() {
        assertEquals("Sex, 27/11 · 21:00", ScheduleFormatter.schedule(Instant.parse("2026-11-28T00:00:00Z"), now, zone))
    }

    @Test
    fun outroAno_mostraOAno() {
        assertEquals("Sex, 01/01/2027 · 20:30", ScheduleFormatter.schedule(Instant.parse("2027-01-01T23:30:00Z"), now, zone))
    }

    @Test
    fun relativo_dosComentarios() {
        assertEquals("agora", ScheduleFormatter.relative(now - 30.0.minutes / 60, now, zone))
        assertEquals("há 5 min", ScheduleFormatter.relative(now - 5.minutes, now, zone))
        assertEquals("há 3 h", ScheduleFormatter.relative(now - 3.hours, now, zone))
        assertEquals("ontem", ScheduleFormatter.relative(now - 1.days, now, zone))
        assertEquals("20/11", ScheduleFormatter.relative(now - 5.days, now, zone))
    }

    @Test
    fun diaEHora_doEncerramento() {
        assertEquals("26/11, 21:00", ScheduleFormatter.dayAndTime(Instant.parse("2026-11-27T00:00:00Z"), now, zone))
    }
}
