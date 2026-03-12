package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.data.model.UserMeResponse

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserMeResponse? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Edit fields
    val editUsername: String = "",
    val editDisplayname: String = "",

    // Edit mode
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,

    // Avatar
    val isUploadingAvatar: Boolean = false,

    // Navigation
    val shouldDismiss: Boolean = false
)