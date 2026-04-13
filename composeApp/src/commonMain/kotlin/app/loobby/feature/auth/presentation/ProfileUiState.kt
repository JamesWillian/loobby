package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.data.model.UserMeResponse

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserMeResponse? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showLogoutConfirmation: Boolean = false,

    // Edit fields
    val editUsername: String = "",
    val editDisplayname: String = "",

    // Edit mode
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,

    // Avatar
    val isUploadingAvatar: Boolean = false,

    // Navigation
    val shouldDismiss: Boolean = false,

    // Change password
    val showChangePassword: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isChangingPassword: Boolean = false,
    val changePasswordMessage: String? = null,
    val changePasswordSuccess: Boolean = false,

    // More options menu
    val showMoreOptionsMenu: Boolean = false,

    // Delete account dialog
    val showDeleteAccountDialog: Boolean = false,
    val deleteAccountPassword: String = "",
    val deleteAccountError: String? = null,
    val isDeletingAccount: Boolean = false,
)