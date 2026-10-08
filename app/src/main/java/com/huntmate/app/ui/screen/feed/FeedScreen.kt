package com.huntmate.app.ui.screen.feed

import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.huntmate.app.data.model.Post
import com.huntmate.app.ui.components.*

@Composable
fun FeedScreen(viewModel: FeedViewModel) {
    val feedState by viewModel.feed.collectAsState()
    val feed = feedState.first
    val savedPostIds = feedState.second
    val composerState by viewModel.composerState.collectAsState()
    val comments = remember { mutableStateMapOf<String, String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.AppBackground)
    ) {
        HuntmateHeader(
            title = "Huntmate Feed",
            actions = {
                IconButton(onClick = { /* TODO: Notifications */ }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = HuntmateColors.Amber)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Stories / Active Hunts Section
            item {
                ActiveHuntsSection()
            }

            // 2. Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeedFilter.entries.forEach { filter ->
                        HuntmateFilterChip(
                            text = when (filter) {
                                FeedFilter.ALL -> "For You"
                                FeedFilter.SAVED -> "Saved"
                                FeedFilter.CITY -> "Nearby"
                            },
                            selected = composerState.selectedFilter == filter,
                            onClick = { viewModel.updateFilter(filter) }
                        )
                    }
                }
            }

            // 3. Feed Items
            if (feed.isEmpty()) {
                item {
                    EmptyFeedState()
                }
            }

            items(feed, key = { it.id }) { post ->
                TravelPostCard(
                    post = post,
                    isSaved = savedPostIds.contains(post.id),
                    comment = comments[post.id].orEmpty(),
                    onCommentChange = { comments[post.id] = it },
                    onLike = { viewModel.toggleLike(post.id) },
                    onSave = { viewModel.toggleSave(post.id) },
                    onComment = {
                        val text = comments[post.id].orEmpty()
                        if (text.isNotBlank()) {
                            viewModel.addComment(post.id, text)
                            comments[post.id] = ""
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ActiveHuntsSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            "Live Moments",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AddStoryButton()
            }
            items(6) { index -> // Mock stories
                StoryCircle(name = if (index == 0) "You" else "Traveler $index")
            }
        }
    }
}

@Composable
private fun StoryCircle(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(2.5.dp, Brush.linearGradient(listOf(HuntmateColors.Amber, HuntmateColors.Tertiary)), RoundedCornerShape(HuntmateRadii.Large))
                .padding(4.dp)
                .clip(RoundedCornerShape(HuntmateRadii.Large))
                .background(Color.LightGray)
        ) {
            InitialAvatar(name, modifier = Modifier.fillMaxSize())
        }
        Text(name, style = MaterialTheme.typography.labelMedium, color = HuntmateColors.Navy)
    }
}

@Composable
private fun AddStoryButton() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(HuntmateRadii.Large))
                .background(HuntmateColors.NavyRaised),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        }
        Text("Share", style = MaterialTheme.typography.labelMedium, color = HuntmateColors.Navy)
    }
}

@Composable
private fun TravelPostCard(
    post: Post,
    isSaved: Boolean,
    comment: String,
    onCommentChange: (String) -> Unit,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onComment: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(HuntmateRadii.Large),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, HuntmateColors.Border)
    ) {
        Column {
            // User Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InitialAvatar(post.authorName, modifier = Modifier.size(44.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(post.cityTag, color = HuntmateColors.MutedText, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = HuntmateColors.MutedText)
                }
            }

            // Media Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable { onLike(); isLiked = !isLiked }
            ) {
                PostMediaPreview(post = post)
                
                // Location Overlay
                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomStart),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(HuntmateRadii.Medium)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(post.cityTag, color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Actions & Description
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(onClick = { onLike(); isLiked = !isLiked }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, 
                                contentDescription = null, 
                                tint = if (isLiked) Color.Red else HuntmateColors.Navy
                            )
                        }
                        IconButton(onClick = onComment, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.ModeComment, contentDescription = null, tint = HuntmateColors.Navy)
                        }
                        IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = HuntmateColors.Navy)
                        }
                    }
                    IconButton(onClick = onSave, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, 
                            contentDescription = null, 
                            tint = if (isSaved) HuntmateColors.Amber else HuntmateColors.Navy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                if (post.hashtags.isNotEmpty()) {
                    Text(
                        text = post.hashtags.joinToString(" ") { "#$it" },
                        color = HuntmateColors.Tertiary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    "View all ${post.commentCount} comments",
                    color = HuntmateColors.MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp).clickable { }
                )
            }
        }
    }
}

@Composable
private fun PostMediaPreview(post: Post) {
    when {
        post.imageUrl.isBlank() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (post.mediaType == "video") HuntmateColors.IndigoImage else HuntmateColors.TealImage),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (post.mediaType == "video") Icons.Outlined.PlayCircle else Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        if (post.mediaType == "video") "Travel video" else "Travel photo",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        post.mediaType == "video" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HuntmateColors.IndigoImage),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                Text("Video uploaded", modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp), color = Color.White)
            }
        }
        else -> {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyFeedState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(64.dp), tint = HuntmateColors.Border)
        Text("No stories in this area yet", style = MaterialTheme.typography.titleMedium, color = HuntmateColors.MutedText)
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Navy),
            shape = RoundedCornerShape(HuntmateRadii.Medium)
        ) {
            Text("Invite Friends")
        }
    }
}

@Composable
fun CreatePostScreen(viewModel: FeedViewModel) {
    val state by viewModel.composerState.collectAsState()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val mimeType = uri?.let { context.contentResolver.getType(it) }.orEmpty()
        val mediaType = if (mimeType.startsWith("video")) "video" else "image"
        viewModel.updateSelectedMedia(uri, mediaType)
    }

    Scaffold(
        topBar = {
            HuntmateHeader(title = "New Moment", actions = {
                TextButton(onClick = viewModel::createPost) {
                    Text("Share", color = HuntmateColors.Amber, fontWeight = FontWeight.Bold)
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Image Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(HuntmateRadii.Large))
                    .background(HuntmateColors.NavyRaised)
                    .clickable { picker.launch(arrayOf("image/*", "video/*")) },
                contentAlignment = Alignment.Center
            ) {
                if (state.selectedMediaUri != null) {
                    if (state.selectedMediaType == "video") {
                        AndroidView(
                            factory = { viewContext ->
                                VideoView(viewContext).apply {
                                    setVideoURI(state.selectedMediaUri)
                                    setOnPreparedListener { player ->
                                        player.isLooping = true
                                        player.setVolume(0f, 0f)
                                        start()
                                    }
                                }
                            },
                            update = { videoView ->
                                videoView.setVideoURI(state.selectedMediaUri)
                                videoView.start()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(HuntmateRadii.Small)
                        ) {
                            Text("Video", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color.White)
                        }
                    } else {
                        AsyncImage(
                            model = state.selectedMediaUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Add a Travel Photo or Video", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            OutlinedTextField(
                value = state.caption,
                onValueChange = viewModel::updateCaption,
                placeholder = { Text("Write a caption...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(HuntmateRadii.Medium),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HuntmateColors.Amber)
            )

            OutlinedTextField(
                value = state.cityTag,
                onValueChange = viewModel::updateCityTag,
                placeholder = { Text("Add location") },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(HuntmateRadii.Medium)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
