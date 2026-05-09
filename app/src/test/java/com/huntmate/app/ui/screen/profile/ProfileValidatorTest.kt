package com.huntmate.app.ui.screen.profile

import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.domain.usecase.ProfileValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidatorTest {

    private val validator = ProfileValidator()

    @Test
    fun `returns error when required onboarding fields are missing`() {
        assertEquals("Name is required", validator.validate(TravelProfile()))
    }

    @Test
    fun `returns null for complete profile`() {
        val profile = TravelProfile(
            name = "Anya",
            homeCity = "Mumbai",
            destinationCity = "Seoul",
            tripStartDate = "2026-05-10",
            tripEndDate = "2026-05-15",
            interests = listOf("street food", "cafes")
        )

        assertNull(validator.validate(profile))
    }
}
