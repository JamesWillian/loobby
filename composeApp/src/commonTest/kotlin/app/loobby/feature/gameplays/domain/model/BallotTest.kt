package app.loobby.feature.gameplays.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class BallotTest {

    @Test
    fun votoUnico_trocaOVoto() {
        assertEquals(listOf("b"), Ballot.afterTap(setOf("a"), tapped = "b", multiVote = false))
    }

    @Test
    fun votoUnico_tocarNoMesmo_tiraOVoto() {
        assertEquals(emptyList(), Ballot.afterTap(setOf("a"), tapped = "a", multiVote = false))
    }

    @Test
    fun votoMultiplo_marcaMaisUm() {
        assertEquals(setOf("a", "b"), Ballot.afterTap(setOf("a"), tapped = "b", multiVote = true).toSet())
    }

    @Test
    fun votoMultiplo_tocarNumVotado_desmarcaSoEle() {
        assertEquals(listOf("b"), Ballot.afterTap(setOf("a", "b"), tapped = "a", multiVote = true))
    }

    @Test
    fun semVoto_oPrimeiroToqueVota() {
        assertEquals(listOf("a"), Ballot.afterTap(emptySet(), tapped = "a", multiVote = false))
    }
}
