package app.loobby.feature.library.data.mapper

import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.library.data.model.GroupLibraryGameResponse
import app.loobby.feature.library.data.model.LibraryGameResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** As respostas no formato da API (internal/library/types.go do loobby-api). */
class LibraryMappersTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun jogoDaBiblioteca_viraODominio() {
        val game = json.decodeFromString<LibraryGameResponse>(
            """{"gameId":"3498","name":"Grand Theft Auto V","coverUrl":"https://media.rawg.io/g.jpg",
                "availablePlatforms":["PC","PLAYSTATION","XBOX","VR"],"ownedPlatforms":["XBOX"],
                "addedAt":"2026-11-26T01:00:00Z","updatedAt":"2026-11-26T02:00:00Z"}"""
        ).toDomain()

        assertEquals("3498", game.catalogId)
        assertEquals(listOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX), game.availablePlatforms) // "VR" fica de fora
        assertEquals(setOf(Platform.XBOX), game.ownedPlatforms)
        assertTrue(game.isOwned)
        // as do catálogo primeiro, depois as outras, que também valem
        assertEquals(
            listOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX, Platform.SWITCH, Platform.MOBILE),
            game.platformChoices,
        )
    }

    @Test
    fun semPlataformasNoCatalogo_ofereceTodas_naOrdemDoApp() {
        val game = json.decodeFromString<LibraryGameResponse>(
            """{"gameId":"1","name":"Hades","availablePlatforms":[],"ownedPlatforms":[],"addedAt":"2026-11-26T01:00:00Z"}"""
        ).toDomain()

        assertFalse(game.isOwned)
        assertEquals(Platform.entries.toList(), game.platformChoices)
    }

    @Test
    fun jogoDoGrupo_semQuemAdicionou() {
        val game = json.decodeFromString<GroupLibraryGameResponse>(
            """{"gameId":"4291","name":"CS2","coverUrl":null,"availablePlatforms":["PC"],"addedBy":null,
                "addedAt":"2026-11-26T01:00:00Z"}"""
        ).toDomain()

        assertNull(game.addedBy)
        assertEquals(listOf(Platform.PC), game.availablePlatforms)
    }

    @Test
    fun tenhoOJogo_vaiParaAApiNaOrdemDoApp() {
        assertEquals(listOf("PC", "SWITCH"), setOf(Platform.SWITCH, Platform.PC).toRequest())
        assertEquals(emptyList(), emptySet<Platform>().toRequest())
    }
}
