package ua.kpi.transformermonitoringsystem.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.kpi.transformermonitoringsystem.data.TransformerRepository
import ua.kpi.transformermonitoringsystem.data.model.AuthCredentials
import ua.kpi.transformermonitoringsystem.data.model.UpdateUser
import ua.kpi.transformermonitoringsystem.data.model.UserProfile
import ua.kpi.transformermonitoringsystem.network.ErrorHandler
import java.net.UnknownHostException
import javax.inject.Inject

sealed interface AuthUiState {
    data object Initializing : AuthUiState
    data object Unauthenticated : AuthUiState
    data object Authenticated : AuthUiState
}

// ViewModel для управлыння акаунтом
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: TransformerRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val appContext = context.applicationContext
    val authState: StateFlow<AuthUiState> = repository.accessTokenFlow
        .map { token ->
            if (token.isNullOrBlank()) AuthUiState.Unauthenticated else AuthUiState.Authenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthUiState.Initializing
        )

    val userRole: StateFlow<String> = repository.roleFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "GUEST"
        )

    val username: StateFlow<String?> = repository.loginFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(loginInput: String, passwordInput: String) {
        val cleanLogin = loginInput.trim()
        if (cleanLogin.isEmpty() || passwordInput.isEmpty()) {
            _errorMessage.value = "Логін та пароль не можуть бути порожніми"
            return
        }
        if (!ErrorHandler.isInternetAvailable(appContext)) {
            _errorMessage.value = ErrorHandler.handleException(appContext, UnknownHostException())
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.login(AuthCredentials(login = cleanLogin, password = passwordInput))
                .onSuccess {
                    loadUserProfile()
                }
                .onFailure { e ->
                    _errorMessage.value = ErrorHandler.handleException(appContext, e)
                }
            _isLoading.value = false
        }
    }

    fun register(loginInput: String, passwordInput: String) {
        val cleanLogin = loginInput.trim()
        if (cleanLogin.length < 3) {
            _errorMessage.value = "Логін має містити щонайменше 3 символи"
            return
        }
        if (passwordInput.length < 6) {
            _errorMessage.value = "Пароль повинен містити щонайменше 6 символів"
            return
        }
        if (!ErrorHandler.isInternetAvailable(appContext)) {
            _errorMessage.value = ErrorHandler.handleException(appContext, UnknownHostException())
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.register(AuthCredentials(login = cleanLogin, password = passwordInput))
                .onSuccess {
                    loadUserProfile()
                }
                .onFailure { e ->
                    _errorMessage.value = ErrorHandler.handleException(appContext, e)
                }
            _isLoading.value = false
        }
    }

    fun loadUserProfile() {
        if (!ErrorHandler.isInternetAvailable(appContext)) return
        viewModelScope.launch {
            repository.getCurrentUserProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                }
        }
    }

    fun updateProfile(
        currentPassword: String,
        newLogin: String?,
        newPassword: String?,
        onSuccess: () -> Unit
    ) {
        val cleanLogin = newLogin?.trim()?.takeIf { it.isNotEmpty() }
        val cleanPassword = newPassword?.takeIf { it.isNotEmpty() }

        if (cleanLogin == null && cleanPassword == null) {
            _errorMessage.value = "Вкажіть новий логін або новий пароль"
            return
        }
        if (currentPassword.isBlank()) {
            _errorMessage.value = "Введіть поточний пароль для підтвердження"
            return
        }
        if (cleanPassword != null && cleanPassword.length < 6) {
            _errorMessage.value = "Новий пароль повинен містити щонайменше 6 символів"
            return
        }
        if (!ErrorHandler.isInternetAvailable(appContext)) {
            _errorMessage.value = ErrorHandler.handleException(appContext, UnknownHostException())
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.updateProfile(
                UpdateUser(
                    currentPassword = currentPassword,
                    login = cleanLogin,
                    password = cleanPassword
                )
            ).onSuccess { updated ->
                _userProfile.value = updated
                onSuccess()
            }.onFailure { e ->
                _errorMessage.value = ErrorHandler.handleException(appContext, e)
            }
            _isLoading.value = false
        }
    }

    fun changeRoleToAdmin(secretKey: String, onSuccess: () -> Unit) {
        if (secretKey.isBlank()) {
            _errorMessage.value = "Введіть секретний ключ адміністратора"
            return
        }
        if (!ErrorHandler.isInternetAvailable(appContext)) {
            _errorMessage.value = ErrorHandler.handleException(appContext, UnknownHostException())
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.changeRoleToAdmin(secretKey.trim())
                .onSuccess {
                    loadUserProfile()
                    onSuccess()
                }
                .onFailure { e ->
                    _errorMessage.value = ErrorHandler.handleException(appContext, e)
                }
            _isLoading.value = false
        }
    }

    fun changeRoleToUser(onSuccess: () -> Unit = {}) {
        if (!ErrorHandler.isInternetAvailable(appContext)) {
            _errorMessage.value = ErrorHandler.handleException(appContext, UnknownHostException())
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.changeRoleToUser()
                .onSuccess {
                    loadUserProfile()
                    onSuccess()
                }
                .onFailure { e ->
                    _errorMessage.value = ErrorHandler.handleException(appContext, e)
                }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.logout()
            _userProfile.value = null
            _errorMessage.value = null
            _isLoading.value = false
        }
    }

    fun deleteAccount() {
        if (!ErrorHandler.isInternetAvailable(appContext)) {
            _errorMessage.value = ErrorHandler.handleException(appContext, UnknownHostException())
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.deleteProfile()
                .onSuccess {
                    _userProfile.value = null
                }
                .onFailure { e ->
                    _errorMessage.value = ErrorHandler.handleException(appContext, e)
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}