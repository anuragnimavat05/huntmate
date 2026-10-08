package com.huntmate.app.domain.repository

import android.net.Uri

interface MediaRepository {
    suspend fun uploadProfilePhoto(userId: String, uri: Uri): String
    suspend fun uploadPostImage(userId: String, uri: Uri): String
    suspend fun uploadPostMedia(userId: String, uri: Uri, mediaType: String): String
}
