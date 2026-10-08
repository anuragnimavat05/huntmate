package com.huntmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.huntmate.app.data.model.DiscoveryFilters
import com.huntmate.app.data.model.MatchResult
import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.domain.repository.DiscoveryRepository
import com.huntmate.app.domain.repository.UserRepository
import com.huntmate.app.domain.usecase.CompatibilityScorer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class FirebaseDiscoveryRepository(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val compatibilityScorer: CompatibilityScorer
) : DiscoveryRepository {

    override fun observeMatches(currentUserId: String, filters: DiscoveryFilters): Flow<List<MatchResult>> = callbackFlow {
        val registration = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    launch {
                        val currentUser = userRepository.getProfile(currentUserId) ?: TravelProfile(userId = currentUserId)
                        val candidates = snapshot?.documents.orEmpty()
                            .mapNotNull { document -> document.toObject(TravelProfile::class.java) }
                            .filterNot { profile ->
                                profile.userId == currentUserId || currentUser.blockedUserIds.contains(profile.userId)
                            }
                            .filter { profile -> passesFilters(profile, filters) }
                            .map { compatibilityScorer.score(currentUser, it, filters) }
                            .sortedByDescending { it.score }
                        trySend(candidates).isSuccess
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    private fun passesFilters(profile: TravelProfile, filters: DiscoveryFilters): Boolean {
        val cityMatches = filters.cityQuery.isBlank() ||
            profile.homeCity.contains(filters.cityQuery, ignoreCase = true) ||
            profile.destinationCity.contains(filters.cityQuery, ignoreCase = true)
        val interestMatches = filters.interestQuery.isBlank() ||
            profile.interests.any { it.contains(filters.interestQuery, ignoreCase = true) }
        val languageMatches = filters.languageQuery.isBlank() ||
            profile.languages.any { it.contains(filters.languageQuery, ignoreCase = true) }
        val budgetMatches = filters.budget == null || profile.budget == filters.budget
        val styleMatches = filters.travelStyle == null || profile.travelStyle == filters.travelStyle
        val tripStatusMatches = filters.tripStatus == null || profile.tripStatus == filters.tripStatus
        return cityMatches && interestMatches && languageMatches && budgetMatches && styleMatches && tripStatusMatches
    }
}
