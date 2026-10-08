package com.huntmate.app.ui.screen.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huntmate.app.ui.components.HuntmateColors
import com.huntmate.app.ui.components.HuntmateRadii
import com.huntmate.app.ui.components.SectionCard

data class ActivityItem(
    val id: String,
    val title: String,
    val body: String
)

@Composable
fun ActivityScreen(viewModel: ActivityViewModel) {
    val items by viewModel.items.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.AppBackground)
            .padding(start = 16.dp, end = 16.dp, top = 92.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Activity", style = MaterialTheme.typography.headlineMedium, color = HuntmateColors.Navy)
                Text("Connection requests, accepted matches, and recent conversations.", color = HuntmateColors.MutedText)
            }
        }
        if (items.isEmpty()) {
            item {
                SectionCard(title = "No alerts yet") {
                    Text("When someone connects, accepts, or messages you, it will show up here.")
                }
            }
        }
        items(items, key = { it.id }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(HuntmateRadii.Large))
                    .border(0.5.dp, HuntmateColors.Border, RoundedCornerShape(HuntmateRadii.Large))
                    .background(Color.White)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(HuntmateColors.Amber.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = HuntmateColors.Amber)
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.title, fontWeight = FontWeight.SemiBold, color = HuntmateColors.Navy)
                    Text(item.body, color = HuntmateColors.MutedText)
                }
            }
        }
    }
}
