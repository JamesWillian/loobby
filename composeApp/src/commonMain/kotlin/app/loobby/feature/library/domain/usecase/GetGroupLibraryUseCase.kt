package app.loobby.feature.library.domain.usecase

import app.loobby.feature.library.domain.repository.LibraryRepository

class GetGroupLibraryUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(groupId: String) = repository.groupLibrary(groupId)
}
