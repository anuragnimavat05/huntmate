package com.huntmate.app.ui.screen.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huntmate.app.ui.components.HuntmateColors
import com.huntmate.app.ui.components.HuntmateFilterChip
import com.huntmate.app.ui.components.HuntmateRadii
import com.huntmate.app.ui.components.InitialAvatar
import com.huntmate.app.ui.components.MatchChip
import com.huntmate.app.ui.components.SectionCard

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel) {
    val state by viewModel.uiState.collectAsState()
    val cards by viewModel.discoverCards.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.AppBackground)
            .padding(top = 76.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HuntmateColors.Navy)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Travelers heading your way", color = HuntmateColors.MutedText, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HuntmateFilterChip("All", selected = state.filters.cityQuery.isBlank(), onClick = { viewModel.updateCity("") })
                    HuntmateFilterChip("Bali", selected = state.filters.cityQuery.equals("Bali", true), onClick = { viewModel.updateCity("Bali") })
                    HuntmateFilterChip("Hiking", selected = state.filters.interestQuery.equals("Hiking", true), onClick = { viewModel.updateInterest("Hiking") })
                    HuntmateFilterChip("English", selected = state.filters.languageQuery.equals("English", true), onClick = { viewModel.updateLanguage("English") })
                }
                state.successMessage?.let { Text(it, color = HuntmateColors.Amber) }
                state.errorMessage?.let { Text(it, color = Color(0xFFFFB4AB)) }
            }
        }

        if (cards.isEmpty()) {
            item {
                SectionCard(title = "No matches right now", modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text("Try a different city, language, or interest to widen your search.")
                }
            }
        }

        items(cards, key = { it.match.profile.userId }) { card ->
            TravelerMatchCard(
                card = card,
                onConnect = { viewModel.sendConnection(card) },
                onReport = { viewModel.reportTraveler(card.match.profile.userId) },
                onBlock = { viewModel.blockTraveler(card.match.profile.userId) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TravelerMatchCard(
    card: DiscoverCard,
    onConnect: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    val profile = card.match.profile
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(HuntmateRadii.Large),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE8ECF2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(if (profile.destinationCity.contains("tokyo", true)) HuntmateColors.IndigoImage else Color(0xFF1F6B61))
            ) {
                MatchScoreRing(card.match.score, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp))
                InitialAvatar(
                    initials = card.travelerName.take(2),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp)
                        .size(70.dp)
                        .border(3.dp, Color.White, CircleShape),
                    background = if (card.travelerName.hashCode() % 2 == 0) HuntmateColors.PeachAvatar else HuntmateColors.BlueAvatar
                )
            }
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(card.travelerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    StatusPill(profile.tripStatus.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() })
                }
                Text(
                    "${profile.homeCity.ifBlank { "Traveler" }} -> ${profile.destinationCity.ifBlank { "Open destination" }} | ${profile.tripStartDate.ifBlank { "Flexible" }}",
                    color = HuntmateColors.MutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(card.travelerBio, color = Color(0xFF4A5568), style = MaterialTheme.typography.bodyLarge)
                Text("WHY YOU MATCH", color = HuntmateColors.MutedText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    card.match.reasons.ifEmpty { listOf("Same destination", "Compatible style") }.take(4).forEach {
                        MatchChip(it)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color(0xFFF0F3F7), RoundedCornerShape(0.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onConnect,
                    enabled = card.connectionState == DiscoverConnectionState.AVAILABLE,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(HuntmateRadii.Medium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (card.connectionState) {
                            DiscoverConnectionState.AVAILABLE -> HuntmateColors.Navy
                            DiscoverConnectionState.PENDING -> Color(0xFFE8ECF2)
                            DiscoverConnectionState.CONNECTED -> Color(0xFFD4F5EC)
                        },
                        contentColor = when (card.connectionState) {
                            DiscoverConnectionState.AVAILABLE -> Color.White
                            DiscoverConnectionState.PENDING -> HuntmateColors.MutedText
                            DiscoverConnectionState.CONNECTED -> Color(0xFF0A6B50)
                        }
                    )
                ) {
                    Text(
                        when (card.connectionState) {
                            DiscoverConnectionState.AVAILABLE -> "Connect"
                            DiscoverConnectionState.PENDING -> "Pending"
                            DiscoverConnectionState.CONNECTED -> "Connected"
                        }
                    )
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(HuntmateRadii.Medium),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8FAFB), contentColor = HuntmateColors.Navy)
                ) {
                    Text("View profile")
                }
                IconButton(
                    onClick = onReport,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(HuntmateRadii.Medium))
                        .background(Color(0xFFF4F6FA))
                ) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More actions", tint = HuntmateColors.MutedText)
                }
            }
        }
    }
}

@Composable
private fun MatchScoreRing(score: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(62.dp)
            .clip(CircleShape)
            .border(2.dp, HuntmateColors.Amber, CircleShape)
            .background(HuntmateColors.Navy.copy(alpha = 0.94f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score.toString(), color = HuntmateColors.Amber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("match", color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(HuntmateRadii.Medium))
            .background(Color(0xFFD4F5EC))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text, color = Color(0xFF0A6B50), style = MaterialTheme.typography.bodyMedium)
    }
}
