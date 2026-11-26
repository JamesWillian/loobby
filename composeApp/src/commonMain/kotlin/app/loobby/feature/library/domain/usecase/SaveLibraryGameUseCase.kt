package app.loobby.feature.library.domain.usecase

import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.library.domain.repository.LibraryRepository

/** Adiciona o jogo à biblioteca da pessoa, ou troca o "tenho o jogo". */
class SaveLibraryGameUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(catalogId: String, owned: Set<Platform>) =
        repository.saveToMyLibrary(catalogId, owned)
}
