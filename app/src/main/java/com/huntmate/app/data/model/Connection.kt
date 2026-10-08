package com.huntmate.app.data.model

enum class ConnectionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class Connection(
    val id: String = "",
    val requesterId: String = "",
    val recipientId: String = "",
    val status: ConnectionStatus = ConnectionStatus.PENDING,
    val compatibilityScore: Int = 0,
    val reasons: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
