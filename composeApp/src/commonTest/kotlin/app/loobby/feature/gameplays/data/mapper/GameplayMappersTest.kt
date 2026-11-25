package app.loobby.feature.gameplays.data.mapper

import app.loobby.feature.gameplays.data.model.GameplayResponse
import app.loobby.feature.gameplays.data.model.GameplaySummaryResponse
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.GameplayChanges
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.GameplayStatus
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.gameplays.domain.model.VotingConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/** As respostas no formato da API (internal/gameplays/types.go do loobby-api). */
class GameplayMappersTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val detail = """
        {"id":"g1","groupId":"grp","groupName":"Turma do CS","name":"Sexta do CS","description":"",
         "mode":"VOTING","platforms":["PC","VR"],
         "scheduledAt":"2026-11-28T00:00:00Z","autoEndAt":"2026-11-29T00:00:00Z","status":"ACTIVE",
         "endedAt":null,"endedBy":null,"inviteCode":null,
         "creator":{"id":"u1","username":"ana","displayname":"Ana","avatarUrl":null},
         "createdAt":"2026-11-25T10:00:00.211055Z","myStatus":"GOING",
         "permissions":{"respond":true,"vote":true,"editGames":true,"comment":true,"manage":false,
                        "configureVoting":false,"tiebreak":true,"leave":false,"moderate":false,"futuro":true},
         "counts":{"going":2,"notGoing":1,"pending":0},
         "participants":[
           {"user":{"id":"u1","username":"ana","displayname":"Ana","avatarUrl":"https://a/1.png"},"status":"GOING","isAnonymous":false},
           {"user":{"id":"u2","username":"bia","displayname":"","avatarUrl":null},"status":"GOING","isAnonymous":false},
           {"user":{"id":"u3","username":"caio","displayname":"Caio","avatarUrl":null},"status":"NOT_GOING","isAnonymous":false}],
         "game":null,
         "voting":{"finalists":1,"multiVote":false,"locked":true,"round":1,
           "games":[
             {"id":"vg1","gameId":"4291","name":"Counter-Strike 2","coverUrl":"https://c/cs.jpg","addedBy":"u1",
              "candidate":true,"myVote":true,"rounds":[{"round":1,"votes":1,"voters":[{"id":"u1","username":"ana","displayname":"Ana","avatarUrl":null}]}]},
             {"id":"vg2","gameId":null,"name":"Truco","coverUrl":null,"addedBy":"u2",
              "candidate":true,"myVote":false,"rounds":[{"round":1,"votes":1,"voters":[{"id":"u2","username":"bia","displayname":"","avatarUrl":null}]}]}],
           "result":{"round":1,"finalistIds":["vg1","vg2"],"tie":true}}}
    """.trimIndent()

    @Test
    fun detalhe_viraODominio() {
        val gameplay = json.decodeFromString<GameplayResponse>(detail).toDomain()

        assertEquals(GameplayMode.VOTING, gameplay.mode)
        assertEquals(listOf(Platform.PC), gameplay.platforms) // "VR" o app ainda não conhece: fica de fora
        assertEquals(Instant.parse("2026-11-28T00:00:00Z"), gameplay.scheduledAt)
        assertEquals(GameplayStatus.ACTIVE, gameplay.status)
        assertNull(gameplay.description) // descrição vazia é nenhuma
        assertEquals(ParticipationStatus.GOING, gameplay.myStatus)
        assertTrue(gameplay.permissions.tiebreak)
        assertFalse(gameplay.permissions.manage)
        assertEquals(listOf("https://a/1.png", null), gameplay.goingAvatars)
        assertEquals("bia", gameplay.participants[1].user.shownName) // sem nome de exibição, vale o @
    }

    @Test
    fun votacao_trazOsVotosDaRodadaAberta_eOEmpate() {
        val voting = json.decodeFromString<GameplayResponse>(detail).toDomain().voting!!

        assertEquals(setOf("vg1"), voting.myVotes)
        assertEquals(2, voting.candidates.size)
        assertTrue(voting.result.tie)
        assertTrue(voting.isFinalist(voting.games[1]))
        assertFalse(voting.allowsMultipleVotes)
        assertNull(voting.games[1].catalogId) // jogo digitado: sem id do catálogo
    }

    @Test
    fun itemDaLista_viraOResumo() {
        val summary = json.decodeFromString<GameplaySummaryResponse>(
            """
            {"id":"g2","groupId":null,"groupName":null,"name":"Zerar o Hades","mode":"FIXED","platforms":["SWITCH"],
             "scheduledAt":"2026-11-29T23:00:00Z","autoEndAt":"2026-11-30T23:00:00Z","status":"ACTIVE","endedAt":null,
             "creator":{"id":"u1","username":"ana","displayname":"Ana","avatarUrl":null},"myStatus":"PENDING",
             "goingCount":1,"goingAvatars":[null],"game":{"gameId":"3498","name":"Hades","coverUrl":null},"leading":[]}
            """.trimIndent(),
        ).toDomain()

        assertTrue(summary.isSingle)
        assertEquals("Hades", summary.game?.name)
        assertEquals(ParticipationStatus.PENDING, summary.myStatus)
        assertEquals(listOf(null), summary.goingAvatars)
    }

    @Test
    fun patch_levaSoOQueMudou_semNulls() {
        val body: JsonObject = GameplayChanges(
            description = "",
            platform = Platform.XBOX,
            game = GameChoice(catalogId = null, name = "Truco"),
            voting = VotingConfig(finalists = 2, multiVote = true),
        ).toPatchJson()

        assertEquals(setOf("description", "platforms", "game", "voting"), body.keys)
        assertEquals("", body["description"]!!.jsonPrimitive.content) // vazia apaga a descrição
        assertEquals("XBOX", body["platforms"]!!.jsonArray.single().jsonPrimitive.content)
        assertEquals(setOf("name"), body["game"]!!.jsonObject.keys) // digitado: sem gameId
    }
}
