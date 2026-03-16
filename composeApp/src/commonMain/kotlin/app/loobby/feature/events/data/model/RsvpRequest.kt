package app.loobby.feature.events.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RsvpRequest(
    val status: String,  // YES, NO, MAYBE, RESERVE
    val isPaid: Boolean = false,
    val obs: String? = null
)