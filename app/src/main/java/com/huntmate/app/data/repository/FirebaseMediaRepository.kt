package com.huntmate.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.huntmate.app.domain.repository.MediaRepository
import kotlinx.coroutines.tasks.await

class FirebaseMediaRepository(
    private val storage: FirebaseStorage
) : MediaRepository {

    override suspend fun uploadProfilePhoto(userId: String, uri: Uri): String {
        return upload("media/profile/$userId/${System.currentTimeMillis()}.jpg", uri)
    }

    override suspend fun uploadPostImage(userId: String, uri: Uri): String {
        return upload("media/posts/$userId/${System.currentTimeMillis()}.jpg", uri)
    }

    override suspend fun uploadPostMedia(userId: String, uri: Uri, mediaType: String): String {
        val extension = if (mediaType == "video") "mp4" else "jpg"
        return upload("media/posts/$userId/${System.currentTimeMillis()}.$extension", uri)
    }

    private suspend fun upload(path: String, uri: Uri): String {
        val ref = storage.reference.child(path)
        return try {
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (exception: Exception) {
            throw Exception(mapStorageMessage(exception), exception)
        }
    }

    private fun mapStorageMessage(exception: Exception): String {
        val storageException = exception as? StorageException ?: return exception.message ?: "Upload failed"
        return when (storageException.errorCode) {
            StorageException.ERROR_BUCKET_NOT_FOUND ->
                "Firebase Storage is not set up yet. Create Storage in Firebase Console first."
            StorageException.ERROR_OBJECT_NOT_FOUND ->
                "Upload could not be completed. Create Firebase Storage and try again."
            StorageException.ERROR_NOT_AUTHENTICATED ->
                "Please sign in again before uploading."
            StorageException.ERROR_NOT_AUTHORIZED ->
                "Storage rules blocked this upload. Check Firebase Storage rules."
            else -> storageException.message ?: "Upload failed"
        }
    }
}
