package app.loobby.feature.library.domain.usecase

import app.loobby.feature.library.domain.repository.LibraryRepository

class GetMyLibraryUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke() = repository.myLibrary()
}
