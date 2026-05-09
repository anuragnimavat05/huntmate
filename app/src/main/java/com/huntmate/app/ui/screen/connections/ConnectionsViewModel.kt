package com.huntmate.app.ui.screen.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huntmate.app.data.model.Connection
import com.huntmate.app.data.model.ConnectionStatus
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.ChatRepository
import com.huntmate.app.domain.repository.ConnectionRepository
import com.huntmate.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConnectionCard(
    val connection: Connection,
    val travelerName: String,
    val travelerSubtitle: String,
    val initials: String
)

class ConnectionsViewModel(
    private val authRepository: AuthRepository,
    private val connectionRepository: ConnectionRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val currentUserId = authRepository.currentUser?.uid.orEmpty()

    val connections = connectionRepository.observeConnections(currentUserId)
        .map { connections ->
            connections.map { connection ->
                val otherUserId = if (connection.requesterId == currentUserId) {
                    connection.recipientId
                } else {
                    connection.requesterId
                }
                val otherProfile = userRepository.getProfile(otherUserId)
                val travelerName = otherProfile?.name?.ifBlank { "Traveler" } ?: "Traveler"
                val subtitle = buildString {
                    append("Compatibility ${connection.compatibilityScore}%")
                    if (otherProfile?.destinationCity?.isNotBlank() == true) {
                        append(" | ${otherProfile.destinationCity}")
                    }
                }
                ConnectionCard(
                    connection = connection,
                    travelerName = travelerName,
                    travelerSubtitle = subtitle,
                    initials = travelerName.take(1).uppercase()
                )
            }
        }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun accept(connection: Connection, onChatReady: (String) -> Unit) {
        viewModelScope.launch {
            connectionRepository.updateConnectionStatus(connection.id, accepted = true)
            val chatId = chatRepository.getOrCreateChat(listOf(connection.requesterId, connection.recipientId))
            onChatReady(chatId)
        }
    }

    fun reject(connectionId: String) {
        viewModelScope.launch { connectionRepository.updateConnectionStatus(connectionId, accepted = false) }
    }

    fun openAcceptedChat(connection: Connection, onChatReady: (String) -> Unit) {
        if (connection.status != ConnectionStatus.ACCEPTED) return
        viewModelScope.launch {
            val chatId = chatRepository.getOrCreateChat(listOf(connection.requesterId, connection.recipientId))
            onChatReady(chatId)
        }
    }
}

class ConnectionsViewModelFactory(
    private val authRepository: AuthRepository,
    private val connectionRepository: ConnectionRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConnectionsViewModel(authRepository, connectionRepository, chatRepository, userRepository) as T
    }
}
