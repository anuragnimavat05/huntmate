package com.huntmate.app.domain.repository

import com.huntmate.app.data.model.Connection
import kotlinx.coroutines.flow.Flow

enum class SendConnectionOutcome {
    CREATED,
    ALREADY_EXISTS
}

interface ConnectionRepository {
    fun observeConnections(userId: String): Flow<List<Connection>>
    suspend fun sendConnectionRequest(connection: Connection): SendConnectionOutcome
    suspend fun updateConnectionStatus(connectionId: String, accepted: Boolean)
}
