package app.loobby.feature.auth.presentation

import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.auth.domain.usecase.GetProfileUseCase
import app.loobby.feature.auth.domain.usecase.InitializeAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.LoginUseCase
import app.loobby.feature.auth.domain.usecase.RegisterUseCase
import app.loobby.feature.auth.domain.usecase.UpdateProfileUseCase
import app.loobby.feature.auth.domain.usecase.UploadAvatarUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val initializeAnonymousUseCase: InitializeAnonymousUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val repo: AuthRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(AuthUiState(isLoading = true))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            try {
                val session = initializeAnonymousUseCase()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    session = session,
                    errorMessage = null
                )
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun login(email: String, password: String) {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val session = loginUseCase(email, password)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    session = session
                )
                loadProfile()
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun register(email: String, password: String) {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val session = registerUseCase(email, password)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    session = session
                )
                loadProfile()
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun loadProfile() {
        scope.launch {
            try {
                val profile = getProfileUseCase()
                _uiState.value = _uiState.value.copy(profile = profile)
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateProfile(username: String?, displayname: String?) {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val profile = updateProfileUseCase(username, displayname)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile
                )
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun uploadAvatar(fileName: String, bytes: ByteArray, contentType: String) {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val profile = uploadAvatarUseCase(fileName, bytes, contentType)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile
                )
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}