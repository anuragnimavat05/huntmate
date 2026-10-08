package com.huntmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.huntmate.app.data.model.Connection
import com.huntmate.app.data.model.ConnectionStatus
import com.huntmate.app.domain.repository.ConnectionRepository
import com.huntmate.app.domain.repository.SendConnectionOutcome
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

class FirebaseConnectionRepository(
    private val firestore: FirebaseFirestore
) : ConnectionRepository {

    override fun observeConnections(userId: String): Flow<List<Connection>> = combine(
        observeConnectionsByField("requesterId", userId),
        observeConnectionsByField("recipientId", userId)
    ) { requested, received ->
        (requested + received)
            .distinctBy { it.id }
            .sortedByDescending { it.updatedAt }
    }

    private fun observeConnectionsByField(field: String, userId: String): Flow<List<Connection>> = callbackFlow {
        val registration = firestore.collection(CONNECTIONS)
            .whereEqualTo(field, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val connections = snapshot?.documents.orEmpty()
                        .mapNotNull { document -> document.toObject(Connection::class.java)?.copy(id = document.id) }
                    trySend(connections).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun sendConnectionRequest(connection: Connection): SendConnectionOutcome {
        val connectionId = buildConnectionId(connection.requesterId, connection.recipientId)
        val document = firestore.collection(CONNECTIONS).document(connectionId)
        document.set(
            connection.copy(
                id = connectionId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        ).await()
        return SendConnectionOutcome.CREATED
    }

    override suspend fun updateConnectionStatus(connectionId: String, accepted: Boolean) {
        firestore.collection(CONNECTIONS).document(connectionId).update(
            mapOf(
                "status" to if (accepted) ConnectionStatus.ACCEPTED.name else ConnectionStatus.REJECTED.name,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    private companion object {
        const val CONNECTIONS = "connections"
    }

    private fun buildConnectionId(firstUserId: String, secondUserId: String): String {
        return listOf(firstUserId, secondUserId).sorted().joinToString("_")
    }
}
