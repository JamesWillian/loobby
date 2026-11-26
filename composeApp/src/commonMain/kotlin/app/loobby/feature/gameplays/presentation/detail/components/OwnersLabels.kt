package app.loobby.feature.gameplays.presentation.detail.components

import app.loobby.feature.gameplays.domain.model.GameplayUser

// Os textos do match: quem vai e tem o jogo na biblioteca.

/** "2 de 4 têm", "1 de 4 tem", ou "Ninguém que vai tem". */
fun ownersCountLabel(owners: Int, going: Int): String = when (owners) {
    0 -> "Ninguém que vai tem"
    1 -> "1 de $going tem"
    else -> "$owners de $going têm"
}

/** "Tem o jogo: Ana", "Têm o jogo: Ana, Bia e Caio". */
fun ownersNamesLabel(owners: List<GameplayUser>): String {
    val names = owners.map { it.shownName }
    val joined = when (names.size) {
        1 -> names.first()
        else -> names.dropLast(1).joinToString(", ") + " e " + names.last()
    }
    return (if (names.size == 1) "Tem o jogo: " else "Têm o jogo: ") + joined
}
