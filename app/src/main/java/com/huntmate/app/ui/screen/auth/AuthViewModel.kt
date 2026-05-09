package com.huntmate.app.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionState(
    val isLoading: Boolean = true,
    val currentUser: FirebaseUser? = null,
    val profile: TravelProfile? = null,
    val authMode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null
)

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState()
                .catch { throwable ->
                    _sessionState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load authentication state"
                        )
                    }
                }
                .collect { user ->
                    val profile = runCatching {
                        user?.uid?.let { userRepository.getProfile(it) }
                    }.getOrElse { throwable ->
                        _sessionState.update {
                            it.copy(
                                errorMessage = throwable.message
                                    ?: "Unable to load profile. Check Firestore setup."
                            )
                        }
                        null
                    }
                    _sessionState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            profile = profile,
                            errorMessage = it.errorMessage
                        )
                    }
                }
        }
    }

    fun updateEmail(value: String) = _sessionState.update { it.copy(email = value) }

    fun updatePassword(value: String) = _sessionState.update { it.copy(password = value) }

    fun toggleAuthMode() {
        _sessionState.update {
            it.copy(
                authMode = if (it.authMode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN,
                errorMessage = null
            )
        }
    }

    fun submit() {
        viewModelScope.launch {
            _sessionState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val state = sessionState.value
                if (state.authMode == AuthMode.SIGN_IN) {
                    authRepository.signIn(state.email, state.password)
                } else {
                    authRepository.signUp(state.email, state.password)
                }
            }.onFailure { throwable ->
                _sessionState.update {
                    it.copy(isLoading = false, errorMessage = throwable.message ?: "Authentication failed")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            _sessionState.update { it.copy(profile = userRepository.getProfile(userId)) }
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository, userRepository) as T
    }
}
