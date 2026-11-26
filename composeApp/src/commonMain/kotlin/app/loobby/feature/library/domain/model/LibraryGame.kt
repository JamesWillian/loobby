package app.loobby.feature.library.domain.model

import app.loobby.feature.gameplays.domain.model.Platform
import kotlin.time.Instant

/**
 * Um jogo da biblioteca da pessoa. Só jogo do catálogo (RAWG): [catalogId] é o id
 * de lá. Ninguém mais vê a biblioteca; os outros só veem o match, nas gameplays.
 */
data class LibraryGame(
    val catalogId: String,
    val name: String,
    val coverUrl: String?,
    /** Onde o jogo existe, pelo catálogo. Vazia: o catálogo não diz. */
    val availablePlatforms: List<Platform>,
    /** O "tenho o jogo". Vazio: o jogo só está na biblioteca. */
    val ownedPlatforms: Set<Platform>,
    val addedAt: Instant,
) {
    val isOwned: Boolean get() = ownedPlatforms.isNotEmpty()

    /**
     * As plataformas que a pessoa pode marcar: as do catálogo primeiro, depois as
     * outras. Valem todas, porque o catálogo às vezes vem incompleto.
     */
    val platformChoices: List<Platform>
        get() = availablePlatforms + Platform.entries.filterNot { it in availablePlatforms }
}
