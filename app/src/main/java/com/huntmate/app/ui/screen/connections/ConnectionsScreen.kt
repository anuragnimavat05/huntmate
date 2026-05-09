package com.huntmate.app.ui.screen.connections

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huntmate.app.data.model.ConnectionStatus
import com.huntmate.app.ui.components.*

@Composable
fun ConnectionsScreen(
    viewModel: ConnectionsViewModel,
    onOpenChat: (String) -> Unit
) {
    val connections by viewModel.connections.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    val filteredConnections = when (selectedTab) {
        0 -> connections.filter { it.connection.status == ConnectionStatus.PENDING }
        1 -> connections.filter { it.connection.status == ConnectionStatus.ACCEPTED }
        else -> connections.filter { it.connection.status == ConnectionStatus.REJECTED }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.AppBackground)
    ) {
        HuntmateHeader(
            title = "My Connections",
            actions = {
                IconButton(onClick = { /* Filter */ }) {
                    Icon(Icons.Outlined.FilterList, contentDescription = null, tint = HuntmateColors.Amber)
                }
            }
        )

        // Custom Premium Tab Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HuntmateColors.Navy,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Pending", "Accepted", "Past").forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    val count = when (index) {
                        0 -> connections.count { it.connection.status == ConnectionStatus.PENDING }
                        1 -> connections.count { it.connection.status == ConnectionStatus.ACCEPTED }
                        else -> connections.count { it.connection.status == ConnectionStatus.REJECTED }
                    }
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = index },
                        shape = RoundedCornerShape(HuntmateRadii.Medium),
                        color = if (isSelected) HuntmateColors.NavyRaised else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, HuntmateColors.Amber.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                label,
                                color = if (isSelected) HuntmateColors.Amber else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) HuntmateColors.Amber else Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        count.toString(),
                                        color = HuntmateColors.Navy,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredConnections.isEmpty()) {
                item {
                    EmptyConnectionsState(selectedTab)
                }
            }

            items(filteredConnections, key = { it.connection.id }) { card ->
                ConnectionCardItem(
                    card = card,
                    onAccept = { viewModel.accept(card.connection, onOpenChat) },
                    onReject = { viewModel.reject(card.connection.id) },
                    onOpenChat = { viewModel.openAcceptedChat(card.connection, onOpenChat) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConnectionCardItem(
    card: ConnectionCard,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onOpenChat: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HuntmateRadii.Large),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, HuntmateColors.Border.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box {
                    InitialAvatar(
                        card.initials,
                        modifier = Modifier.size(64.dp),
                        background = HuntmateColors.PeachAvatar
                    )
                    // Compatibility Badge
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp),
                        shape = CircleShape,
                        color = HuntmateColors.Amber,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(10.dp), tint = HuntmateColors.Navy)
                            Text("${card.connection.compatibilityScore}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HuntmateColors.Navy)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(card.travelerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(card.travelerSubtitle, color = HuntmateColors.MutedText, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match Reasons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                card.connection.reasons.forEach { reason ->
                    Surface(
                        shape = RoundedCornerShape(HuntmateRadii.Small),
                        color = HuntmateColors.ChipBlue,
                        border = BorderStroke(1.dp, HuntmateColors.ChipBlueBorder)
                    ) {
                        Text(
                            reason,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = HuntmateColors.ChipText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            when (card.connection.status) {
                ConnectionStatus.PENDING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(HuntmateRadii.Medium),
                            colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Navy)
                        ) {
                            Text("Confirm Match", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(HuntmateRadii.Medium),
                            border = BorderStroke(1.dp, HuntmateColors.Border)
                        ) {
                            Text("Ignore", color = HuntmateColors.MutedText)
                        }
                    }
                }
                ConnectionStatus.ACCEPTED -> {
                    Button(
                        onClick = onOpenChat,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(HuntmateRadii.Medium),
                        colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Amber)
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(18.dp), tint = HuntmateColors.Navy)
                        Spacer(Modifier.width(8.dp))
                        Text("Send Message", fontWeight = FontWeight.Bold, color = HuntmateColors.Navy)
                    }
                }
                else -> {
                    Text("Connection closed", color = HuntmateColors.MutedText, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun EmptyConnectionsState(selectedTab: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val message = when (selectedTab) {
            0 -> "No pending requests. Try discovering new travelers!"
            1 -> "You haven't matched with anyone yet."
            else -> "No past connections."
        }
        Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(64.dp), tint = HuntmateColors.Border)
        Text(message, color = HuntmateColors.MutedText, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
