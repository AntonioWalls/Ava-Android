package com.antoniowalls.ava.profile.data

import android.graphics.Bitmap
import android.net.Uri
import com.antoniowalls.ava.profile.model.ChatUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

internal class ProfileRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun load(uid: String): ChatUser? =
        firestore.collection("Users").document(uid).get().await().toObject(ChatUser::class.java)

    suspend fun save(original: ChatUser, updated: ChatUser) {
        val uid = updated.userid
        val directoryChanged = original.userName != updated.userName ||
            original.imageUrl != updated.imageUrl
        firestore.runBatch { batch ->
            batch.set(firestore.collection("Users").document(uid), updated)
            if (directoryChanged) {
                batch.set(firestore.collection("PublicProfiles").document(uid), updated.directoryFields())
            }
        }.await()
    }

    suspend fun upload(uid: String, uri: Uri?, bitmap: Bitmap?): String {
        val reference = storage.reference.child("profile_images/$uid/avatar.jpg")
        if (uri != null) {
            reference.putFile(uri).await()
        } else {
            val data = withContext(Dispatchers.Default) {
                ByteArrayOutputStream().use {
                    check(requireNotNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 80, it))
                    it.toByteArray()
                }
            }
            val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
            reference.putBytes(data, metadata).await()
        }
        return reference.downloadUrl.await().toString()
    }
}

internal fun ChatUser.directoryFields(): Map<String, String> = mapOf(
    "userid" to userid,
    "userName" to userName.orEmpty(),
    "imageUrl" to imageUrl.orEmpty()
)
