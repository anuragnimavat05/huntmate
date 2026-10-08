package com.huntmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huntmate.app.data.model.MatchResult
import com.huntmate.app.data.model.Post
import com.huntmate.app.data.model.TravelProfile

object HuntmateColors {
    val Navy = Color(0xFF0D1B2A)
    val NavyRaised = Color(0xFF152638)
    val Amber = Color(0xFFF5A623)
    val Tertiary = Color(0xFF5FA8D3)
    val AppBackground = Color(0xFFF4F6FA)
    val MutedText = Color(0xFF8A96A8)
    val Border = Color(0xFFE3E8EF)
    val ChipBlue = Color(0xFFF0F7FF)
    val ChipBlueBorder = Color(0xFFBDD7F5)
    val ChipText = Color(0xFF1A4A7A)
    val Success = Color(0xFF22C55E)
    val TealImage = Color(0xFF2E8375)
    val IndigoImage = Color(0xFF33477F)
    val PeachAvatar = Color(0xFFFFD8C2)
    val BlueAvatar = Color(0xFFB8D4F0)
}

object HuntmateRadii {
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HuntmateRadii.Large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, HuntmateColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
fun HuntmateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(HuntmateRadii.Medium)
    )
}

@Composable
fun LoadingState(message: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator()
        Text(message)
    }
}

@Composable
fun HuntmateHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuntmateColors.Navy)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "* $title",
                color = HuntmateColors.Amber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = actions)
        }
        subtitle?.let {
            Text(text = it, color = HuntmateColors.MutedText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onClick: () -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .background(HuntmateColors.NavyRaised)
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
            badge?.let {
                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(HuntmateColors.Amber),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(it, color = HuntmateColors.Navy, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HuntmateFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontWeight = FontWeight.SemiBold) },
        modifier = modifier,
        shape = RoundedCornerShape(HuntmateRadii.Medium),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HuntmateColors.Amber,
            selectedLabelColor = HuntmateColors.Navy,
            containerColor = HuntmateColors.NavyRaised,
            labelColor = Color.White.copy(alpha = 0.72f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.White.copy(alpha = 0.18f),
            selectedBorderColor = HuntmateColors.Amber
        )
    )
}

@Composable
fun MatchChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(HuntmateRadii.Small))
            .border(1.dp, HuntmateColors.ChipBlueBorder, RoundedCornerShape(HuntmateRadii.Small))
            .background(HuntmateColors.ChipBlue)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = HuntmateColors.ChipText, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun InitialAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    background: Color = HuntmateColors.PeachAvatar,
    foreground: Color = Color(0xFF7B2D00)
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(initials.take(2).uppercase(), color = foreground, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProfileSummary(profile: TravelProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(profile.name, style = MaterialTheme.typography.titleMedium)
        Text(profile.bio.ifBlank { "No bio yet." }, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(profile.homeCity.ifBlank { "Home city" }) })
            AssistChip(onClick = {}, label = { Text(profile.destinationCity.ifBlank { "Destination" }) })
        }
    }
}

@Composable
fun PostSummary(post: Post) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(post.authorName, style = MaterialTheme.typography.titleSmall)
        Text(post.caption)
        Text("${post.cityTag} | ${post.likeCount} likes | ${post.commentCount} comments")
    }
}

@Composable
fun MatchSummary(match: MatchResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${match.profile.name} | ${match.score}% fit", style = MaterialTheme.typography.titleSmall)
        Text("${match.profile.homeCity} -> ${match.profile.destinationCity}")
        Text(match.reasons.joinToString())
    }
}
