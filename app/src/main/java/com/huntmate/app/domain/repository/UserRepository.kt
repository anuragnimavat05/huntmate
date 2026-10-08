package com.huntmate.app.domain.repository

import com.huntmate.app.data.model.TravelProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getProfile(userId: String): TravelProfile?
    fun observeProfile(userId: String): Flow<TravelProfile?>
    suspend fun saveProfile(profile: TravelProfile)
    suspend fun blockUser(currentUserId: String, blockedUserId: String)
    suspend fun reportUser(currentUserId: String, reportedUserId: String, reason: String)
}
