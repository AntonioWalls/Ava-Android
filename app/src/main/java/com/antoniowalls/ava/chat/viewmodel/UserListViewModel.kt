package com.antoniowalls.ava.chat.viewmodel

import com.antoniowalls.ava.R

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.ava.profile.model.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserListViewModel(private val application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _users = MutableStateFlow<List<ChatUser>>(emptyList())
    val users: StateFlow<List<ChatUser>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("UserListViewModel", "Coroutine Exception: ${throwable.localizedMessage}", throwable)
        _errorMessage.value = application.getString(R.string.feed_error)
        _isLoading.value = false
    }

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isLoading.value = true
            _errorMessage.value = null
            val currentUserId = auth.currentUser?.uid

            if (currentUserId == null) {
                Log.e("UserListViewModel", "Current user is null. Cannot fetch users.")
                _errorMessage.value = application.getString(R.string.directory_sign_in)
                _users.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            try {
                val result = firestore.collection("PublicProfiles").limit(100).get().await()
                val userList = mutableListOf<ChatUser>()
                for (document in result.documents) {
                    val user = document.toObject(ChatUser::class.java)
                    if (user != null && user.userid != currentUserId) { // Excluir al usuario actual
                        userList.add(user)
                    }
                }
                _users.value = userList
                Log.d("UserListViewModel", "Fetched ${userList.size} users.")
            } catch (e: Exception) {
                Log.e("UserListViewModel", "Error fetching users from Firestore", e)
                _errorMessage.value = application.getString(R.string.directory_load_failed)
                _users.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Para limpiar el mensaje de error si se maneja en la UI
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
