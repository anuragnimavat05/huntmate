package com.huntmate.app.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.huntmate.app.data.model.BudgetTier
import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.data.model.TravelStyle
import com.huntmate.app.data.model.TripStatus
import com.huntmate.app.ui.components.HuntmateColors
import com.huntmate.app.ui.components.HuntmateRadii
import com.huntmate.app.ui.components.HuntmateTextField
import com.huntmate.app.ui.components.MatchChip
import com.huntmate.app.ui.components.SectionCard
import com.huntmate.app.ui.components.AiItineraryGeneratorCard
import com.huntmate.app.ui.components.SharedTripBudgetCard
import com.huntmate.app.ui.components.SafetyShieldCard
import com.huntmate.app.ui.components.TravelerXpCard
import com.huntmate.app.ui.components.TripCountdownCard
import com.huntmate.app.ui.components.TravelPulseCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: ProfileViewModel,
    currentProfile: TravelProfile,
    onCompleted: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.profile

    LaunchedEffect(currentProfile.userId) {
        viewModel.prime(currentProfile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Set up your Huntmate profile", style = MaterialTheme.typography.headlineMedium, color = HuntmateColors.Navy)
        Text("Add the trip details needed for matching, countdown, safety, and itinerary tools.", color = HuntmateColors.MutedText)

        SectionCard(title = "About") {
            HuntmateTextField(profile.name, viewModel::updateName, "Display Name")
            HuntmateTextField(profile.bio, viewModel::updateBio, "Bio", minLines = 3)
        }

        SectionCard(title = "Trip Details") {
            HuntmateTextField(profile.homeCity, viewModel::updateHomeCity, "Home city")
            HuntmateTextField(profile.destinationCity, viewModel::updateDestinationCity, "Destination city")
            HuntmateTextField(profile.tripStartDate, viewModel::updateTripStartDate, "Trip start YYYY-MM-DD")
            HuntmateTextField(profile.tripEndDate, viewModel::updateTripEndDate, "Trip end YYYY-MM-DD")
            HuntmateTextField(profile.tripGoal, viewModel::updateTripGoal, "Trip goal")
        }

        SectionCard(title = "Interests & Style") {
            HuntmateTextField(profile.interests.joinToString(", "), viewModel::updateInterests, "Interests")
            HuntmateTextField(profile.languages.joinToString(", "), viewModel::updateLanguages, "Languages")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TravelStyle.entries.forEach { style ->
                    FilterChip(
                        selected = profile.travelStyle == style,
                        onClick = { viewModel.updateTravelStyle(style) },
                        label = { Text(style.name.lowercase().replaceFirstChar { it.titlecase() }) },
                        shape = RoundedCornerShape(HuntmateRadii.Medium)
                    )
                }
            }
        }

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.successMessage?.let { Text(it, color = HuntmateColors.Success) }

        Button(
            onClick = { viewModel.save(onboardingComplete = true, onSaved = onCompleted) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(HuntmateRadii.Medium),
            colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Amber, contentColor = HuntmateColors.Navy)
        ) {
            Text("Complete setup", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    fallbackProfile: TravelProfile
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.profile
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.uploadPhoto(uri)
    }

    LaunchedEffect(fallbackProfile.userId) {
        viewModel.prime(fallbackProfile)
    }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp,
                color = Color.White
            ) {
                Button(
                    onClick = { viewModel.save(onboardingComplete = true) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(HuntmateRadii.Medium),
                    colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Navy)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HuntmateColors.AppBackground)
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            EnhancedProfileHero(profile, onEditPhoto = { imagePicker.launch("image/*") })

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile Strength Gauge
                ProfileStrengthCard(state.completionPercent)

                TripCountdownCard(profile)
                TravelPulseCard(profile)
                AiItineraryGeneratorCard(profile)
                SharedTripBudgetCard()
                TravelerXpCard(profile)
                SafetyShieldCard(profile)

                SectionCard(title = "About Me") {
                    HuntmateTextField(profile.name, viewModel::updateName, "Display Name")
                    Spacer(modifier = Modifier.height(8.dp))
                    HuntmateTextField(profile.bio, viewModel::updateBio, "Bio (Tell your story)", minLines = 3)
                }

                SectionCard(title = "Active Trip") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            HuntmateTextField(profile.homeCity, viewModel::updateHomeCity, "From")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            HuntmateTextField(profile.destinationCity, viewModel::updateDestinationCity, "To")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Trip Status", style = MaterialTheme.typography.labelMedium, color = HuntmateColors.MutedText)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TripStatus.entries.forEach { status ->
                            val isSelected = profile.tripStatus == status
                            Surface(
                                modifier = Modifier.weight(1f).clickable { viewModel.updateTripStatus(status) },
                                shape = RoundedCornerShape(HuntmateRadii.Medium),
                                color = if (isSelected) HuntmateColors.Amber else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) HuntmateColors.Amber else HuntmateColors.Border)
                            ) {
                                Text(
                                    status.name.lowercase().replaceFirstChar { it.titlecase() },
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                SectionCard(title = "Travel Preferences") {
                    Text("Style", style = MaterialTheme.typography.labelMedium, color = HuntmateColors.MutedText)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TravelStyle.entries.forEach { style ->
                            FilterChip(
                                selected = profile.travelStyle == style,
                                onClick = { viewModel.updateTravelStyle(style) },
                                label = { Text(style.name.lowercase().replaceFirstChar { it.titlecase() }) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedProfileHero(profile: TravelProfile, onEditPhoto: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.verticalGradient(
                    listOf(HuntmateColors.Navy, HuntmateColors.NavyRaised)
                )
            )
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .background(Color.White.copy(alpha = 0.03f), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    border = BorderStroke(3.dp, HuntmateColors.Amber),
                    shadowElevation = 8.dp
                ) {
                    if (profile.profilePhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = profile.profilePhotoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(profile.name.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = HuntmateColors.Navy)
                        }
                    }
                }
                Surface(
                    onClick = onEditPhoto,
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = HuntmateColors.Amber,
                    shadowElevation = 4.dp
                ) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.padding(6.dp), tint = HuntmateColors.Navy)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(profile.name.ifBlank { "Traveler" }, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                if (profile.destinationCity.isNotBlank()) "Exploring ${profile.destinationCity}" else "Ready for adventure",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProfileStrengthCard(percent: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HuntmateRadii.Large),
        color = HuntmateColors.NavyRaised,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.size(56.dp),
                    color = HuntmateColors.Amber,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text("${percent}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HuntmateColors.Amber, modifier = Modifier.size(16.dp))
                    Text("Profile Strength", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Complete your profile to get 3x more matches.",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
