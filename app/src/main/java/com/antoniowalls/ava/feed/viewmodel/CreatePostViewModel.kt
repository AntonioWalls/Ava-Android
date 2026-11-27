package com.antoniowalls.ava.feed.viewmodel

import com.antoniowalls.ava.R

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.ava.profile.model.ChatUser
import com.antoniowalls.ava.feed.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

sealed class CreatePostUiState {
    object Idle : CreatePostUiState()
    object Loading : CreatePostUiState()
    data class Success(val postId: String) : CreatePostUiState()
    data class Error(val message: String) : CreatePostUiState()
}

class CreatePostViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    // Para almacenar temporalmente los datos del post
    val postText = MutableStateFlow("")
    val imageUri = MutableStateFlow<Uri?>(null)
    val imageBitmap = MutableStateFlow<Bitmap?>(null) // Para fotos de cámara
    // videoUri: MutableStateFlow<Uri?> = MutableStateFlow(null) // Para videos (futuro)

    private val _currentUserProfile = MutableStateFlow<ChatUser?>(null)

    init {
        fetchCurrentUserProfile()
    }

    private fun fetchCurrentUserProfile() {
        auth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                try {
                    val doc = firestore.collection("Users").document(userId).get().await()
                    _currentUserProfile.value = doc.toObject(ChatUser::class.java)
                } catch (e: Exception) {
                    Log.e("CreatePostVM", "Error fetching current user profile", e)
                }
            }
        }
    }


    fun createPost() {
        val currentText = postText.value.trim()
        val currentImageUri = imageUri.value
        val currentImageBitmap = imageBitmap.value
        val currentUser = auth.currentUser
        val userProfile = _currentUserProfile.value

        if (currentUser == null || userProfile == null) {
            _uiState.value = CreatePostUiState.Error(getApplication<Application>().getString(R.string.post_profile_required))
            return
        }

        if (currentText.isBlank() && currentImageUri == null && currentImageBitmap == null) {
            _uiState.value = CreatePostUiState.Error(getApplication<Application>().getString(R.string.post_empty))
            return
        }

        _uiState.value = CreatePostUiState.Loading

        viewModelScope.launch {
            var uploadedImageUrl: String? = null
            val postId = UUID.randomUUID().toString()

            try {
                // Subir imagen si existe
                if (currentImageUri != null) {
                    val imageRef = storage.reference.child("post_media/${currentUser.uid}/$postId/image.jpg")
                    imageRef.putFile(currentImageUri).await()
                    uploadedImageUrl = imageRef.downloadUrl.await().toString()
                } else if (currentImageBitmap != null) {
                    val imageRef = storage.reference.child("post_media/${currentUser.uid}/$postId/image.jpg")
                    val baos = ByteArrayOutputStream()
                    currentImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val data = baos.toByteArray()
                    imageRef.putBytes(data, com.google.firebase.storage.StorageMetadata.Builder()
                        .setContentType("image/jpeg").build()).await()
                    uploadedImageUrl = imageRef.downloadUrl.await().toString()
                }

                // Crear objeto Post
                val newPost = Post(
                    id = postId,
                    userId = currentUser.uid,
                    userName = userProfile.userName ?: getApplication<Application>().getString(R.string.ui_anonimo),
                    userProfileImageUrl = userProfile.imageUrl,
                    text = if (currentText.isNotBlank()) currentText else null,
                    imageUrl = uploadedImageUrl,
                    videoUrl = null, // Implementar subida de video después
                    timestamp = null
                )

                // Guardar Post en Firestore
                firestore.collection("Posts").document(postId).set(newPost).await()
                _uiState.value = CreatePostUiState.Success(postId)
                // Limpiar campos después de éxito
                postText.value = ""
                imageUri.value = null
                imageBitmap.value = null

            } catch (e: Exception) {
                Log.e("CreatePostVM", "Error creando post", e)
                _uiState.value = CreatePostUiState.Error(getApplication<Application>().getString(R.string.post_failed))
            }
        }
    }
    fun resetState() {
        _uiState.value = CreatePostUiState.Idle
    }
}
