package com.huntmate.app.domain.usecase

import com.huntmate.app.data.model.TravelProfile

class ProfileValidator {
    fun validate(profile: TravelProfile): String? {
        return when {
            profile.name.isBlank() -> "Name is required"
            profile.homeCity.isBlank() -> "Home city is required"
            profile.destinationCity.isBlank() -> "Destination city is required"
            profile.tripStartDate.isBlank() || profile.tripEndDate.isBlank() -> "Trip dates are required"
            profile.interests.isEmpty() -> "Add at least one interest"
            profile.languages.isEmpty() -> "Add at least one language"
            profile.tripGoal.isBlank() -> "Add your trip goal"
            else -> null
        }
    }
}
