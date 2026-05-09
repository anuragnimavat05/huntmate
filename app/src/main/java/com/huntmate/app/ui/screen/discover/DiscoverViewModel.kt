package com.huntmate.app.ui.screen.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huntmate.app.data.model.Connection
import com.huntmate.app.data.model.ConnectionStatus
import com.huntmate.app.data.model.DiscoveryFilters
import com.huntmate.app.data.model.MatchResult
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.ConnectionRepository
import com.huntmate.app.domain.repository.DiscoveryRepository
import com.huntmate.app.domain.repository.SendConnectionOutcome
import com.huntmate.app.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DiscoverConnectionState {
    AVAILABLE,
    PENDING,
    CONNECTED
}

data class DiscoverCard(
    val match: MatchResult,
    val travelerName: String,
    val travelerBio: String,
    val connectionState: DiscoverConnectionState
)

data class DiscoverUiState(
    val filters: DiscoveryFilters = DiscoveryFilters(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class DiscoverViewModel(
    private val authRepository: AuthRepository,
    private val discoveryRepository: DiscoveryRepository,
    private val connectionRepository: ConnectionRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val currentUserId = authRepository.currentUser?.uid.orEmpty()
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val discoverCards: StateFlow<List<DiscoverCard>> = _uiState
        .flatMapLatest { state ->
            combine(
                discoveryRepository.observeMatches(currentUserId, state.filters),
                connectionRepository.observeConnections(currentUserId)
            ) { matches, connections ->
                buildDiscoverCards(matches, connections)
            }
        }
        .catch { throwable ->
            _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateCity(value: String) {
        _uiState.value = _uiState.value.copy(filters = _uiState.value.filters.copy(cityQuery = value))
    }

    fun updateInterest(value: String) {
        _uiState.value = _uiState.value.copy(filters = _uiState.value.filters.copy(interestQuery = value))
    }

    fun updateLanguage(value: String) {
        _uiState.value = _uiState.value.copy(filters = _uiState.value.filters.copy(languageQuery = value))
    }

    fun sendConnection(card: DiscoverCard) {
        viewModelScope.launch {
            runCatching {
                val currentUserId = authRepository.currentUser?.uid ?: error("Sign in required")
                connectionRepository.sendConnectionRequest(
                    com.huntmate.app.data.model.Connection(
                        requesterId = currentUserId,
                        recipientId = card.match.profile.userId,
                        status = ConnectionStatus.PENDING,
                        compatibilityScore = card.match.score,
                        reasons = card.match.reasons
                    )
                )
            }.onSuccess { outcome ->
                _uiState.value = _uiState.value.copy(
                    successMessage = when (outcome) {
                        SendConnectionOutcome.CREATED -> "Connection request sent"
                        SendConnectionOutcome.ALREADY_EXISTS -> "Connection request already exists"
                    },
                    errorMessage = null
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
            }
        }
    }

    fun blockTraveler(userId: String) {
        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: return@launch
            runCatching { userRepository.blockUser(currentUserId, userId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Traveler blocked", errorMessage = null)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                }
        }
    }

    fun reportTraveler(userId: String) {
        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: return@launch
            runCatching { userRepository.reportUser(currentUserId, userId, "Profile reported from discovery") }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Traveler reported", errorMessage = null)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                }
        }
    }

    private suspend fun buildDiscoverCards(
        matches: List<MatchResult>,
        connections: List<Connection>
    ): List<DiscoverCard> {
        return matches.map { match ->
            val profile = userRepository.getProfile(match.profile.userId)
            val relatedConnection = connections.firstOrNull {
                it.requesterId == match.profile.userId || it.recipientId == match.profile.userId
            }
            DiscoverCard(
                match = match,
                travelerName = profile?.name?.ifBlank { match.profile.name }.orEmpty().ifBlank { "Traveler" },
                travelerBio = profile?.bio?.ifBlank { match.profile.bio }.orEmpty()
                    .ifBlank { "Ready for a new journey." },
                connectionState = when (relatedConnection?.status) {
                    ConnectionStatus.PENDING -> DiscoverConnectionState.PENDING
                    ConnectionStatus.ACCEPTED -> DiscoverConnectionState.CONNECTED
                    else -> DiscoverConnectionState.AVAILABLE
                }
            )
        }
    }
}

class DiscoverViewModelFactory(
    private val authRepository: AuthRepository,
    private val discoveryRepository: DiscoveryRepository,
    private val connectionRepository: ConnectionRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DiscoverViewModel(authRepository, discoveryRepository, connectionRepository, userRepository) as T
    }
}
