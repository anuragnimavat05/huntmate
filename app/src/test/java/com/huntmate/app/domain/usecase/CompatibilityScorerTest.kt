package com.huntmate.app.domain.usecase

import com.huntmate.app.data.model.BudgetTier
import com.huntmate.app.data.model.DiscoveryFilters
import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.data.model.TravelStyle
import org.junit.Assert.assertTrue
import org.junit.Test

class CompatibilityScorerTest {

    private val scorer = CompatibilityScorer()

    @Test
    fun `score increases for overlapping travel signals`() {
        val currentUser = TravelProfile(
            userId = "a",
            homeCity = "Delhi",
            destinationCity = "Tokyo",
            tripStartDate = "2026-04-10",
            tripEndDate = "2026-04-18",
            interests = listOf("food", "culture"),
            budget = BudgetTier.MID_RANGE,
            travelStyle = TravelStyle.CULTURAL
        )
        val candidate = TravelProfile(
            userId = "b",
            homeCity = "Delhi",
            destinationCity = "Tokyo",
            tripStartDate = "2026-04-12",
            tripEndDate = "2026-04-20",
            interests = listOf("culture", "museums"),
            budget = BudgetTier.MID_RANGE,
            travelStyle = TravelStyle.CULTURAL
        )

        val result = scorer.score(currentUser, candidate, DiscoveryFilters())

        assertTrue(result.score >= 80)
        assertTrue(result.reasons.any { it.contains("destination", ignoreCase = true) })
    }
}
