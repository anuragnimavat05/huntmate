package com.huntmate.app.domain.repository

import com.huntmate.app.data.model.Chat
import com.huntmate.app.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(userId: String): Flow<List<Chat>>
    fun observeMessages(chatId: String): Flow<List<Message>>
    suspend fun getOrCreateChat(participantIds: List<String>): String
    suspend fun sendMessage(chatId: String, message: Message)
}
