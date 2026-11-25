package app.loobby.feature.gameplays.presentation.form

import app.loobby.core.ui.components.parseDateDigits
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.GameplayRules
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Confere o formulário antes de mandar para a API, com as mesmas regras dela, para
 * o erro aparecer na hora e em português. A API confere de novo.
 */
object GameplayFormValidator {

    sealed interface Result {
        data class Valid(val scheduledAt: Instant) : Result
        data class Invalid(val message: String) : Result
    }

    /**
     * [checkSchedule] false: na edição, o horário não mudou e pode já ter passado
     * (a gameplay em andamento); aí ele não é conferido.
     */
    fun validate(
        state: GameplayFormUiState,
        now: Instant,
        zone: TimeZone,
        checkSchedule: Boolean = true,
    ): Result {
        val name = state.name.trim()
        val scheduledAt = scheduleOf(state.dateDigits, state.timeDigits, zone)
        val error = when {
            name.isEmpty() -> "Dê um nome à gameplay"
            name.length > GameplayRules.MAX_NAME -> "Use até ${GameplayRules.MAX_NAME} caracteres no nome"
            state.description.trim().length > GameplayRules.MAX_DESCRIPTION ->
                "Use até ${GameplayRules.MAX_DESCRIPTION} caracteres na descrição"
            scheduledAt == null -> "Informe a data e a hora"
            checkSchedule && scheduledAt < now - GameplayRules.PAST_TOLERANCE -> "Esse horário já passou"
            state.platform == null -> "Escolha a plataforma"
            state.mode == GameplayMode.FIXED && state.fixedGame == null -> "Escolha o jogo"
            state.mode == GameplayMode.VOTING -> votingError(state)
            else -> null
        }
        return if (error != null || scheduledAt == null) Result.Invalid(error ?: "Informe a data e a hora")
        else Result.Valid(scheduledAt)
    }

    /** O mesmo jogo do catálogo ou o mesmo nome, sem diferenciar maiúsculas. */
    fun isRepeated(games: List<GameChoice>, candidate: GameChoice): Boolean = games.any { game ->
        game.name.equals(candidate.name, ignoreCase = true) ||
            (candidate.catalogId != null && game.catalogId == candidate.catalogId)
    }

    /** DDMMAAAA + HHMM no fuso [zone] → o instante; null se incompleto ou inválido. */
    fun scheduleOf(dateDigits: String, timeDigits: String, zone: TimeZone): Instant? {
        val date = parseDateDigits(dateDigits) ?: return null
        if (timeDigits.length != 4) return null
        val time = runCatching {
            LocalTime(hour = timeDigits.substring(0, 2).toInt(), minute = timeDigits.substring(2, 4).toInt())
        }.getOrNull() ?: return null
        return LocalDateTime(date, time).toInstant(zone)
    }

    private fun votingError(state: GameplayFormUiState): String? = when {
        state.finalists !in 1..GameplayRules.MAX_FINALISTS -> "Finalistas: de 1 a ${GameplayRules.MAX_FINALISTS}"
        state.votingGames.size > GameplayRules.MAX_GAMES -> "Até ${GameplayRules.MAX_GAMES} jogos na votação"
        else -> null
    }
}
