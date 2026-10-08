package com.huntmate.app.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huntmate.app.ui.components.HuntmateColors
import com.huntmate.app.ui.components.HuntmateHeader
import com.huntmate.app.ui.components.InitialAvatar
import com.huntmate.app.ui.components.SectionCard

@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onOpenChat: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.AppBackground)
            .padding(bottom = 80.dp)
    ) {
        HuntmateHeader(
            title = "Messages",
            actions = {
                IconButton(onClick = { /* New Chat action */ }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "New Message", tint = HuntmateColors.Amber)
                }
            }
        )

        // Premium Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { /* Handle search focus */ },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = HuntmateColors.MutedText)
                Text("Search travelers or trips...", color = HuntmateColors.MutedText, style = MaterialTheme.typography.bodyLarge)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.chats, key = { it.chatId }) { chat ->
                ChatRow(chat = chat, onOpen = { onOpenChat(chat.chatId) })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = HuntmateColors.Border.copy(alpha = 0.5f)
                )
            }
            if (state.chats.isEmpty()) {
                item {
                    EmptyMessagesCard()
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: ChatListItem, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box {
            InitialAvatar(
                chat.initials, 
                modifier = Modifier.size(56.dp), 
                background = HuntmateColors.PeachAvatar
            )
            // Online Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(HuntmateColors.Success)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    chat.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = HuntmateColors.Navy
                )
                Text(
                    "2m ago", 
                    color = HuntmateColors.MutedText, 
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    chat.subtitle, 
                    color = HuntmateColors.MutedText, 
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                // Unread Indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(HuntmateColors.Amber)
                )
            }
        }
    }
}

@Composable
private fun EmptyMessagesCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline, 
                    contentDescription = null, 
                    tint = HuntmateColors.Navy,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            "Start your adventure", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Bold,
            color = HuntmateColors.Navy
        )
        Text(
            "Connect with travelers heading to your destination to start planning your next hunt together.",
            color = HuntmateColors.MutedText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Navy)
        ) {
            Text("Find Fellow Travelers", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChatThreadScreen(viewModel: ChatThreadViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val composerState by viewModel.composerState.collectAsState()

    Scaffold(
        topBar = {
            HuntmateHeader(
                title = uiState.chatTitle, 
                subtitle = "Active now",
                actions = {
                    IconButton(onClick = { /* Call action */ }) {
                        Icon(Icons.Outlined.Call, contentDescription = null, tint = HuntmateColors.Amber)
                    }
                }
            )
        },
        bottomBar = {
            ChatComposer(
                value = composerState.input,
                onValueChange = viewModel::updateInput,
                onSend = viewModel::sendMessage
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HuntmateColors.AppBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    SectionCard(title = "Kick off the trip!") {
                        Text("Send a message to introduce yourself and start planning the logistics.")
                    }
                }
            }
            items(uiState.messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }
        }
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Add Attachment logic */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = HuntmateColors.Navy)
            }
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Message...") },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuntmateColors.Border,
                    unfocusedBorderColor = HuntmateColors.Border,
                    containerColor = HuntmateColors.AppBackground
                )
            )
            
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (value.isNotBlank()) HuntmateColors.Amber else Color(0xFFE3E8EF))
            ) {
                Icon(
                    Icons.Outlined.Send,
                    contentDescription = "Send", 
                    tint = if (value.isNotBlank()) HuntmateColors.Navy else HuntmateColors.MutedText
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageItem) {
    val isMe = message.isCurrentUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isMe) 20.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 20.dp
            ),
            color = if (isMe) HuntmateColors.Navy else Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isMe) {
                    Text(
                        text = message.senderLabel,
                        color = HuntmateColors.Amber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = message.content,
                    color = if (isMe) Color.White else HuntmateColors.Navy,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = message.timeLabel,
                    color = if (isMe) Color.White.copy(alpha = 0.6f) else HuntmateColors.MutedText,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
