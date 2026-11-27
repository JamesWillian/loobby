package app.loobby.feature.gameplays.domain.usecase

import app.loobby.feature.gameplays.domain.repository.GameplaysRepository
import kotlinx.coroutines.flow.Flow

/**
 * O id de cada gameplay que saiu deste aparelho (excluída, ou a pessoa saiu ou
 * perdeu o acesso), para as listas abertas a tirarem na hora.
 */
class ObserveRemovedGameplaysUseCase(private val repository: GameplaysRepository) {
    operator fun invoke(): Flow<String> = repository.removals
}
