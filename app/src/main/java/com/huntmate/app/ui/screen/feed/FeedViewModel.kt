package com.huntmate.app.ui.screen.feed

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huntmate.app.data.model.Comment
import com.huntmate.app.data.model.Post
import com.huntmate.app.domain.repository.AuthRepository
import com.huntmate.app.domain.repository.PostRepository
import com.huntmate.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FeedFilter {
    ALL,
    SAVED,
    CITY
}

data class FeedComposerState(
    val caption: String = "",
    val cityTag: String = "",
    val selectedMediaUri: Uri? = null,
    val selectedMediaType: String = "image",
    val selectedFilter: FeedFilter = FeedFilter.ALL,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class FeedViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _composerState = MutableStateFlow(FeedComposerState())
    val composerState: StateFlow<FeedComposerState> = _composerState.asStateFlow()
    private val currentUserId = authRepository.currentUser?.uid.orEmpty()

    val feed = combine(
        postRepository.observeFeed(),
        postRepository.observeSavedPostIds(currentUserId),
        composerState,
        userRepository.observeProfile(currentUserId)
    ) { posts, savedPostIds, state, profile ->
        when (state.selectedFilter) {
            FeedFilter.ALL -> posts
            FeedFilter.SAVED -> posts.filter { savedPostIds.contains(it.id) }
            FeedFilter.CITY -> posts.filter {
                profile?.destinationCity?.isNotBlank() == true &&
                    it.cityTag.equals(profile.destinationCity, ignoreCase = true)
            }
        } to savedPostIds
    }.catch { throwable ->
        _composerState.value = _composerState.value.copy(errorMessage = throwable.message)
        emit(emptyList<Post>() to emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Post>() to emptySet())

    fun updateCaption(value: String) {
        _composerState.value = _composerState.value.copy(caption = value, successMessage = null, errorMessage = null)
    }

    fun updateCityTag(value: String) {
        _composerState.value = _composerState.value.copy(cityTag = value, successMessage = null, errorMessage = null)
    }

    fun updateSelectedMedia(uri: Uri?, mediaType: String) {
        _composerState.value = _composerState.value.copy(
            selectedMediaUri = uri,
            selectedMediaType = mediaType,
            successMessage = null,
            errorMessage = null
        )
    }

    fun updateFilter(filter: FeedFilter) {
        _composerState.value = _composerState.value.copy(selectedFilter = filter)
    }

    fun createPost() {
        viewModelScope.launch {
            runCatching {
                val user = authRepository.currentUser ?: error("You must be signed in")
                val profile = userRepository.getProfile(user.uid) ?: error("Complete your profile first")
                val post = Post(
                    authorId = user.uid,
                    authorName = profile.name,
                    authorPhotoUrl = profile.profilePhotoUrl,
                    caption = composerState.value.caption,
                    cityTag = composerState.value.cityTag,
                    hashtags = composerState.value.caption.split(" ")
                        .filter { it.startsWith("#") }
                        .map { it.removePrefix("#") }
                )
                postRepository.createPost(
                    post = post,
                    mediaUri = composerState.value.selectedMediaUri,
                    mediaType = composerState.value.selectedMediaType
                )
            }.onSuccess { result ->
                _composerState.value = FeedComposerState(
                    successMessage = result.warningMessage ?: "Post published"
                )
            }.onFailure { throwable ->
                _composerState.value = _composerState.value.copy(errorMessage = throwable.message)
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            authRepository.currentUser?.uid?.let { postRepository.toggleLike(postId, it) }
        }
    }

    fun toggleSave(postId: String) {
        viewModelScope.launch {
            authRepository.currentUser?.uid?.let { postRepository.toggleSave(postId, it) }
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser ?: return@launch
            val profile = userRepository.getProfile(user.uid) ?: return@launch
            postRepository.addComment(
                postId = postId,
                comment = Comment(
                    postId = postId,
                    authorId = user.uid,
                    authorName = profile.name,
                    content = text
                )
            )
        }
    }
}

class FeedViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FeedViewModel(authRepository, userRepository, postRepository) as T
    }
}
