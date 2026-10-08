package com.huntmate.app.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huntmate.app.data.model.Message
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.ChatRepository
import com.huntmate.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatListItem(
    val chatId: String,
    val title: String,
    val subtitle: String,
    val initials: String
)

data class ChatMessageItem(
    val id: String,
    val senderLabel: String,
    val content: String,
    val isCurrentUser: Boolean,
    val timeLabel: String
)

data class ChatListUiState(
    val chats: List<ChatListItem> = emptyList()
)

data class ChatComposerState(
    val input: String = "",
    val errorMessage: String? = null
)

data class ChatThreadUiState(
    val chatTitle: String = "Conversation",
    val chatSubtitle: String = "Travel chat",
    val messages: List<ChatMessageItem> = emptyList()
)

class ChatListViewModel(
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val currentUserId = authRepository.currentUser?.uid.orEmpty()

    val uiState: StateFlow<ChatListUiState> = chatRepository.observeChats(currentUserId)
        .combine(MutableStateFlow(Unit)) { chats, _ ->
            val items = chats.map { chat ->
                val otherUserId = chat.participantIds.firstOrNull { it != currentUserId }.orEmpty()
                val otherProfile = if (otherUserId.isNotBlank()) userRepository.getProfile(otherUserId) else null
                val title = otherProfile?.name?.takeIf { it.isNotBlank() } ?: "Travel chat"
                val subtitle = chat.lastMessage.ifBlank { "Start the conversation" }
                ChatListItem(
                    chatId = chat.id,
                    title = title,
                    subtitle = subtitle,
                    initials = title.take(1).uppercase()
                )
            }
            ChatListUiState(chats = items)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatListUiState())
}

class ChatListViewModelFactory(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListViewModel(authRepository, chatRepository, userRepository) as T
    }
}

class ChatThreadViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val chatId: String
) : ViewModel() {
    private val currentUserId = authRepository.currentUser?.uid.orEmpty()

    val uiState: StateFlow<ChatThreadUiState> = combine(
        chatRepository.observeChats(currentUserId),
        chatRepository.observeMessages(chatId)
    ) { chats, messages ->
        val chat = chats.firstOrNull { it.id == chatId }
        val otherUserId = chat?.participantIds?.firstOrNull { it != currentUserId }.orEmpty()
        val otherProfile = if (otherUserId.isNotBlank()) userRepository.getProfile(otherUserId) else null
        val title = otherProfile?.name?.takeIf { it.isNotBlank() } ?: "Conversation"
        val subtitle = otherProfile?.destinationCity?.takeIf { it.isNotBlank() } ?: "Travel chat"

        val mappedMessages = messages.map { message ->
            val isCurrentUser = message.senderId == currentUserId
            val senderLabel = if (isCurrentUser) {
                "You"
            } else {
                otherProfile?.name?.takeIf { it.isNotBlank() } ?: "Traveler"
            }
            ChatMessageItem(
                id = message.id,
                senderLabel = senderLabel,
                content = message.content,
                isCurrentUser = isCurrentUser,
                timeLabel = formatTime(message.sentAt)
            )
        }

        ChatThreadUiState(
            chatTitle = title,
            chatSubtitle = subtitle,
            messages = mappedMessages
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatThreadUiState())

    private val _composerState = MutableStateFlow(ChatComposerState())
    val composerState: StateFlow<ChatComposerState> = _composerState.asStateFlow()

    fun updateInput(value: String) {
        _composerState.value = _composerState.value.copy(input = value, errorMessage = null)
    }

    fun sendMessage() {
        viewModelScope.launch {
            runCatching {
                val currentUser = authRepository.currentUser ?: error("Not signed in")
                val content = composerState.value.input.trim()
                if (content.isBlank()) error("Write a message first")
                chatRepository.sendMessage(
                    chatId = chatId,
                    message = Message(
                        chatId = chatId,
                        senderId = currentUser.uid,
                        content = content
                    )
                )
            }.onSuccess {
                _composerState.value = ChatComposerState()
            }.onFailure { throwable ->
                _composerState.value = _composerState.value.copy(errorMessage = throwable.message)
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

class ChatThreadViewModelFactory(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val chatId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatThreadViewModel(authRepository, chatRepository, userRepository, chatId) as T
    }
}
