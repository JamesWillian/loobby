package app.loobby.feature.gameplays.presentation.form

import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.Platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/** Agora: 25/11/2026, 10h em São Paulo. */
class GameplayFormValidatorTest {

    private val zone = TimeZone.of("America/Sao_Paulo")
    private val now = Instant.parse("2026-11-25T13:00:00Z")

    private val valid = GameplayFormUiState(
        mode = GameplayMode.FREE,
        name = "Sexta do CS",
        dateDigits = "27112026",
        timeDigits = "2100",
        platform = Platform.PC,
    )

    private fun errorOf(state: GameplayFormUiState, checkSchedule: Boolean = true): String =
        (GameplayFormValidator.validate(state, now, zone, checkSchedule) as GameplayFormValidator.Result.Invalid).message

    @Test
    fun formularioCompleto_valeComOHorarioNoFuso() {
        val result = GameplayFormValidator.validate(valid, now, zone)

        assertIs<GameplayFormValidator.Result.Valid>(result)
        assertEquals(Instant.parse("2026-11-28T00:00:00Z"), result.scheduledAt)
    }

    @Test
    fun semNome_semPlataforma_semHorario() {
        assertEquals("Dê um nome à gameplay", errorOf(valid.copy(name = "  ")))
        assertEquals("Escolha a plataforma", errorOf(valid.copy(platform = null)))
        assertEquals("Informe a data e a hora", errorOf(valid.copy(timeDigits = "21")))
        assertEquals("Informe a data e a hora", errorOf(valid.copy(dateDigits = "31022026")))
    }

    @Test
    fun horarioPassado_soVale_comAToleranciaDe5Minutos() {
        val fourMinutesAgo = valid.copy(dateDigits = "25112026", timeDigits = "0956")
        val tenMinutesAgo = valid.copy(dateDigits = "25112026", timeDigits = "0950")

        assertIs<GameplayFormValidator.Result.Valid>(GameplayFormValidator.validate(fourMinutesAgo, now, zone))
        assertEquals("Esse horário já passou", errorOf(tenMinutesAgo))
    }

    @Test
    fun naEdicao_horarioQueNaoMudou_naoEConferido() {
        val inProgress = valid.copy(dateDigits = "25112026", timeDigits = "0800")

        assertIs<GameplayFormValidator.Result.Valid>(
            GameplayFormValidator.validate(inProgress, now, zone, checkSchedule = false),
        )
    }

    @Test
    fun jogoDefinido_precisaDoJogo() {
        val fixed = valid.copy(mode = GameplayMode.FIXED)

        assertEquals("Escolha o jogo", errorOf(fixed))
        assertIs<GameplayFormValidator.Result.Valid>(
            GameplayFormValidator.validate(fixed.copy(fixedGame = GameChoice(null, "Truco")), now, zone),
        )
    }

    @Test
    fun jogoRepetido_pelaMesmaIdOuPeloMesmoNome() {
        val games = listOf(GameChoice("4291", "Counter-Strike 2"))

        assertTrue(GameplayFormValidator.isRepeated(games, GameChoice(null, "counter-strike 2")))
        assertTrue(GameplayFormValidator.isRepeated(games, GameChoice("4291", "CS2")))
        assertTrue(!GameplayFormValidator.isRepeated(games, GameChoice("3498", "GTA V")))
    }
}
