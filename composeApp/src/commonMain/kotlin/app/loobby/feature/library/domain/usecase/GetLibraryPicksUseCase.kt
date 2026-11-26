package app.loobby.feature.library.domain.usecase

import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.library.domain.model.LibrarySource
import app.loobby.feature.library.domain.repository.LibraryRepository

/** Os jogos de uma biblioteca, prontos para entrar numa gameplay sem buscar. */
class GetLibraryPicksUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(source: LibrarySource): List<GameChoice> = when (source) {
        LibrarySource.Mine -> repository.myLibrary().map { GameChoice(it.catalogId, it.name, it.coverUrl) }
        is LibrarySource.Group ->
            repository.groupLibrary(source.groupId).map { GameChoice(it.catalogId, it.name, it.coverUrl) }
    }
}
