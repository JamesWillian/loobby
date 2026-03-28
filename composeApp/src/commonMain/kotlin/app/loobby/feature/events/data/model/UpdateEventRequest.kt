package app.loobby.feature.events.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEventRequest(
    val name: String? = null,
    val description: String? = null,
    val scheduledDatetime: String? = null,                // ISO-8601 UTC
    val gameplay: CreateGameplayDetailsRequest? = null,    // reutiliza o DTO de criação
    val sport: CreateSportDetailsRequest? = null,          // reutiliza o DTO de criação
    val clearDescription: Boolean = false                  // true → remove a descrição existente
)