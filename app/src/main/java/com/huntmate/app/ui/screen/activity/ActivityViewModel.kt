package com.huntmate.app.ui.screen.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huntmate.app.data.model.ConnectionStatus
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.ChatRepository
import com.huntmate.app.domain.repository.ConnectionRepository
import com.huntmate.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ActivityViewModel(
    authRepository: AuthRepository,
    connectionRepository: ConnectionRepository,
    chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val currentUserId = authRepository.currentUser?.uid.orEmpty()

    val items = combine(
        connectionRepository.observeConnections(currentUserId),
        chatRepository.observeChats(currentUserId)
    ) { connections, chats ->
        val connectionItems = connections.map { connection ->
            val otherUserId = if (connection.requesterId == currentUserId) connection.recipientId else connection.requesterId
            val name = userRepository.getProfile(otherUserId)?.name?.ifBlank { "Traveler" } ?: "Traveler"
            ActivityItem(
                id = "connection_${connection.id}",
                title = when (connection.status) {
                    ConnectionStatus.PENDING -> "Connection request"
                    ConnectionStatus.ACCEPTED -> "Match accepted"
                    ConnectionStatus.REJECTED -> "Request declined"
                },
                body = when (connection.status) {
                    ConnectionStatus.PENDING -> "$name is waiting in your connections list."
                    ConnectionStatus.ACCEPTED -> "You and $name can chat now."
                    ConnectionStatus.REJECTED -> "$name is no longer available for this request."
                }
            )
        }

        val chatItems = chats.filter { it.lastMessage.isNotBlank() }.map { chat ->
            val otherUserId = chat.participantIds.firstOrNull { it != currentUserId }.orEmpty()
            val name = userRepository.getProfile(otherUserId)?.name?.ifBlank { "Traveler" } ?: "Traveler"
            ActivityItem(
                id = "chat_${chat.id}",
                title = "New chat activity",
                body = "$name: ${chat.lastMessage}"
            )
        }

        (connectionItems + chatItems).sortedBy { it.id }.reversed()
    }.catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class ActivityViewModelFactory(
    private val authRepository: AuthRepository,
    private val connectionRepository: ConnectionRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActivityViewModel(authRepository, connectionRepository, chatRepository, userRepository) as T
    }
}
