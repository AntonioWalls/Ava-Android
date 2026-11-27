package com.antoniowalls.ava.chat.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.antoniowalls.ava.chat.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    chatMessage: ChatMessage,
    isCurrentUser: Boolean
) {
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start

    // Formateador para la hora del mensaje
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UiDimensions.base, vertical = UiDimensions.small),
        horizontalAlignment = alignment
    ) {
        // Mostrar el nombre del remitente si no es el usuario actual
        if (!isCurrentUser) {
            Text(
                text = chatMessage.sender ?: stringResource(R.string.ui_desconocido), // O podrías tener el nombre del remitente aquí
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = UiDimensions.base, bottom = UiDimensions.xs)
            )
        }

        Box(
            modifier = Modifier
                .wrapContentWidth() // La burbuja solo ocupa el ancho necesario
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = if (isCurrentUser) UiDimensions.large else UiDimensions.small,
                        topEnd = if (isCurrentUser) UiDimensions.small else UiDimensions.large,
                        bottomStart = UiDimensions.large,
                        bottomEnd = UiDimensions.large
                    )
                )
                .padding(horizontal = UiDimensions.medium, vertical = UiDimensions.base)
        ) {
            Column(horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start) {
                Text(
                    text = chatMessage.message ?: "",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(UiDimensions.small))
                Text(
                    text = chatMessage.time?.let { timeFormatter.format(it) } ?: "--:--",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = UiDimensions.base // Sombra para destacar el campo de entrada
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = UiDimensions.base, vertical = UiDimensions.base),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.ui_escribe_un_mensaje)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(UiDimensions.extraLarge),
                maxLines = 5 // Permite varias líneas pero no ilimitadas
            )
            Spacer(modifier = Modifier.width(UiDimensions.base))
            IconButton(
                onClick = onSendClick,
                enabled = value.isNotBlank() && enabled,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (value.isNotBlank() && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (value.isNotBlank() && enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.ui_enviar_mensaje)
                )
            }
        }
    }
}