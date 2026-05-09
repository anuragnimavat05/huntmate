package com.huntmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.huntmate.app.data.model.Chat
import com.huntmate.app.data.model.Message
import com.huntmate.app.domain.repository.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseChatRepository(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun observeChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val registration = firestore.collection(CHATS)
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val chats = snapshot?.documents.orEmpty()
                        .mapNotNull { document -> document.toObject(Chat::class.java)?.copy(id = document.id) }
                        .sortedByDescending { it.lastMessageAt }
                    trySend(chats).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val registration = firestore.collection(CHATS).document(chatId).collection(MESSAGES)
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val messages = snapshot?.documents.orEmpty()
                        .mapNotNull { document -> document.toObject(Message::class.java)?.copy(id = document.id) }
                    trySend(messages).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getOrCreateChat(participantIds: List<String>): String {
        val existing = firestore.collection(CHATS)
            .whereEqualTo("participantIds", participantIds.sorted())
            .get()
            .await()
            .documents
            .firstOrNull()
        if (existing != null) return existing.id

        val chatRef = firestore.collection(CHATS).document()
        chatRef.set(Chat(id = chatRef.id, participantIds = participantIds.sorted())).await()
        return chatRef.id
    }

    override suspend fun sendMessage(chatId: String, message: Message) {
        val chatDoc = firestore.collection(CHATS).document(chatId)
        val messageDoc = chatDoc.collection(MESSAGES).document()
        firestore.runTransaction { transaction ->
            val chat = transaction.get(chatDoc).toObject(Chat::class.java)
            transaction.set(messageDoc, message.copy(id = messageDoc.id, chatId = chatId))
            transaction.set(
                chatDoc,
                mapOf(
                    "id" to chatId,
                    "participantIds" to (chat?.participantIds ?: emptyList<String>()),
                    "lastMessage" to message.content,
                    "lastMessageAt" to message.sentAt
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }.await()
    }

    private companion object {
        const val CHATS = "chats"
        const val MESSAGES = "messages"
    }
}
