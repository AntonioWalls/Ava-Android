package com.antoniowalls.ava.feed.viewmodel

import com.antoniowalls.ava.R

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.ava.profile.model.ChatUser
import com.antoniowalls.ava.feed.model.Comment
import com.antoniowalls.ava.feed.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FeedViewModel(private val application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedPostComments = MutableStateFlow<List<Comment>>(emptyList())
    val selectedPostComments: StateFlow<List<Comment>> = _selectedPostComments.asStateFlow()

    private val _selectedPostIdForComments = MutableStateFlow<String?>(null)
    val selectedPostIdForComments: StateFlow<String?> = _selectedPostIdForComments.asStateFlow()

    // --- PERFIL DEL USUARIO ACTUAL ---
    private val _currentUserProfile = MutableStateFlow<ChatUser?>(null)
    val currentUserProfile: StateFlow<ChatUser?> = _currentUserProfile.asStateFlow() // Expuesto públicamente
    // --- FIN PERFIL DEL USUARIO ACTUAL ---

    private var postsListenerRegistration: ListenerRegistration? = null
    private var commentsListenerRegistration: ListenerRegistration? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("FeedViewModel", "Coroutine Exception: ${throwable.localizedMessage}", throwable)
        _errorMessage.value = application.getString(R.string.feed_error)
        _isLoading.value = false
    }

    init {
        fetchCurrentUserProfile() // Cargar perfil del usuario actual
        fetchPosts() // Cargar posts
    }

    private fun fetchCurrentUserProfile() {
        auth.currentUser?.uid?.let { userId ->
            viewModelScope.launch(coroutineExceptionHandler) {
                try {
                    val doc = firestore.collection("Users").document(userId).get().await()
                    val user = doc.toObject(ChatUser::class.java)
                    _currentUserProfile.value = user
                    Log.d("FeedViewModel", "Perfil del usuario actual cargado: ${user?.userName}")
                } catch (e: Exception) {
                    Log.e("FeedViewModel", "Error fetching current user profile", e)
                    _errorMessage.value = application.getString(R.string.feed_profile_failed)
                }
            }
        }
    }

    fun fetchPosts() {
        _isLoading.value = true
        _errorMessage.value = null
        postsListenerRegistration?.remove()
        postsListenerRegistration = firestore.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshots, e ->
                _isLoading.value = false // Mover esto aquí para que se actualice después de la primera carga o error
                if (e != null) {
                    Log.w("FeedViewModel", "Listen failed for posts.", e)
                    _errorMessage.value = application.getString(R.string.feed_posts_failed)
                    _posts.value = emptyList()
                    return@addSnapshotListener
                }
                val postList = mutableListOf<Post>()
                for (doc in snapshots!!) {
                    try {
                        val post = doc.toObject(Post::class.java).copy(id = doc.id)
                        postList.add(post)
                    } catch (ex: Exception) {
                        Log.e("FeedViewModel", "Error convirtiendo documento a Post: ${doc.id}", ex)
                    }
                }
                _posts.value = postList
            }
    }

    fun toggleLike(postId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _errorMessage.value = application.getString(R.string.feed_like_sign_in)
            return
        }
        val postRef = firestore.collection("Posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
            // val currentLikesCount = (snapshot.getLong("likesCount") ?: 0L).toInt() // No es necesario leerlo si usamos increment

            if (currentLikedBy.contains(currentUserId)) {
                transaction.update(postRef, "likedBy", FieldValue.arrayRemove(currentUserId))
                transaction.update(postRef, "likesCount", FieldValue.increment(-1))
            } else {
                transaction.update(postRef, "likedBy", FieldValue.arrayUnion(currentUserId))
                transaction.update(postRef, "likesCount", FieldValue.increment(1))
            }
            null
        }.addOnSuccessListener {
            Log.d("FeedViewModel", "Like/Unlike exitoso para el post: $postId")
        }.addOnFailureListener { e ->
            Log.e("FeedViewModel", "Error al dar Like/Unlike al post: $postId", e)
            _errorMessage.value = application.getString(R.string.feed_like_failed)
        }
    }

    fun selectPostForComments(postId: String?) {
        if (postId == _selectedPostIdForComments.value && postId != null) return

        commentsListenerRegistration?.remove()
        _selectedPostIdForComments.value = postId
        _selectedPostComments.value = emptyList()

        if (postId != null) {
            commentsListenerRegistration = firestore.collection("Posts").document(postId)
                .collection("Comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("FeedViewModel", "Comments listen failed for post $postId.", e)
                        _errorMessage.value = application.getString(R.string.feed_comments_failed)
                        _selectedPostComments.value = emptyList()
                        return@addSnapshotListener
                    }
                    val commentsList = mutableListOf<Comment>()
                    for (doc in snapshots!!) {
                        try {
                            val comment = doc.toObject(Comment::class.java).copy(id = doc.id)
                            commentsList.add(comment)
                        } catch (ex: Exception) {
                            Log.e("FeedViewModel", "Error convirtiendo documento a Comment: ${doc.id}", ex)
                        }
                    }
                    _selectedPostComments.value = commentsList.asReversed()
                }
        }
    }

    suspend fun addComment(postId: String, commentText: String): Boolean {
        val currentUserId = auth.currentUser?.uid
        val userProfile = _currentUserProfile.value

        if (currentUserId == null || userProfile == null) {
            _errorMessage.value = application.getString(R.string.feed_comment_sign_in)
            return false
        }
        if (commentText.isBlank()) {
            _errorMessage.value = application.getString(R.string.feed_comment_empty)
            return false
        }

        val commentId = UUID.randomUUID().toString()
        val newComment = Comment(
            id = commentId,
            postId = postId,
            userId = currentUserId,
            userName = userProfile.userName ?: application.getString(R.string.ui_anonimo),
            userProfileImageUrl = userProfile.imageUrl,
            text = commentText,
            timestamp = null
        )

        return try {
            val post = firestore.collection("Posts").document(postId)
            firestore.runTransaction { transaction ->
                check(transaction.get(post).exists())
                transaction.set(post.collection("Comments").document(commentId), newComment)
                transaction.update(post, "commentsCount", FieldValue.increment(1), "lastCommentId", commentId)
            }.await()
            true
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Comment send failed", e)
            false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        postsListenerRegistration?.remove()
        commentsListenerRegistration?.remove()
        Log.d("FeedViewModel", "ViewModel cleared, listeners removed.")
    }
}
