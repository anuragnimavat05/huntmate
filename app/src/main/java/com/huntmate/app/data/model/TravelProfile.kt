package com.huntmate.app.data.model

data class TravelProfile(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val profilePhotoUrl: String = "",
    val homeCity: String = "",
    val destinationCity: String = "",
    val tripStartDate: String = "",
    val tripEndDate: String = "",
    val languages: List<String> = emptyList(),
    val tripGoal: String = "",
    val introMessage: String = "",
    val tripStatus: TripStatus = TripStatus.PLANNING,
    val interests: List<String> = emptyList(),
    val budget: BudgetTier = BudgetTier.MID_RANGE,
    val travelStyle: TravelStyle = TravelStyle.EXPLORER,
    val partnerPreferences: List<String> = emptyList(),
    val onboardingComplete: Boolean = false,
    val blockedUserIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

enum class BudgetTier {
    BUDGET,
    MID_RANGE,
    LUXURY
}

enum class TravelStyle {
    EXPLORER,
    RELAXED,
    ADVENTURE,
    CULTURAL,
    FOODIE
}

enum class TripStatus {
    PLANNING,
    BOOKED,
    IN_DESTINATION
}
