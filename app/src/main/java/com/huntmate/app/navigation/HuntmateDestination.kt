package com.huntmate.app.navigation

sealed class HuntmateDestination(val route: String) {
    data object Feed : HuntmateDestination("feed")
    data object Discover : HuntmateDestination("discover")
    data object CreatePost : HuntmateDestination("create_post")
    data object Connections : HuntmateDestination("connections")
    data object Profile : HuntmateDestination("profile")
    data object Activity : HuntmateDestination("activity")
    data object ChatList : HuntmateDestination("chat_list")
    data object ChatThread : HuntmateDestination("chat_thread/{chatId}") {
        fun createRoute(chatId: String): String = "chat_thread/$chatId"
    }
}
