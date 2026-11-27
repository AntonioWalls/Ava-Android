package com.antoniowalls.ava.chat.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit // Icono para editar perfil
import androidx.compose.material.icons.filled.Settings // O un icono de engranaje
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antoniowalls.ava.R
import com.antoniowalls.ava.profile.model.ChatUser
import com.antoniowalls.ava.chat.viewmodel.UserListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    navController: NavController,
    userListViewModel: UserListViewModel = viewModel()
) {
    val users by userListViewModel.users.collectAsState()
    val isLoading by userListViewModel.isLoading.collectAsState()
    val errorMessage by userListViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            userListViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_iniciar_un_chat)) },
                actions = { // Añadimos la sección de acciones
                    IconButton(onClick = {
                        navController.navigate("edit_profile_screen") // Navegar a la pantalla de edición
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings, // O Icons.Filled.Edit
                            contentDescription = stringResource(R.string.ui_editar_perfil)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer // Color para el icono de acción
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (users.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = UiDimensions.base)
                ) {
                    items(users, key = { it.userid }) { user ->
                        UserListItem(
                            user = user,
                            onClick = {
                                navController.navigate("chat_screen/${user.userid}")
                            }
                        )
                        Divider(thickness = UiDimensions.hairline, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            } else if (errorMessage == null) {
                Text(
                    text = stringResource(R.string.ui_no_hay_otros_usuarios_disponibles),
                    modifier = Modifier.align(Alignment.Center).padding(UiDimensions.large),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// UserListItem Composable (sin cambios, ya lo tenías)
@Composable
fun UserListItem(
    user: ChatUser,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = UiDimensions.large, vertical = UiDimensions.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.imageUrl)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .build(),
            contentDescription = stringResource(R.string.ui_foto_de_perfil_de, user.userName.orEmpty()),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(UiDimensions.touchTarget)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(UiDimensions.large))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.userName ?: stringResource(R.string.ui_usuario_desconocido),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
