package app.loobby.feature.gameplays.presentation.detail.components

import app.loobby.feature.gameplays.domain.model.GameplayUser
import kotlin.test.Test
import kotlin.test.assertEquals

class OwnersLabelsTest {

    private fun user(name: String) = GameplayUser(id = name, username = name.lowercase(), displayname = name, avatarUrl = null)

    @Test
    fun contagem_noSingularENoPlural() {
        assertEquals("Ninguém que vai tem", ownersCountLabel(owners = 0, going = 4))
        assertEquals("1 de 4 tem", ownersCountLabel(owners = 1, going = 4))
        assertEquals("2 de 4 têm", ownersCountLabel(owners = 2, going = 4))
    }

    @Test
    fun nomes_juntosComoNaFrase() {
        assertEquals("Tem o jogo: Ana", ownersNamesLabel(listOf(user("Ana"))))
        assertEquals("Têm o jogo: Ana e Bia", ownersNamesLabel(listOf(user("Ana"), user("Bia"))))
        assertEquals("Têm o jogo: Ana, Bia e Caio", ownersNamesLabel(listOf(user("Ana"), user("Bia"), user("Caio"))))
    }
}
