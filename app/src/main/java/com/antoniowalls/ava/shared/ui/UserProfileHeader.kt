package com.antoniowalls.ava.shared.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antoniowalls.ava.R // Para tu placeholder
import com.antoniowalls.ava.profile.model.ChatUser

@Composable
fun UserProfileHeader(
    userProfile: ChatUser?, // Recibe el ChatUser (puede ser null mientras carga)
    onClick: () -> Unit, // Acción al hacer clic (ej. navegar a EditProfileScreen)
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = UiDimensions.base, vertical = UiDimensions.small) // Padding más pequeño para caber en TopAppBar
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(userProfile?.imageUrl)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_foreground) // Cambia a tu placeholder
                .error(R.drawable.ic_launcher_foreground)       // Cambia a tu placeholder de error
                .build(),
            contentDescription = stringResource(R.string.ui_foto_de_perfil),
            modifier = Modifier
                .size(UiDimensions.size32) // Tamaño más pequeño para TopAppBar
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(UiDimensions.base))
        Text(
            text = userProfile?.userName ?: stringResource(R.string.ui_usuario), // Muestra "Usuario" si el nombre aún no carga
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimaryContainer // Asumiendo que estará sobre un primaryContainer
        )
    }
}
