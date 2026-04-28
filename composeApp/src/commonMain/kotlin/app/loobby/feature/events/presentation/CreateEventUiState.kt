package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.EventType

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
    val gameName: String = "",
    val gameId: String = "",

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