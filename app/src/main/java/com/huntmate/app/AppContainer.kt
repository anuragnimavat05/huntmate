package com.huntmate.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.huntmate.app.data.repository.FirebaseAuthRepository
import com.huntmate.app.data.repository.FirebaseChatRepository
import com.huntmate.app.data.repository.FirebaseConnectionRepository
import com.huntmate.app.data.repository.FirebaseDiscoveryRepository
import com.huntmate.app.data.repository.FirebaseMediaRepository
import com.huntmate.app.data.repository.FirebasePostRepository
import com.huntmate.app.data.repository.FirebaseUserRepository
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.ChatRepository
import com.huntmate.app.domain.repository.ConnectionRepository
import com.huntmate.app.domain.repository.DiscoveryRepository
import com.huntmate.app.domain.repository.MediaRepository
import com.huntmate.app.domain.repository.PostRepository
import com.huntmate.app.domain.repository.UserRepository
import com.huntmate.app.domain.usecase.CompatibilityScorer
import com.huntmate.app.domain.usecase.ProfileValidator

class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val authRepository: AuthRepository = FirebaseAuthRepository(auth)
    val mediaRepository: MediaRepository = FirebaseMediaRepository(storage)
    val userRepository: UserRepository = FirebaseUserRepository(firestore)
    val postRepository: PostRepository = FirebasePostRepository(
        firestore = firestore,
        mediaRepository = mediaRepository,
        currentUserIdProvider = { auth.currentUser?.uid }
    )
    val discoveryRepository: DiscoveryRepository = FirebaseDiscoveryRepository(
        firestore = firestore,
        userRepository = userRepository,
        compatibilityScorer = CompatibilityScorer()
    )
    val connectionRepository: ConnectionRepository = FirebaseConnectionRepository(firestore)
    val chatRepository: ChatRepository = FirebaseChatRepository(firestore)
    val profileValidator = ProfileValidator()
}
