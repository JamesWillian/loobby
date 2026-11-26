package app.loobby.feature.library.presentation.components

import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.gameplays.presentation.common.label
import app.loobby.feature.library.domain.model.GroupLibraryGame
import app.loobby.feature.library.domain.model.LibraryGame

// Os textos das bibliotecas, num lugar só.

/** "Nenhum jogo", "1 jogo", "12 jogos". */
fun gamesCountLabel(count: Int): String = when (count) {
    0 -> "Nenhum jogo"
    1 -> "1 jogo"
    else -> "$count jogos"
}

/** "Tenho: PC · Switch", ou "Na biblioteca" sem o "tenho o jogo". */
fun LibraryGame.ownedLine(): String =
    if (isOwned) "Tenho: " + platformsLine(ownedPlatforms) else "Na biblioteca"

/** Onde o jogo sai, pelo catálogo; ou o aviso de que o catálogo não diz. */
fun catalogPlatformsLine(platforms: List<Platform>): String =
    if (platforms.isEmpty()) "O catálogo não diz em quais plataformas o jogo sai."
    else "No catálogo: " + platformsLine(platforms.toSet())

/** "Adicionado por Ana", ou só "Na biblioteca do grupo" se a conta foi excluída. */
fun GroupLibraryGame.addedByLine(): String =
    addedBy?.let { "Adicionado por ${it.shownName}" } ?: "Na biblioteca do grupo"

private fun platformsLine(platforms: Set<Platform>): String =
    Platform.entries.filter { it in platforms }.joinToString(" · ") { it.label }
