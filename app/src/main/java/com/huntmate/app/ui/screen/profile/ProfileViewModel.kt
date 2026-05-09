package com.huntmate.app.ui.screen.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huntmate.app.data.model.BudgetTier
import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.data.model.TripStatus
import com.huntmate.app.data.model.TravelStyle
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.MediaRepository
import com.huntmate.app.domain.repository.UserRepository
import com.huntmate.app.domain.usecase.ProfileValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: TravelProfile = TravelProfile(),
    val isSaving: Boolean = false,
    val completionPercent: Int = 0,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mediaRepository: MediaRepository,
    private val profileValidator: ProfileValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val currentUserId = authRepository.currentUser?.uid
        if (currentUserId != null) {
            viewModelScope.launch {
                userRepository.observeProfile(currentUserId)
                    .catch { throwable ->
                        _uiState.update {
                            it.copy(
                                errorMessage = throwable.message
                                    ?: "Unable to observe profile. Check Firestore setup."
                            )
                        }
                    }
                    .collect { profile ->
                        if (profile != null) {
                            _uiState.update {
                                it.copy(profile = profile, completionPercent = completionPercent(profile))
                            }
                        }
                    }
            }
        }
    }

    fun prime(profile: TravelProfile) {
        _uiState.update { current ->
            if (current.profile.userId.isBlank()) current.copy(profile = profile) else current
        }
    }

    fun updateName(value: String) = updateProfile { copy(name = value) }
    fun updateBio(value: String) = updateProfile { copy(bio = value) }
    fun updateHomeCity(value: String) = updateProfile { copy(homeCity = value) }
    fun updateDestinationCity(value: String) = updateProfile { copy(destinationCity = value) }
    fun updateTripStartDate(value: String) = updateProfile { copy(tripStartDate = value) }
    fun updateTripEndDate(value: String) = updateProfile { copy(tripEndDate = value) }
    fun updateLanguages(value: String) = updateProfile { copy(languages = value.split(",").map(String::trim).filter(String::isNotBlank)) }
    fun updateTripGoal(value: String) = updateProfile { copy(tripGoal = value) }
    fun updateIntroMessage(value: String) = updateProfile { copy(introMessage = value) }
    fun updateTripStatus(value: TripStatus) = updateProfile { copy(tripStatus = value) }
    fun updateInterests(value: String) = updateProfile { copy(interests = value.split(",").map(String::trim).filter(String::isNotBlank)) }
    fun updatePartnerPreferences(value: String) = updateProfile { copy(partnerPreferences = value.split(",").map(String::trim).filter(String::isNotBlank)) }
    fun updateBudget(value: BudgetTier) = updateProfile { copy(budget = value) }
    fun updateTravelStyle(value: TravelStyle) = updateProfile { copy(travelStyle = value) }

    fun uploadPhoto(uri: Uri) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { mediaRepository.uploadProfilePhoto(userId, uri) }
                .onSuccess { url -> updateProfile { copy(profilePhotoUrl = url) } }
                .onFailure { throwable -> _uiState.update { it.copy(errorMessage = throwable.message) } }
        }
    }

    fun save(onboardingComplete: Boolean = false, onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, successMessage = null, errorMessage = null) }
            runCatching {
                val currentUser = authRepository.currentUser ?: error("User not signed in")
                val profile = uiState.value.profile.copy(
                    userId = currentUser.uid,
                    email = currentUser.email.orEmpty(),
                    onboardingComplete = onboardingComplete || uiState.value.profile.onboardingComplete
                )
                profileValidator.validate(profile)?.let { error(it) }
                userRepository.saveProfile(profile)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = if (onboardingComplete) "Onboarding complete" else "Profile updated"
                    )
                }
                onSaved?.invoke()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSaving = false, errorMessage = throwable.message) }
            }
        }
    }

    private fun updateProfile(transform: TravelProfile.() -> TravelProfile) {
        _uiState.update {
            val updated = it.profile.transform()
            it.copy(
                profile = updated,
                completionPercent = completionPercent(updated),
                successMessage = null,
                errorMessage = null
            )
        }
    }

    private fun completionPercent(profile: TravelProfile): Int {
        val checks = listOf(
            profile.name.isNotBlank(),
            profile.bio.isNotBlank(),
            profile.homeCity.isNotBlank(),
            profile.destinationCity.isNotBlank(),
            profile.tripStartDate.isNotBlank(),
            profile.tripEndDate.isNotBlank(),
            profile.interests.isNotEmpty(),
            profile.languages.isNotEmpty(),
            profile.tripGoal.isNotBlank(),
            profile.introMessage.isNotBlank()
        )
        return ((checks.count { it }.toFloat() / checks.size) * 100).toInt()
    }
}

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mediaRepository: MediaRepository,
    private val profileValidator: ProfileValidator
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(authRepository, userRepository, mediaRepository, profileValidator) as T
    }
}
