package com.huntmate.app.domain.repository

import android.net.Uri
import com.huntmate.app.data.model.Comment
import com.huntmate.app.data.model.Post
import kotlinx.coroutines.flow.Flow

data class CreatePostResult(
    val usedMediaUpload: Boolean,
    val warningMessage: String? = null
)

interface PostRepository {
    fun observeFeed(): Flow<List<Post>>
    fun observeSavedPostIds(userId: String): Flow<Set<String>>
    fun observeComments(postId: String): Flow<List<Comment>>
    suspend fun createPost(post: Post, mediaUri: Uri?, mediaType: String): CreatePostResult
    suspend fun toggleLike(postId: String, userId: String)
    suspend fun toggleSave(postId: String, userId: String)
    suspend fun addComment(postId: String, comment: Comment)
}
