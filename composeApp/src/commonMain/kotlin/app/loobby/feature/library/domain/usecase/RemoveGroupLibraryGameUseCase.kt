package app.loobby.feature.library.domain.usecase

import app.loobby.feature.library.domain.repository.LibraryRepository

class RemoveGroupLibraryGameUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(groupId: String, catalogId: String) =
        repository.removeFromGroupLibrary(groupId, catalogId)
}
