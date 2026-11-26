package app.loobby.feature.gameplays.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class InviteCodeTest {

    @Test
    fun seisCaracteres_eGrupo() {
        assertEquals(InviteKind.GROUP, InviteCode.kindOf("$-ABC123"))
        assertEquals(InviteKind.GROUP, InviteCode.kindOf("abc123"))
    }

    @Test
    fun oitoCaracteres_eGameplayUnica() {
        assertEquals(InviteKind.GAMEPLAY, InviteCode.kindOf("$-AB12CD34"))
        assertEquals(InviteKind.GAMEPLAY, InviteCode.kindOf("  $-ab12cd34 "))
    }

    @Test
    fun quatroTracoQuatro_eEventoDaVersaoAntiga() {
        assertEquals(InviteKind.LEGACY_EVENT, InviteCode.kindOf("$-A1B2-C3D4"))
    }

    @Test
    fun outrosFormatos_naoSaoConvite() {
        assertEquals(InviteKind.UNKNOWN, InviteCode.kindOf("$-ABC"))
        assertEquals(InviteKind.UNKNOWN, InviteCode.kindOf("$-ABC-123"))
        assertEquals(InviteKind.UNKNOWN, InviteCode.kindOf(""))
    }

    @Test
    fun normalize_poeOPrefixoEMaiusculas() {
        assertEquals("$-AB12CD34", InviteCode.normalize(" ab12cd34 "))
        assertEquals("$-AB12CD34", InviteCode.normalize("$-ab12cd34"))
    }
}
