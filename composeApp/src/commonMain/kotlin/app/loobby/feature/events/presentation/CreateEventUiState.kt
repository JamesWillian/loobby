package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.games.domain.model.GameDomain

data class CreateEventUiState(
    // Step 1 — type selection
    val selectedType: EventType? = null,

    // Common fields
    val name: String = "",
    val description: String = "",
    val scheduledDate: String = "",  // dígitos brutos "DDMMYYYY" (formatado visualmente por DateTransformation)
    val scheduledTime: String = "",  // dígitos brutos "HHmm" (formatado visualmente por TimeTransformation)

    // SPORT fields
    val durationMinutes: String = "",
    val arena: String = "",
    val pricePerPlayer: String = "",
    val maxPlayers: String = "",
    val acceptReserve: Boolean = false,

    // GAMEPLAY fields
    // gameName/gameId guardam o jogo escolhido (RAWG ou manual) — não são mais
    // editados diretamente no form de detalhes; vêm do Step 2 (seleção de jogo).
    val gameName: String = "",
    val gameId: String = "",

    // Step 2 (GAMEPLAY) — seleção de jogo
    // Quando true, a sheet renderiza a tela de busca/escolha de jogo.
    val gameSelectionVisible: Boolean = false,
    val gameSearchQuery: String = "",
    val gameSearchResults: List<GameDomain> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    // Jogo RAWG escolhido (null quando o nome foi digitado manualmente). Guardado
    // para persistir no cache offline ao criar o evento.
    val selectedGame: GameDomain? = null,
    // Texto do campo de entrada manual de nome de jogo (tabuleiro / indefinido).
    val manualGameName: String = "",

    // Submission state
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,

    // True quando o usuário modificou algum campo após abrir/carregar o formulário.
    // Usado para perguntar se quer descartar antes de fechar o sheet.
    val isDirty: Boolean = false,

    // CHANGED: campos para modo edição
    val isEditMode: Boolean = false,
    val editingEventId: String? = null,
    val editingGroupId: String? = null
)