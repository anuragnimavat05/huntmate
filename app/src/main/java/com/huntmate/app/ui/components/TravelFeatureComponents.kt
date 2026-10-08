package com.huntmate.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huntmate.app.data.model.TravelProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TravelStoriesReel(profile: TravelProfile, modifier: Modifier = Modifier) {
    val destination = profile.destinationCity.ifBlank { "Next city" }
    val stories = listOf(
        "Your Hunt" to destination,
        "Food trail" to "Local finds",
        "Sunset spot" to "Live now",
        "Hostel crew" to "3 updates",
        "Hidden route" to "New"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stories.forEachIndexed { index, story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(HuntmateRadii.Large))
                        .background(if (index == 0) HuntmateColors.Amber else HuntmateColors.NavyRaised),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (index == 0) Icons.Outlined.PhotoCamera else Icons.Outlined.Explore,
                        contentDescription = story.first,
                        tint = if (index == 0) HuntmateColors.Navy else Color.White
                    )
                }
                Text(story.first, style = MaterialTheme.typography.labelMedium, color = HuntmateColors.Navy)
                Text(story.second, style = MaterialTheme.typography.labelSmall, color = HuntmateColors.MutedText)
            }
        }
    }
}

@Composable
fun AiItineraryGeneratorCard(profile: TravelProfile, modifier: Modifier = Modifier) {
    var destination by remember(profile.destinationCity) { mutableStateOf(profile.destinationCity) }
    var interests by remember(profile.interests) { mutableStateOf(profile.interests.joinToString(", ")) }
    var plan by remember { mutableStateOf("") }

    SectionCard(title = "AI Itinerary Hunt", modifier = modifier) {
        HuntmateTextField(destination, { destination = it }, "Destination")
        HuntmateTextField(interests, { interests = it }, "Interests")
        Button(
            onClick = {
                val city = destination.ifBlank { "your destination" }
                val theme = interests.ifBlank { "local food, culture, and scenic walks" }
                plan = "Day 1: arrival walk and cafe hunt in $city\nDay 2: $theme route with photo stops\nDay 3: relaxed checkout, market visit, and match meetup"
            },
            colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Navy),
            shape = RoundedCornerShape(HuntmateRadii.Medium)
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Generate plan")
        }
        if (plan.isNotBlank()) {
            FeatureCallout(icon = Icons.Outlined.ConfirmationNumber, title = "Suggested hunt", body = plan)
        }
    }
}

@Composable
fun SharedTripBudgetCard(modifier: Modifier = Modifier) {
    var hotel by remember { mutableIntStateOf(0) }
    var transport by remember { mutableIntStateOf(0) }
    var meals by remember { mutableIntStateOf(0) }
    var people by remember { mutableIntStateOf(2) }
    val total = hotel + transport + meals
    val perPerson = if (people <= 0) total else total / people

    SectionCard(title = "Shared Trip Budget", modifier = modifier) {
        BudgetRow("Hotels", hotel) { hotel = it }
        BudgetRow("Transport", transport) { transport = it }
        BudgetRow("Meals", meals) { meals = it }
        BudgetRow("People", people) { people = it.coerceAtLeast(1) }
        FeatureCallout(
            icon = Icons.Outlined.CurrencyRupee,
            title = "Split estimate",
            body = "Total Rs $total | Rs $perPerson each"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TravelerXpCard(profile: TravelProfile, modifier: Modifier = Modifier) {
    val completed = listOf(
        profile.onboardingComplete,
        profile.destinationCity.isNotBlank(),
        profile.interests.isNotEmpty(),
        profile.tripStatus.name == "BOOKED" || profile.tripStatus.name == "IN_DESTINATION"
    )
    val xp = completed.count { it } * 120

    SectionCard(title = "Traveler XP", modifier = modifier) {
        LinearProgressIndicator(
            progress = { (xp / 500f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(HuntmateRadii.Small)),
            color = HuntmateColors.Amber,
            trackColor = HuntmateColors.Border
        )
        Text("$xp XP earned", color = HuntmateColors.Navy, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            XpBadge("Profile ready", completed[0])
            XpBadge("City explorer", completed[1])
            XpBadge("Interest hunter", completed[2])
            XpBadge("Trip confirmed", completed[3])
        }
    }
}

@Composable
fun TripCountdownCard(profile: TravelProfile, modifier: Modifier = Modifier) {
    val daysLeft = remember(profile.tripStartDate) {
        runCatching {
            ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(profile.tripStartDate)).coerceAtLeast(0)
        }.getOrNull()
    }
    SectionCard(title = "Trip Countdown", modifier = modifier) {
        FeatureCallout(
            icon = Icons.Outlined.CalendarMonth,
            title = profile.destinationCity.ifBlank { "Next trip" },
            body = if (daysLeft != null) "$daysLeft days until your hunt begins" else "Add a trip start date to start the countdown"
        )
    }
}

@Composable
fun TravelPulseCard(profile: TravelProfile, modifier: Modifier = Modifier) {
    val destination = profile.destinationCity.ifBlank { "your destination" }
    SectionCard(title = "Travel Pulse Matchmaker", modifier = modifier) {
        Text("3 ideal trip partners for $destination", color = HuntmateColors.MutedText)
        listOf("Same destination", "Similar budget", "Shared interests").forEachIndexed { index, reason ->
            FeatureCallout(
                icon = Icons.Outlined.AutoAwesome,
                title = "Partner ${index + 1}",
                body = reason
            )
        }
    }
}

@Composable
fun SafetyShieldCard(profile: TravelProfile, modifier: Modifier = Modifier) {
    var checkedIn by remember { mutableStateOf(false) }
    SectionCard(title = "Safety Shield", modifier = modifier) {
        FeatureCallout(
            icon = Icons.Outlined.Security,
            title = if (checkedIn) "Checked in safely" else "Ready to check in",
            body = if (checkedIn) {
                "Accepted connections can be notified that you arrived safely."
            } else {
                "Use check-in when you reach ${profile.destinationCity.ifBlank { "your stop" }}."
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { checkedIn = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(HuntmateRadii.Medium),
                colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Navy)
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Check in")
            }
            Button(
                onClick = { checkedIn = true },
                shape = RoundedCornerShape(HuntmateRadii.Medium),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE7E2), contentColor = Color(0xFF9A2D18))
            ) {
                Icon(Icons.Outlined.Sos, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SOS")
            }
        }
    }
}

@Composable
private fun BudgetRow(label: String, value: Int, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { onChange(it.filter(Char::isDigit).toIntOrNull() ?: 0) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(HuntmateRadii.Medium)
    )
}

@Composable
private fun XpBadge(text: String, earned: Boolean) {
    Surface(
        shape = RoundedCornerShape(HuntmateRadii.Medium),
        color = if (earned) Color(0xFFFFF3DC) else Color(0xFFF7F9FC),
        border = BorderStroke(1.dp, if (earned) HuntmateColors.Amber else HuntmateColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Badge, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (earned) HuntmateColors.Amber else HuntmateColors.MutedText)
            Text(text, color = if (earned) HuntmateColors.Navy else HuntmateColors.MutedText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FeatureCallout(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HuntmateRadii.Medium))
            .border(1.dp, HuntmateColors.Border, RoundedCornerShape(HuntmateRadii.Medium))
            .background(Color(0xFFF8FAFB))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(HuntmateColors.Amber.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HuntmateColors.Amber, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = HuntmateColors.Navy, fontWeight = FontWeight.SemiBold)
            Text(body, color = HuntmateColors.MutedText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
