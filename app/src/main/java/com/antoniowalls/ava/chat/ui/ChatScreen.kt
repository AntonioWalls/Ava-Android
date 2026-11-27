package com.antoniowalls.ava.chat.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.R
import android.util.Log // Asegúrate de tener este import para Log.e
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider // Import para ViewModelProvider.Factory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.antoniowalls.ava.chat.model.ChatMessage // Asegúrate que el path sea correcto
import com.antoniowalls.ava.chat.viewmodel.ChatViewModel // Asegúrate que el path sea correcto
import com.antoniowalls.ava.shared.viewmodel.AppViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    friendId: String?,
    chatViewModel: ChatViewModel = viewModel(factory = AppViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val messages by chatViewModel.chatMessages.collectAsState()
    val messageText by chatViewModel.messageText.collectAsState()
    val chattingWithUser by chatViewModel.chattingWithUser.collectAsState()
    val sending by chatViewModel.sending.collectAsState()
    val error by chatViewModel.error.collectAsState()

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(friendId) {
        if (!friendId.isNullOrBlank()) {
            chatViewModel.loadChatRoom(friendId)
        } else {
            Log.e("ChatScreen", "Friend ID is null or blank. Cannot load chat.")
        }
    }

    LaunchedEffect(messages.lastOrNull()?.id) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                try { // Añadir try-catch por si la lista está vacía momentáneamente durante una recomposición rápida
                    if (lazyListState.layoutInfo.totalItemsCount > 0 && messages.indices.contains(messages.size - 1)) {
                        lazyListState.animateScrollToItem(messages.size - 1)
                    }
                } catch (e: Exception) {
                    Log.e("ChatScreenScroll", "Error scrolling to item: ${e.localizedMessage}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chattingWithUser?.userName ?: stringResource(R.string.ui_chat)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.ui_volver)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (friendId.isNullOrBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.ui_no_se_pudo_cargar_el_chat_usuario))
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = UiDimensions.base),
                        contentPadding = PaddingValues(top = UiDimensions.base, bottom = UiDimensions.base),
                        verticalArrangement = Arrangement.spacedBy(UiDimensions.base)
                    ) {
                        items(messages, key = { it.id }) { message -> // Usar message.id como key
                            MessageBubble(
                                chatMessage = message,
                                isCurrentUser = message.sender == currentUserId
                            )
                        }
                    }

                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(UiDimensions.base)) }
                    ChatInputField(
                        value = messageText,
                        onValueChange = { chatViewModel.onMessageTextChanged(it) },
                        onSendClick = {
                            if (messageText.isNotBlank()) {
                                chatViewModel.sendMessage()
                            }
                        },
                        enabled = chattingWithUser != null && !sending
                    )
                }
            }
        }
    )
}

// --- MUEVE LA FACTORY AQUÍ ---
// Factory para ChatViewModel si no usas Hilt para la inyección de ViewModels con Application context
class ChatViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
