package com.antoniowalls.ava.feed.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antoniowalls.ava.R
import com.antoniowalls.ava.feed.model.Comment
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

// --- COMPOSABLE PARA EL CONTENIDO DEL BOTTOM SHEET DE COMENTARIOS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheetContent(
    postId: String,
    comments: List<Comment>,
    onAddComment: suspend (postId: String, text: String) -> Boolean,
    onDismiss: () -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var sendFailed by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val send = {
        val draft = newCommentText.trim()
        if (!sending && draft.isNotEmpty()) {
            sending = true
            sendFailed = false
            keyboardController?.hide()
            scope.launch {
                try {
                    sendFailed = !onAddComment(postId, draft)
                    if (!sendFailed && newCommentText.trim() == draft) newCommentText = ""
                } finally {
                    sending = false
                }
            }
        }
    }

    // Scroll al último comentario cuando la lista cambie
    LaunchedEffect(comments.lastOrNull()?.id) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = UiDimensions.size200,
                max = UiDimensions.commentSheetHeight(androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp)
            )
            .padding(top = UiDimensions.large) // Padding para que no choque con el handle del sheet
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = UiDimensions.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.ui_comentarios), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.ui_cerrar_comentarios))
            }
        }
        Divider(modifier = Modifier.padding(vertical = UiDimensions.base))

        if (comments.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.ui_aun_no_hay_comentarios_se_el_primero))
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f) // Para que ocupe el espacio disponible
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = UiDimensions.large, vertical = UiDimensions.base),
                verticalArrangement = Arrangement.spacedBy(UiDimensions.basePlus)
            ) {
                items(comments, key = { it.id }) { comment ->
                    CommentItem(comment)
                }
            }
        }

        Divider()
        if (sendFailed) {
            Text(
                stringResource(R.string.comment_send_error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = UiDimensions.large)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UiDimensions.large, vertical = UiDimensions.base),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.ui_escribe_un_comentario)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() })
            )
            IconButton(
                onClick = send,
                enabled = !sending && newCommentText.isNotBlank()
            ) {
                Icon(Icons.Filled.Send, stringResource(R.string.ui_enviar_comentario))
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(comment.userProfileImageUrl)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_foreground) // Cambia placeholder
                .error(R.drawable.ic_launcher_foreground)
                .build(),
            contentDescription = stringResource(R.string.ui_foto_de, comment.userName.orEmpty()),
            modifier = Modifier
                .size(UiDimensions.size36)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(UiDimensions.medium))
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(UiDimensions.base))
                .padding(horizontal = UiDimensions.basePlus, vertical = UiDimensions.compact)
                .weight(1f) // Para que el texto se ajuste si es largo
        ) {
            Text(
                text = comment.userName ?: stringResource(R.string.ui_anonimo),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = comment.timestamp?.let { dateFormat.format(it) } ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
