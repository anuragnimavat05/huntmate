package com.huntmate.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.huntmate.app.data.model.Comment
import com.huntmate.app.data.model.Post
import com.huntmate.app.domain.repository.CreatePostResult
import com.huntmate.app.domain.repository.MediaRepository
import com.huntmate.app.domain.repository.PostRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebasePostRepository(
    private val firestore: FirebaseFirestore,
    private val mediaRepository: MediaRepository,
    private val currentUserIdProvider: () -> String?
) : PostRepository {

    override fun observeFeed(): Flow<List<Post>> = callbackFlow {
        val registration = firestore.collection(POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val posts = snapshot?.documents.orEmpty().mapNotNull { document ->
                        document.toObject(Post::class.java)?.copy(id = document.id)
                    }
                    trySend(posts).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeSavedPostIds(userId: String): Flow<Set<String>> = callbackFlow {
        val registration = firestore.collection(USERS).document(userId)
            .collection(SAVED_POSTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.documents.orEmpty().map { it.id }.toSet()).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val registration = firestore.collection(POSTS).document(postId)
            .collection(COMMENTS)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val comments = snapshot?.documents.orEmpty().mapNotNull { document ->
                        document.toObject(Comment::class.java)?.copy(id = document.id)
                    }
                    trySend(comments).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createPost(post: Post, mediaUri: Uri?, mediaType: String): CreatePostResult {
        val userId = currentUserIdProvider().orEmpty()
        val mediaUploadResult = if (mediaUri != null) {
            runCatching { mediaRepository.uploadPostMedia(userId, mediaUri, mediaType) }
        } else {
            Result.success("")
        }
        val mediaUrl = mediaUploadResult.getOrDefault("")
        firestore.collection(POSTS).add(post.copy(imageUrl = mediaUrl, mediaType = mediaType)).await()
        return if (mediaUri != null && mediaUrl.isBlank()) {
            CreatePostResult(
                usedMediaUpload = false,
                warningMessage = "Post published without media because Firebase Storage blocked the upload."
            )
        } else {
            CreatePostResult(usedMediaUpload = mediaUrl.isNotBlank())
        }
    }

    override suspend fun toggleLike(postId: String, userId: String) {
        val likeDoc = firestore.collection(POSTS).document(postId).collection(LIKES).document(userId)
        val postDoc = firestore.collection(POSTS).document(postId)
        firestore.runTransaction { transaction ->
            val post = transaction.get(postDoc).toObject(Post::class.java) ?: return@runTransaction
            val alreadyLiked = transaction.get(likeDoc).exists()
            if (alreadyLiked) {
                transaction.delete(likeDoc)
                transaction.update(postDoc, "likeCount", (post.likeCount - 1).coerceAtLeast(0))
            } else {
                transaction.set(likeDoc, mapOf("createdAt" to System.currentTimeMillis()))
                transaction.update(postDoc, "likeCount", post.likeCount + 1)
            }
        }.await()
    }

    override suspend fun toggleSave(postId: String, userId: String) {
        val saveDoc = firestore.collection(USERS).document(userId).collection(SAVED_POSTS).document(postId)
        if (saveDoc.get().await().exists()) {
            saveDoc.delete().await()
        } else {
            saveDoc.set(mapOf("savedAt" to System.currentTimeMillis())).await()
        }
    }

    override suspend fun addComment(postId: String, comment: Comment) {
        val postDoc = firestore.collection(POSTS).document(postId)
        firestore.runTransaction { transaction ->
            val post = transaction.get(postDoc).toObject(Post::class.java) ?: return@runTransaction
            val commentDoc = postDoc.collection(COMMENTS).document()
            transaction.set(commentDoc, comment.copy(id = commentDoc.id))
            transaction.update(postDoc, "commentCount", post.commentCount + 1)
        }.await()
    }

    private companion object {
        const val POSTS = "posts"
        const val USERS = "users"
        const val COMMENTS = "comments"
        const val LIKES = "likes"
        const val SAVED_POSTS = "saved_posts"
    }
}
