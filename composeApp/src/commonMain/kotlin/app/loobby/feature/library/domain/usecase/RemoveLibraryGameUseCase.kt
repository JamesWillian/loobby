package app.loobby.feature.library.domain.usecase

import app.loobby.feature.library.domain.repository.LibraryRepository

class RemoveLibraryGameUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(catalogId: String) = repository.removeFromMyLibrary(catalogId)
}
