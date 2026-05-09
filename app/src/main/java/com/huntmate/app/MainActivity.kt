package com.huntmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.huntmate.app.data.model.TravelProfile
import com.huntmate.app.navigation.HuntmateDestination
import com.huntmate.app.ui.screen.activity.ActivityScreen
import com.huntmate.app.ui.screen.activity.ActivityViewModel
import com.huntmate.app.ui.screen.activity.ActivityViewModelFactory
import com.huntmate.app.ui.screen.auth.AuthScreen
import com.huntmate.app.ui.screen.auth.AuthViewModel
import com.huntmate.app.ui.screen.auth.AuthViewModelFactory
import com.huntmate.app.ui.screen.auth.SessionState
import com.huntmate.app.ui.screen.chat.ChatListScreen
import com.huntmate.app.ui.screen.chat.ChatListViewModel
import com.huntmate.app.ui.screen.chat.ChatListViewModelFactory
import com.huntmate.app.ui.screen.chat.ChatThreadScreen
import com.huntmate.app.ui.screen.chat.ChatThreadViewModel
import com.huntmate.app.ui.screen.chat.ChatThreadViewModelFactory
import com.huntmate.app.ui.screen.connections.ConnectionsScreen
import com.huntmate.app.ui.screen.connections.ConnectionsViewModel
import com.huntmate.app.ui.screen.connections.ConnectionsViewModelFactory
import com.huntmate.app.ui.screen.discover.DiscoverScreen
import com.huntmate.app.ui.screen.discover.DiscoverViewModel
import com.huntmate.app.ui.screen.discover.DiscoverViewModelFactory
import com.huntmate.app.ui.screen.feed.CreatePostScreen
import com.huntmate.app.ui.screen.feed.FeedScreen
import com.huntmate.app.ui.screen.feed.FeedViewModel
import com.huntmate.app.ui.screen.feed.FeedViewModelFactory
import com.huntmate.app.ui.screen.profile.OnboardingScreen
import com.huntmate.app.ui.screen.profile.ProfileScreen
import com.huntmate.app.ui.screen.profile.ProfileViewModel
import com.huntmate.app.ui.screen.profile.ProfileViewModelFactory
import com.huntmate.app.ui.components.HuntmateColors
import com.huntmate.app.ui.components.HuntmateRadii
import com.huntmate.app.ui.theme.HuntmateTheme

class MainActivity : ComponentActivity() {
    private val container = AppContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.isVerticalScrollBarEnabled = false
        window.decorView.isHorizontalScrollBarEnabled = false
        window.attributes = window.attributes.apply {
            preferredRefreshRate = 120f
        }
        setContent {
            HuntmateTheme {
                HuntmateApp(container = container)
            }
        }
    }
}

@Composable
private fun HuntmateApp(container: AppContainer) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(container.authRepository, container.userRepository))
    val sessionState by authViewModel.sessionState.collectAsState()

    when {
        sessionState.isLoading -> FullScreenMessage("Checking your traveler profile...")
        sessionState.currentUser == null -> AuthScreen(viewModel = authViewModel)
        sessionState.profile?.onboardingComplete != true -> {
            val profileViewModel: ProfileViewModel = viewModel(
                key = "onboarding",
                factory = ProfileViewModelFactory(
                    authRepository = container.authRepository,
                    userRepository = container.userRepository,
                    mediaRepository = container.mediaRepository,
                    profileValidator = container.profileValidator
                )
            )
            OnboardingScreen(
                viewModel = profileViewModel,
                currentProfile = sessionState.profile ?: TravelProfile(
                    userId = sessionState.currentUser?.uid.orEmpty(),
                    email = sessionState.currentUser?.email.orEmpty()
                ),
                onCompleted = { authViewModel.refreshProfile() }
            )
        }
        else -> MainShell(container = container, sessionState = sessionState, authViewModel = authViewModel)
    }
}

@Composable
private fun MainShell(
    container: AppContainer,
    sessionState: SessionState,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val bottomDestinations = remember {
        listOf(
            HuntmateDestination.Feed,
            HuntmateDestination.Discover,
            HuntmateDestination.CreatePost,
            HuntmateDestination.Connections,
            HuntmateDestination.Profile
        )
    }
    val hideTopChrome = currentRoute == HuntmateDestination.ChatList.route ||
        currentRoute?.startsWith("chat_thread/") == true
    val hideBottomChrome = currentRoute?.startsWith("chat_thread/") == true

    Box(modifier = Modifier.fillMaxSize().background(HuntmateColors.AppBackground)) {
        MainNavHost(
            container = container,
            sessionState = sessionState,
            navToChat = { chatId -> navController.navigate(HuntmateDestination.ChatThread.createRoute(chatId)) },
            navController = navController
        )

        if (!hideTopChrome) {
            HuntmateTopBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp),
                onOpenActivity = { navController.navigate(HuntmateDestination.Activity.route) },
                onOpenChats = { navController.navigate(HuntmateDestination.ChatList.route) },
                onSignOut = { authViewModel.signOut() }
            )
        }
        if (!hideBottomChrome) {
            HuntmateBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp),
                currentRoute = currentRoute,
                destinations = bottomDestinations,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun HuntmateTopBar(
    modifier: Modifier = Modifier,
    onOpenActivity: () -> Unit,
    onOpenChats: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            bottomStart = HuntmateRadii.Large,
            bottomEnd = HuntmateRadii.Large
        ),
        color = HuntmateColors.Navy,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HuntmateColors.Amber.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("*", color = HuntmateColors.Amber, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Huntmate",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = HuntmateColors.Amber,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopIconAction(
                    label = "Alerts",
                    icon = Icons.Outlined.NotificationsNone,
                    background = HuntmateColors.NavyRaised,
                    onClick = onOpenActivity
                )
                TopIconAction(
                    label = "Chats",
                    icon = Icons.Outlined.ChatBubbleOutline,
                    background = HuntmateColors.NavyRaised,
                    onClick = onOpenChats
                )
                TopIconAction(
                    label = "Sign out",
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    background = HuntmateColors.NavyRaised,
                    onClick = onSignOut
                )
            }
        }
    }
}

@Composable
private fun TopIconAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(HuntmateRadii.Medium))
            .semantics { contentDescription = label },
        color = background,
        shape = RoundedCornerShape(HuntmateRadii.Medium),
        shadowElevation = 0.dp
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun HuntmateBottomBar(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    destinations: List<HuntmateDestination>,
    onNavigate: (HuntmateDestination) -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shape = RoundedCornerShape(
            topStart = HuntmateRadii.Large,
            topEnd = HuntmateRadii.Large
        ),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                val isCenterAction = destination == HuntmateDestination.CreatePost
                val icon = when (destination) {
                    HuntmateDestination.Feed -> Icons.Outlined.Home
                    HuntmateDestination.Discover -> Icons.Outlined.Search
                    HuntmateDestination.CreatePost -> Icons.Outlined.AddBox
                    HuntmateDestination.Connections -> Icons.Outlined.ChatBubbleOutline
                    HuntmateDestination.Profile -> Icons.Outlined.Person
                    else -> Icons.Outlined.Home
                }
                val label = when (destination) {
                    HuntmateDestination.Feed -> "Feed"
                    HuntmateDestination.Discover -> "Discover"
                    HuntmateDestination.CreatePost -> "Post"
                    HuntmateDestination.Connections -> "Connect"
                    HuntmateDestination.Profile -> "Profile"
                    else -> destination.route
                }

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(destination) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = if (isCenterAction) Color.White else HuntmateColors.Navy,
                        selectedTextColor = HuntmateColors.Navy,
                        unselectedIconColor = Color(0xFFB0BAC9),
                        unselectedTextColor = Color(0xFFB0BAC9)
                    ),
                    alwaysShowLabel = true,
                    icon = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(if (isCenterAction) HuntmateRadii.Large else HuntmateRadii.Medium))
                                .background(
                                    when {
                                        isCenterAction -> HuntmateColors.Amber
                                        selected -> Color.Transparent
                                        else -> Color.Transparent
                                    }
                                )
                                .then(if (isCenterAction) Modifier.size(52.dp) else Modifier)
                                .padding(
                                    horizontal = if (isCenterAction) 12.dp else 8.dp,
                                    vertical = if (isCenterAction) 12.dp else 8.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = label)
                        }
                    },
                    label = {
                        Text(
                            label,
                            modifier = if (isCenterAction) Modifier.width(48.dp) else Modifier,
                            fontWeight = if (selected || isCenterAction) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MainNavHost(
    container: AppContainer,
    sessionState: SessionState,
    navToChat: (String) -> Unit,
    navController: androidx.navigation.NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = HuntmateDestination.Feed.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(HuntmateDestination.Feed.route) {
            val viewModel: FeedViewModel = viewModel(
                factory = FeedViewModelFactory(
                    authRepository = container.authRepository,
                    userRepository = container.userRepository,
                    postRepository = container.postRepository
                )
            )
            FeedScreen(viewModel = viewModel)
        }
        composable(HuntmateDestination.Discover.route) {
            val viewModel: DiscoverViewModel = viewModel(
                factory = DiscoverViewModelFactory(
                    authRepository = container.authRepository,
                    discoveryRepository = container.discoveryRepository,
                    connectionRepository = container.connectionRepository,
                    userRepository = container.userRepository
                )
            )
            DiscoverScreen(viewModel = viewModel)
        }
        composable(HuntmateDestination.CreatePost.route) {
            val viewModel: FeedViewModel = viewModel(
                key = "create_post",
                factory = FeedViewModelFactory(
                    authRepository = container.authRepository,
                    userRepository = container.userRepository,
                    postRepository = container.postRepository
                )
            )
            CreatePostScreen(viewModel = viewModel)
        }
        composable(HuntmateDestination.Connections.route) {
            val viewModel: ConnectionsViewModel = viewModel(
                factory = ConnectionsViewModelFactory(
                    authRepository = container.authRepository,
                    connectionRepository = container.connectionRepository,
                    chatRepository = container.chatRepository,
                    userRepository = container.userRepository
                )
            )
            ConnectionsScreen(viewModel = viewModel, onOpenChat = navToChat)
        }
        composable(HuntmateDestination.Profile.route) {
            val viewModel: ProfileViewModel = viewModel(
                key = "profile",
                factory = ProfileViewModelFactory(
                    authRepository = container.authRepository,
                    userRepository = container.userRepository,
                    mediaRepository = container.mediaRepository,
                    profileValidator = container.profileValidator
                )
            )
            ProfileScreen(
                viewModel = viewModel,
                fallbackProfile = sessionState.profile ?: TravelProfile(
                    userId = sessionState.currentUser?.uid.orEmpty(),
                    email = sessionState.currentUser?.email.orEmpty()
                )
            )
        }
        composable(HuntmateDestination.Activity.route) {
            val viewModel: ActivityViewModel = viewModel(
                factory = ActivityViewModelFactory(
                    container.authRepository,
                    container.connectionRepository,
                    container.chatRepository,
                    container.userRepository
                )
            )
            ActivityScreen(viewModel = viewModel)
        }
        composable(HuntmateDestination.ChatList.route) {
            val viewModel: ChatListViewModel = viewModel(
                factory = ChatListViewModelFactory(
                    container.authRepository,
                    container.chatRepository,
                    container.userRepository
                )
            )
            ChatListScreen(viewModel = viewModel, onOpenChat = navToChat)
        }
        composable(
            route = HuntmateDestination.ChatThread.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId").orEmpty()
            val viewModel: ChatThreadViewModel = viewModel(
                key = chatId,
                factory = ChatThreadViewModelFactory(
                    container.authRepository,
                    container.chatRepository,
                    container.userRepository,
                    chatId
                )
            )
            ChatThreadScreen(viewModel = viewModel)
        }
    }
}

@Composable
private fun FullScreenMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.AppBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = HuntmateColors.Amber)
        Text(
            message,
            modifier = Modifier.padding(top = 16.dp),
            color = HuntmateColors.Navy,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
