package com.huntmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getProfile(userId: String): TravelProfile? {
        return firestore.collection(USERS).document(userId).get().await().toObject(TravelProfile::class.java)
    }

    override fun observeProfile(userId: String): Flow<TravelProfile?> = callbackFlow {
        val registration = firestore.collection(USERS).document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.toObject(TravelProfile::class.java)).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun saveProfile(profile: TravelProfile) {
        firestore.collection(USERS).document(profile.userId).set(profile).await()
    }

    override suspend fun blockUser(currentUserId: String, blockedUserId: String) {
        val docRef = firestore.collection(USERS).document(currentUserId)
        firestore.runTransaction { transaction ->
            val profile = transaction.get(docRef).toObject(TravelProfile::class.java) ?: TravelProfile(userId = currentUserId)
            val blocked = (profile.blockedUserIds + blockedUserId).distinct()
            transaction.set(docRef, profile.copy(blockedUserIds = blocked))
        }.await()
    }

    override suspend fun reportUser(currentUserId: String, reportedUserId: String, reason: String) {
        firestore.collection(REPORTS).add(
            mapOf(
                "currentUserId" to currentUserId,
                "reportedUserId" to reportedUserId,
                "reason" to reason,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    private companion object {
        const val USERS = "users"
        const val REPORTS = "reports"
    }
}
