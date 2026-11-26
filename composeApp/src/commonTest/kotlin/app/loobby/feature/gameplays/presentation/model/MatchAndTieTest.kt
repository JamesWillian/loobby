package app.loobby.feature.gameplays.presentation.model

import app.loobby.feature.gameplays.data.mapper.toDomain
import app.loobby.feature.gameplays.data.model.GameplayResponse
import app.loobby.feature.gameplays.data.model.GameplaySummaryResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** O match (owners) e o empate (tie) que a Fase 3 trouxe para as respostas da API. */
class MatchAndTieTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun user(id: String) = """{"id":"$id","username":"$id","displayname":"","avatarUrl":null}"""

    private fun detail(mode: String, game: String, voting: String, round: Int = 1) = """
        {"id":"g1","groupId":"grp","groupName":"Turma","name":"Sexta","mode":"$mode","platforms":["PC"],
         "scheduledAt":"2026-11-28T00:00:00Z","autoEndAt":"2026-11-29T00:00:00Z","status":"ACTIVE",
         "creator":${user("ana")},"createdAt":"2026-11-25T10:00:00Z","myStatus":"GOING",
         "counts":{"going":3,"notGoing":0,"pending":0},"game":$game,
         "voting":${voting.replace("ROUND", round.toString())}}
    """.trimIndent()

    @Test
    fun votacao_trazQuemTemCadaJogo() {
        val voting = """{"finalists":1,"round":ROUND,"games":[
            {"id":"vg1","gameId":"4291","name":"CS2","candidate":true,"owners":[${user("ana")},${user("bia")}]},
            {"id":"vg2","gameId":null,"name":"Truco","candidate":true,"owners":[]}],
            "result":{"round":1,"finalistIds":[],"tie":false}}"""
        val gameplay = json.decodeFromString<GameplayResponse>(detail("VOTING", "null", voting)).toDomain()
        val ballot = gameplay.voting!!.ballot()

        assertEquals(listOf("ana", "bia"), ballot[0].owners.map { it.id })
        assertTrue(ballot[0].isCatalogGame)
        assertEquals(false, ballot[1].isCatalogGame) // digitado: sem match
    }

    @Test
    fun jogoDefinido_trazQuemTem() {
        val game = """{"gameId":"4291","name":"CS2","coverUrl":null,"owners":[${user("caio")}]}"""
        val gameplay = json.decodeFromString<GameplayResponse>(detail("FIXED", game, "null")).toDomain()

        assertEquals(listOf("caio"), gameplay.gameOwners.map { it.id })
        assertEquals("CS2", gameplay.game?.name)
    }

    @Test
    fun listas_mostramOEmpate() {
        val summary = json.decodeFromString<GameplaySummaryResponse>(
            """{"id":"g1","name":"Sexta","mode":"VOTING","scheduledAt":"2026-11-28T00:00:00Z",
                "autoEndAt":"2026-11-29T00:00:00Z","status":"ACTIVE","creator":${user("ana")},"myStatus":"GOING",
                "leading":[{"gameId":"1","name":"A"},{"gameId":"2","name":"B"}],"tie":true}"""
        ).toDomain()

        assertEquals(ModeHighlight.Leading(listOf("A", "B"), isTie = true), summary.toCardModel().highlight)
    }

    @Test
    fun listas_semOTie_saoFinalistas() {
        val summary = json.decodeFromString<GameplaySummaryResponse>(
            """{"id":"g1","name":"Sexta","mode":"VOTING","scheduledAt":"2026-11-28T00:00:00Z",
                "autoEndAt":"2026-11-29T00:00:00Z","status":"ENDED","creator":${user("ana")},"myStatus":"GOING",
                "leading":[{"gameId":"1","name":"A"},{"gameId":"2","name":"B"}]}"""
        ).toDomain()

        assertEquals(ModeHighlight.Leading(listOf("A", "B"), isTie = false), summary.toCardModel().highlight)
    }

    @Test
    fun desempateSemVoto_oCardMostraOEmpateQueOAbriu() {
        val voting = """{"finalists":1,"round":ROUND,"games":[
            {"id":"vg1","gameId":"1","name":"A","candidate":true},
            {"id":"vg2","gameId":"2","name":"B","candidate":true},
            {"id":"vg3","gameId":"3","name":"C","candidate":false}],
            "result":{"round":2,"finalistIds":[],"tie":false}}"""
        val gameplay = json.decodeFromString<GameplayResponse>(detail("VOTING", "null", voting, round = 2)).toDomain()

        assertEquals(ModeHighlight.Leading(listOf("A", "B"), isTie = true), gameplay.toCardModel().highlight)
    }
}
