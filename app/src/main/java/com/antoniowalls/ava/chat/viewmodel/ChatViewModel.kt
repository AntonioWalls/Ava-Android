package com.antoniowalls.ava.chat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.ava.R
import com.antoniowalls.ava.chat.model.ChatMessage
import com.antoniowalls.ava.profile.model.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatViewModel(private val application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _messageText = MutableStateFlow("")
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _chattingWithUser = MutableStateFlow<ChatUser?>(null)
    private val _sending = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    val messageText = _messageText.asStateFlow()
    val chatMessages = _chatMessages.asStateFlow()
    val chattingWithUser = _chattingWithUser.asStateFlow()
    val sending = _sending.asStateFlow()
    val error = _error.asStateFlow()

    private var roomId: String? = null
    private var loadJob: Job? = null
    private var messagesListener: ListenerRegistration? = null
    private var pending: ChatMessage? = null

    fun onMessageTextChanged(newText: String) {
        _messageText.value = newText
    }

    fun loadChatRoom(friendId: String) {
        loadJob?.cancel()
        messagesListener?.remove()
        roomId = null
        pending = null
        _chatMessages.value = emptyList()
        _chattingWithUser.value = null
        _error.value = null
        val uid = auth.currentUser?.uid ?: return
        if (uid == friendId) return
        loadJob = viewModelScope.launch {
            try {
                val friend = firestore.collection("PublicProfiles").document(friendId)
                    .get().await().toObject(ChatUser::class.java)
                    ?: error("Directory profile does not exist")
                val participants = listOf(uid, friendId).sorted()
                val id = participants.joinToString("-")
                val room = firestore.collection("Messages").document(id)
                room.set(mapOf("participants" to participants), SetOptions.merge()).await()
                _chattingWithUser.value = friend
                roomId = id
                messagesListener = room.collection("chats")
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(100)
                    .addSnapshotListener { snapshot, failure ->
                        if (failure != null) {
                            Log.w("ChatViewModel", "Message listener failed", failure)
                            _error.value = application.getString(R.string.chat_load_error)
                        } else if (auth.currentUser?.uid == uid && roomId == id) {
                            _chatMessages.value = snapshot?.documents.orEmpty().asReversed().mapNotNull {
                                it.toObject(ChatMessage::class.java)?.copy(id = it.id)
                            }
                        }
                    }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.w("ChatViewModel", "Chat load failed", failure)
                _error.value = application.getString(R.string.chat_load_error)
            }
        }
    }

    fun sendMessage() {
        if (_sending.value) return
        val content = _messageText.value.trim()
        val sender = auth.currentUser?.uid ?: return
        val receiver = _chattingWithUser.value?.userid ?: return
        val id = roomId ?: return
        if (content.isBlank()) return
        val message = pending?.takeIf {
            it.sender == sender && it.receiver == receiver && it.message == content
        } ?: ChatMessage(sender = sender, receiver = receiver, message = content, id = UUID.randomUUID().toString())
        pending = message
        _sending.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val room = firestore.collection("Messages").document(id)
                val reference = room.collection("chats").document(message.id)
                val fields = mapOf(
                    "id" to message.id, "sender" to sender, "receiver" to receiver,
                    "message" to content, "time" to FieldValue.serverTimestamp()
                )
                firestore.runTransaction { transaction ->
                    if (!transaction.get(reference).exists()) {
                        transaction.set(reference, fields)
                        transaction.update(room, mapOf(
                            "lastMessageId" to message.id,
                            "lastMessageAt" to FieldValue.serverTimestamp()
                        ))
                    }
                }.await()
                if (roomId == id) {
                    if (_messageText.value.trim() == content) _messageText.value = ""
                    pending = null
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.w("ChatViewModel", "Message send failed", failure)
                if (roomId == id) _error.value = application.getString(R.string.chat_send_error)
            } finally {
                _sending.value = false
            }
        }
    }

    override fun onCleared() {
        messagesListener?.remove()
        super.onCleared()
    }
}
