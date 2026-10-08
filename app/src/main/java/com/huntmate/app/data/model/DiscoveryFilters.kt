package com.huntmate.app.data.model

data class DiscoveryFilters(
    val cityQuery: String = "",
    val interestQuery: String = "",
    val languageQuery: String = "",
    val budget: BudgetTier? = null,
    val travelStyle: TravelStyle? = null,
    val tripStatus: TripStatus? = null,
    val tripStartDate: String = "",
    val tripEndDate: String = ""
)

data class MatchResult(
    val profile: TravelProfile,
    val score: Int,
    val reasons: List<String>
)
