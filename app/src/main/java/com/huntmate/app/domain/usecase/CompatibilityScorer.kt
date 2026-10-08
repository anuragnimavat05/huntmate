package com.huntmate.app.domain.usecase

import com.huntmate.app.data.model.DiscoveryFilters
import com.huntmate.app.data.model.MatchResult
import com.huntmate.app.data.model.TravelProfile
import kotlin.math.max

class CompatibilityScorer {

    fun score(
        currentUser: TravelProfile,
        candidate: TravelProfile,
        filters: DiscoveryFilters
    ): MatchResult {
        var score = 0
        val reasons = mutableListOf<String>()

        if (currentUser.destinationCity.equals(candidate.destinationCity, ignoreCase = true) &&
            currentUser.destinationCity.isNotBlank()
        ) {
            score += 30
            reasons += "Same destination city"
        }

        if (currentUser.homeCity.equals(candidate.homeCity, ignoreCase = true) &&
            currentUser.homeCity.isNotBlank()
        ) {
            score += 15
            reasons += "Same home city"
        }

        val sharedInterests = currentUser.interests.intersect(candidate.interests.toSet())
        if (sharedInterests.isNotEmpty()) {
            score += minOf(25, sharedInterests.size * 8)
            reasons += "Shared interests: ${sharedInterests.take(3).joinToString()}"
        }

        if (currentUser.budget == candidate.budget) {
            score += 15
            reasons += "Similar budget"
        }

        if (currentUser.travelStyle == candidate.travelStyle) {
            score += 15
            reasons += "Matching travel style"
        }

        val sharedLanguages = currentUser.languages.intersect(candidate.languages.toSet())
        if (sharedLanguages.isNotEmpty()) {
            score += 10
            reasons += "Common language: ${sharedLanguages.first()}"
        }

        if (currentUser.tripStatus == candidate.tripStatus) {
            score += 10
            reasons += "Same trip stage"
        }

        if (currentUser.tripGoal.isNotBlank() &&
            candidate.tripGoal.contains(currentUser.tripGoal, ignoreCase = true)
        ) {
            score += 5
            reasons += "Similar trip goal"
        }

        if (datesOverlap(currentUser.tripStartDate, currentUser.tripEndDate, candidate.tripStartDate, candidate.tripEndDate)) {
            score += 20
            reasons += "Overlapping trip dates"
        }

        if (filters.cityQuery.isNotBlank()) {
            val matchesCity = candidate.homeCity.contains(filters.cityQuery, ignoreCase = true) ||
                candidate.destinationCity.contains(filters.cityQuery, ignoreCase = true)
            score = if (matchesCity) score + 10 else max(0, score - 20)
            if (matchesCity) reasons += "Matches city filter"
        }

        if (filters.interestQuery.isNotBlank() &&
            candidate.interests.any { it.contains(filters.interestQuery, ignoreCase = true) }
        ) {
            score += 5
            reasons += "Matches interest filter"
        }

        if (filters.languageQuery.isNotBlank() &&
            candidate.languages.any { it.contains(filters.languageQuery, ignoreCase = true) }
        ) {
            score += 5
            reasons += "Matches language filter"
        }

        if (filters.budget != null && candidate.budget != filters.budget) {
            score = max(0, score - 20)
        }

        if (filters.travelStyle != null && candidate.travelStyle != filters.travelStyle) {
            score = max(0, score - 20)
        }

        if (filters.tripStatus != null && candidate.tripStatus != filters.tripStatus) {
            score = max(0, score - 15)
        }

        return MatchResult(profile = candidate, score = score.coerceIn(0, 100), reasons = reasons.distinct())
    }

    private fun datesOverlap(startA: String, endA: String, startB: String, endB: String): Boolean {
        if (startA.isBlank() || endA.isBlank() || startB.isBlank() || endB.isBlank()) return false
        return startA <= endB && startB <= endA
    }
}
