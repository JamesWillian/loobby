package app.loobby.feature.groups.presentation

import app.loobby.core.preferences.UserPreferencesRepository
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.domain.usecase.ListMyFeedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Responsável APENAS pelo feed da sidebar (grupos + eventos instantâneos):
 *  - Carrega o feed da API.
 *  - Mantém o item atualmente selecionado.
 *  - Persiste a seleção em [UserPreferencesRepository] para restaurar no próximo boot.
 *  - Limpa a seleção apenas em logout real (não em cada emissão do sessionFlow).
 */
class FeedViewModel(
    private val listMyFeedUseCase: ListMyFeedUseCase,
    private val prefs: UserPreferencesRepository,
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            // Guarda o userId anterior para detectar transições (login vs. logout).
            var previousUserId: String? = null
            var initialized = false

            authRepository.sessionFlow
                .map { it?.userId }
                .distinctUntilChanged()
                .collect { userId ->
                    val isLogout = initialized && previousUserId != null && userId == null
                    val isUserSwitch = initialized && previousUserId != null &&
                            userId != null && userId != previousUserId

                    // Só limpa prefs em logout real ou troca de usuário.
                    // No boot inicial (ou retorno do mesmo usuário) mantemos a seleção salva.
                    if (isLogout || isUserSwitch) {
                        prefs.clearLastSelectedFeedItem()
                        _uiState.value = FeedUiState(isLoading = false)
                    }

                    if (userId != null) {
                        refreshFeed(userId)
                    } else if (isLogout) {
                        _uiState.value = FeedUiState(isLoading = false)
                    }

                    previousUserId = userId
                    initialized = true
                }
        }
    }

    /** Leitura síncrona do último item selecionado — usado para computar a rota inicial do app. */
    fun getLastSelectedFeedId(): String? = prefs.getLastSelectedFeedId()
    fun getLastSelectedFeedType(): String? = prefs.getLastSelectedFeedType()

    /**
     * Recarrega o feed da API. Após carregar, restaura a seleção salva (se ainda existir
     * na nova lista) ou cai no primeiro item como fallback.
     */
    fun refreshFeed(userId: String? = null) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val uid = userId ?: authRepository.sessionFlow.first()?.userId ?: ""
                val feed = if (uid.isNotBlank()) listMyFeedUseCase(uid) else emptyList()
                _uiState.update { it.copy(feed = feed, isLoading = false) }
                restoreLastSelectedOrFallback(feed.map { it.id })
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Erro ao carregar feed"
                    )
                }
            }
        }
    }

    /**
     * Seleciona um item do feed e persiste a escolha.
     * Não carrega dados do grupo — isso é responsabilidade do [GroupsViewModel].
     */
    fun selectFeedItem(id: String, type: FeedType) {
        val typeStr = if (type == FeedType.EVENT) "EVENT" else "GROUP"
        prefs.saveLastSelectedFeedItem(id, typeStr)

        _uiState.update {
            it.copy(
                selectedFeedId = id,
                selectedFeedType = type
            )
        }
    }

    /** Limpa a seleção persistida e em memória (ex: usuário saiu de um grupo). */
    fun clearSelection() {
        prefs.clearLastSelectedFeedItem()
        _uiState.update { it.copy(selectedFeedId = null, selectedFeedType = null) }
    }

    private fun restoreLastSelectedOrFallback(availableIds: List<String>) {
        val lastId = prefs.getLastSelectedFeedId()
        val lastType = prefs.getLastSelectedFeedType()

        when {
            lastId != null && lastId in availableIds -> {
                val feedType = if (lastType == "EVENT") FeedType.EVENT else FeedType.GROUP
                selectFeedItem(lastId, feedType)
            }
            availableIds.isNotEmpty() -> {
                val first = _uiState.value.feed.firstOrNull() ?: return
                selectFeedItem(first.id, first.entryType)
            }
            else -> {
                // feed vazio → sem seleção
                _uiState.update { it.copy(selectedFeedId = null, selectedFeedType = null) }
            }
        }
    }
}
